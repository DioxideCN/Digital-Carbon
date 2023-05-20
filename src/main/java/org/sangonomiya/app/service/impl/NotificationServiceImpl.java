package org.sangonomiya.app.service.impl;

import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.sangonomiya.app.core.Response;
import org.sangonomiya.app.core.ResponseBounce;
import org.sangonomiya.app.core.lazy.LazyOptional;
import org.sangonomiya.kotlin.Pair;
import org.sangonomiya.app.entity.Notification;
import org.sangonomiya.app.entity.UserVO;
import org.sangonomiya.app.extension.annotation.RequestConsistency;
import org.sangonomiya.app.extension.component.WebSocketUnit;
import org.sangonomiya.app.mapper.NotificationMapper;
import org.sangonomiya.app.service.INotificationService;
import org.sangonomiya.app.service.IUserService;
import org.sangonomiya.groovy.DateHandler;
import org.sangonomiya.groovy.LambdaHandler;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Dioxide.CN
 * @date 2023/3/21 14:03
 * @since 1.0
 */
@Slf4j
@Service
public class NotificationServiceImpl implements INotificationService {

    @Resource
    private IUserService userService;
    @Resource
    private NotificationMapper notificationMapper;


    /**
     * 获取用户的第page页的20条消息
     * @param username 用户名
     * @param page 页码
     * @return 返回本页的所有消息
     */
    @Override
    @RequestConsistency
    public ResponseBounce<Object> getUserRecentNotification(String username, int page) {
        return getUserCountedNotification(username, page, 20);
    }

    /**
     * 获取用户的最近5条消息
     * @param username 用户名
     * @return 返回最近5条消息的封装体
     */
    @Override
    @RequestConsistency
    public ResponseBounce<Object> getUserSimpleNotification(String username) {
        return getUserCountedNotification(username, 1, 5);
    }

    /**
     * 依据id查询消息并获得其所在的页码
     * @param username 用户名
     * @param messageId 消息id
     * @return 返回封装结果
     */
    @Override
    @RequestConsistency
    public ResponseBounce<Object> getNotificationDetail(String username, int messageId) {
        UserVO receiver = userService.getUserByUsername(username);
        Notification notification = notificationMapper.selectById(messageId);
        if (notification == null)
            return Response.fail("消息不存在");
        if (!notification.getReceiver().equals(receiver.getId()))
            return Response.fail("你无法查看该消息");

        // 使用LazyOptional来为初级开发者提升血压 Pair<索引,messageId>
        Pair<Integer, Integer> pair = LazyOptional
                .of(() -> notificationMapper.selectList(
                        new QueryWrapper<Notification>()
                            .eq("receiver", 5)
                            .orderByDesc("create_time")
                            .orderByDesc("id")
                            .select("id"))
                    .stream()
                    .map(Notification::getId)
                    .map(LambdaHandler.Function.with(Pair::flip))
                    .filter(p -> p.right().equals(messageId))
                    .findFirst()
                    .orElse(Pair.empty()))
                .orElse(Pair.empty());

        notification.setReceiverUsername(receiver.getUsername());
        notification.setSenderUsername(
                userService
                        .getUserById(notification.getSender())
                        .getUsername());
        Map<String, Object> response = new HashMap<>();
        response.put("pageIndex", pair.left() / 20 + 1);
        response.put("message", notification);
        return Response.success(response);
    }

    /**
     * 封装的通用分页查表方法
     * @param username 用户名
     * @param page 页码
     * @param count 每页数量
     * @return 返回Response封装结果
     */
    private ResponseBounce<Object> getUserCountedNotification(String username, int page, int count) {
        UserVO awaitUser = userService.getUserByUsername(username);
        int userId = awaitUser.getId();
        Long total = countUserNotification(userId);
        if (total == 0) return Response.fail("没有更多消息了");

        List<Notification> result = getUserNotification(
                userId, page, count);
        if (result.size() == 0) return Response.fail("没有更多消息了");

        result.forEach(obj -> {
            obj.setSenderUsername(
                    userService.getUserById(obj.getSender()).getUsername());
            obj.setReceiverUsername(
                    userService.getUserById(obj.getReceiver()).getUsername());
            // 隐式包裹
            obj.setSender(null);
            obj.setReceiver(null);
        });

        Map<String, Object> response = new HashMap<>();
        response.put("unread", countUserUnread(userId));
        response.put("total", total);
        response.put("selected", result);
        return Response.success(response);
    }

    /**
     * 将消息标记为已读
     * @param username 用户名
     * @param messageId 消息id
     * @return 返校是否标记成功
     */
    @Override
    @RequestConsistency
    public ResponseBounce<Object> markMessageAsRead(String username, int messageId) {
        UserVO receiver = userService.getUserByUsername(username);
        if (markAsRead(receiver.getId(), messageId)) {
            return Response.success();
        } else {
            return Response.fail("你无法处理该消息");
        }
    }

    @Override
    @RequestConsistency
    public ResponseBounce<Object> batchMarkMessageAsRead(String username, int[] awaitMessage) {
        UserVO receiver = userService.getUserByUsername(username);
        int receiverId = receiver.getId();
        boolean trigger = true;
        for (int messageId : awaitMessage) {
            // 如果有false就存在无法处理的消息
            if (!markAsRead(receiverId, messageId)) {
                trigger = false;
            }
        }

        if (!trigger)
            return Response.fail("部分消息无法处理");
        return Response.success();
    }

    private boolean markAsRead(int receiverId, int messageId) {
        if (!isMessageReceiver(receiverId, messageId))
            return false;

        Notification notification = notificationMapper.selectById(messageId);
        // 考虑多线程环境
        if (notification.isRead())
            return true;

        notification.setRead(true);
        notificationMapper.updateById(notification);
        return true;
    }

    /**
     * 将消息标删除
     * @param username 用户名
     * @param messageId 消息id
     * @return 返校是否删除成功
     */
    @Override
    @RequestConsistency
    public ResponseBounce<Object> deleteMessage(String username, int messageId) {
        UserVO receiver = userService.getUserByUsername(username);
        if (canDelete(receiver.getId(), messageId)) {
            return Response.success();
        } else {
            return Response.fail("你无法处理该消息");
        }
    }

    @Override
    @RequestConsistency
    public ResponseBounce<Object> batchDeleteMessage(String username, int[] awaitMessage) {
        UserVO receiver = userService.getUserByUsername(username);
        int receiverId = receiver.getId();
        boolean trigger = true;
        for (int messageId : awaitMessage) {
            // 如果有false就存在无法处理的消息
            if (!canDelete(receiverId, messageId)) {
                trigger = false;
            }
        }

        if (!trigger)
            return Response.fail("你无法处理该消息");
        return Response.success();
    }

    private boolean canDelete(int userId, int messageId) {
        if (!isMessageReceiver(userId, messageId))
            return false;

        notificationMapper.deleteById(messageId);
        return true;
    }

    /**
     * 用户是否是某条消息的接收者
     * @param userId 用户id
     * @param messageId 消息id
     * @return true 是 false 不是
     */
    @SuppressWarnings("all")
    private boolean isMessageReceiver(int userId, int messageId) {
        Notification notification = notificationMapper.selectById(messageId);
        return notification.getReceiver() == userId;
    }

    /**
     * 用户是否是某条消息的发送者
     * @param userId 用户id
     * @param messageId 消息id
     * @return true 是 false 不是
     */
    @SuppressWarnings("all")
    private boolean isMessageSender(int userId, int messageId) {
        Notification notification = notificationMapper.selectById(messageId);
        return notification.getSender() == userId;
    }

    private List<Notification> getUserNotification(int userId, int page, int count) {
        // 未读优先再按时间排序
        Page<Notification> notificationPage = notificationMapper.selectPage(
                new Page<>(page, count),
                new QueryWrapper<Notification>()
                        .eq("receiver", userId)
                        .orderByDesc("create_time")
                        .orderByDesc("id")
        );
        return notificationPage.getRecords();
    }

    private Long countUserNotification(int userId) {
        return notificationMapper.selectCount(new QueryWrapper<Notification>()
                .eq("receiver", userId));
    }

    private Long countUserUnread(int userId) {
        return notificationMapper.selectCount(new QueryWrapper<Notification>()
                .eq("receiver", userId)
                .eq("is_read", 0));
    }

    /**
     * 将notification消息通知到客户端并存入数据库
     * @param message 消息内容，一般通过MessageBuilder来构建
     * @param sender 消息发送者
     * @param receiver 消息接受者
     */
    @Override
    public void sendMessageToClient(String message, UserVO sender, UserVO receiver) {
        log.info("{} send a message {} to {}", sender.getUsername(), message, receiver.getUsername());

        // 将消息插入数据库中暂存
        notificationMapper.insert(new Notification()
                .setReceiver(receiver.getId())
                .setSender(sender.getId())
                .setCreateTime(DateHandler.getDateString())
                .setMessage(message)
                .setRead(false));
        // 将插入的结果通知到前端
        Notification notification = notificationMapper.selectOne(new QueryWrapper<Notification>()
                .eq("receiver", receiver.getId())
                .orderByDesc("create_time")
                .orderByDesc("id")
                .last("LIMIT 1"));
        notification.setReceiverUsername(receiver.getUsername());
        notification.setSenderUsername(
                userService
                        .getUserById(notification.getSender())
                        .getUsername());
        // 这里调用websocketUnit向用户所在的session信道发送信息
        WebSocketUnit.notifyClient(
                receiver.getUsername(),
                JSONObject.toJSONString(notification));
    }

}
