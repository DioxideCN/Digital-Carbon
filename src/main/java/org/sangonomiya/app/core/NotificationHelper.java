package org.sangonomiya.app.core;

import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.sangonomiya.kotlin.Pair;
import org.sangonomiya.app.entity.Notification;
import org.sangonomiya.app.entity.UserVO;
import org.sangonomiya.app.mapper.NotificationMapper;
import org.sangonomiya.groovy.DateHandler;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * NotificationHelper是一个多例Bean对象每次注入都会构造一个新的对象
 * @author Dioxide.CN
 * @date 2023/4/4 15:15
 * @since 1.0
 */
@Component
@Scope("prototype")
public class NotificationHelper {

    @Resource
    private NotificationMapper notificationMapper;

    private Notification source;
    private boolean reverse = false;
    private UserVO receiverOrSender;

    /**
     * 设置Helper的消息源
     * @param source 消息源
     * @return 责任链
     */
    public NotificationHelper source(Pair<String, UserVO> source) {
        Notification notification = new Notification();
        notification.setMessage(source.left());
        notification.setReceiver(source.right().getId());
        this.receiverOrSender = source.right();
        this.source = notification;
        return this;
    }

    public NotificationHelper reverse() {
        this.reverse = true;
        return this;
    }

    /* Condition条件组 */

    private boolean NEED_READ = false; // 是否只检查已读的消息
    private long TIME_SCOPE = 0L; // 检查多少天内的消息，0为全部
    private TimeUnit TIME_UNIT = TimeUnit.DAYS;

    public NotificationHelper read(boolean read) {
        this.NEED_READ = read;
        return this;
    }

    public NotificationHelper time(long time) {
        this.TIME_SCOPE = time;
        return this;
    }

    public NotificationHelper timeUnit(TimeUnit unit) {
        this.TIME_UNIT = unit;
        return this;
    }

    /* Condition条件组 */

    /**
     * strictSame会对消息的key和title都进行匹配
     * @return 返回是否存在相似消息
     */
    public boolean strictSame() {
        if (this.source == null) return false;
        return sameConditional(true);
    }

    /**
     * same只会对消息的key进行匹配
     * @return 返回是否存在相似消息
     */
    public boolean same() {
        if (this.source == null) return false;
        return sameConditional(false);
    }

    private boolean sameConditional(boolean isStrict) {
        JSONObject sourceMessage = JSONObject.parseObject(source.getMessage());

        QueryWrapper<Notification> queryWrapper = new QueryWrapper<Notification>()
                .eq(reverse?"sender":"receiver", receiverOrSender.getId())
                .orderByDesc("create_time"); // 按时间降序排序

        if (this.NEED_READ)
            queryWrapper.eq("is_read", 1);


        List<Notification> receivedList = notificationMapper.selectList(queryWrapper);
        if (receivedList.size() == 0) return false;

        boolean titleFlag = false;
        boolean keyFlag = false;
        // 对筛选出的消息进行判断是否有重复消息
        for (Notification target : receivedList) {
            JSONObject targetMessage = JSONObject.parseObject(target.getMessage());
            if (this.TIME_SCOPE > 0) {
                // 优化：因为是按时间降序的所以一旦超过范围就不再判断
                if (!timeConditional(target.getCreateTime(), this.TIME_SCOPE)) break;
            }

            if (isStrict) {
                // 匹配title
                String targetTitle = (String) targetMessage.get("title");
                if (targetTitle != null && targetTitle.equals(sourceMessage.get("title")))
                    titleFlag = true;
            }
            // 匹配key
            String targetKey = (String) targetMessage.get("key");
            if (targetKey != null && targetKey.equals(sourceMessage.get("key")))
                keyFlag = true;
        }
        return isStrict ? keyFlag && titleFlag : keyFlag;
    }

    /**
     * 时间条件判断
     * @param receivedTime 接收时间
     * @param jetLag 时间范围
     * @return true 消息在时间范围内 false 消息不在时间范围内
     */
    private boolean timeConditional(String receivedTime, long jetLag) {
        long realJetLag;
        if (TIME_UNIT == TimeUnit.DAYS) {
            realJetLag =
                    DateHandler.calcJetLag(receivedTime, DateHandler.getDateString());
        } else {
            switch (TIME_UNIT) {
                case SECONDS -> jetLag *= 1000; // s -> ms
                case MINUTES -> jetLag *= 60 * 1000; // m -> ms
                case HOURS -> jetLag*= 60 * 60 * 1000; // h -> ms
            }
            // millions::long
            realJetLag =
                    DateHandler.calcMillionJetLag(receivedTime, DateHandler.getDateString());
        }
        return realJetLag < jetLag;
    }

}
