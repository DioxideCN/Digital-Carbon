package org.sangonomiya.app.core.quartz;

import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.RowBounds;
import org.jetbrains.annotations.NotNull;
import org.quartz.JobExecutionContext;
import org.sangonomiya.app.core.NotificationHelper;
import org.sangonomiya.kotlin.Pair;
import org.sangonomiya.app.core.message.MessageAction;
import org.sangonomiya.app.core.message.MessageBuilder;
import org.sangonomiya.app.core.message.MessageType;
import org.sangonomiya.app.entity.Company;
import org.sangonomiya.app.entity.Payment;
import org.sangonomiya.app.entity.Permission;
import org.sangonomiya.app.entity.UserVO;
import org.sangonomiya.app.mapper.*;
import org.sangonomiya.app.service.*;
import org.sangonomiya.groovy.DateHandler;
import org.springframework.scheduling.quartz.QuartzJobBean;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * CleanCompanyJob任务在每天凌晨00:00的时候会检索过期和即将过期的企业账户，对于即将过期的企业账户所有者进行续费通知操作、对于已过期但未超过7天的企业账户所有者进行数据保留和续费通知，对于已经过期7天的企业账户进行数据删除操作和删除通知。
 * @author Dioxide.CN
 * @date 2023/4/1 15:51
 * @since 1.0
 */
@Slf4j
public class CleanCompanyJob extends QuartzJobBean{

    @Resource
    private INotificationService notificationService;
    @Resource
    private IUserService userService;
    @Resource
    private ICompanyService companyService;
    @Resource
    private IProductService productService;

    @Resource
    private NotificationHelper notificationHelper;

    @Resource
    private PaymentMapper paymentMapper;
    @Resource
    private RelateMapper relateMapper;
    @Resource
    private PermissionMapper permissionMapper;
    @Resource
    private CompanyMapper companyMapper;

    private static final int A_WEEK = 7;

    /**
     * 每小时对企业数据进行一次清洗
     */
    @Override
    public void executeInternal(@NotNull JobExecutionContext context) {
        log.info("starting clean repo_company database...");
        cleanPayment();
        log.info("successfully clean repo_company database.");
    }

    /**
     * cleanPayment对订单业务和企业数据进行合理的释放操作
     */
    private void cleanPayment() {
        String today = DateHandler.getDateString();
        List<Payment> payments = paymentMapper.selectList(null);

        int warnCount = 0;
        int deleteCount = 0;
        for (Payment payment : payments) {
            String endingDate = payment.getEndingDate();
            if (DateHandler.isBeforeToday(endingDate)) {
                // 已过期
                if (DateHandler.calcJetLag(today, endingDate) > A_WEEK) {
                    // 过期超过一周
                    if (removeExpireCompany(payment)) deleteCount++;
                } else {
                    // 过期未超过一周
                    if (warnExpireCompany(payment)) warnCount++;
                }
            }
        }
        log.info("warned {} companies, removed {} companies", warnCount, deleteCount);
    }

    /**
     * 已过期但未超过一周的订单进行资源释放警告
     * @param payment 订单
     */
    private boolean warnExpireCompany(Payment payment) {
        UserVO root = userService.getUserById(1);
        // 通知用户业务
        UserVO user = userService.getUserById(payment.getHoldUserId());
        if (user == null) {
            log.error("payment ghost exist {}", payment);
            return false;
        }
        String message = new MessageBuilder()
                .title("企业账户已过期")
                .type(MessageType.SIMPLE_MESSAGE)
                .body("您的企业账户已过期，企业数据将在7天后释放。重新购买企业资质后可继续使用企业账户。")
                .action(MessageAction.NONE_ACTION)
                .key("EXPIRING_WARNING")
                .build();
        // 在7天内每3天发送1次共发送2次，之后的见removeExpireCompany方法
        boolean trigger = notificationHelper
                .source(Pair.of(message, user))
                .time(3)
                .strictSame();
        if (!trigger) {
            notificationService.sendMessageToClient(message, root, user);
        }
        return true;
    }

    /**
     * 已过期且超过一周的订单进行资源释放和通知
     * @param payment 订单
     */
    private boolean removeExpireCompany(Payment payment) {
        UserVO root = userService.getUserById(1);
        // 通知用户业务
        UserVO user = userService.getUserById(payment.getHoldUserId());
        if (user == null) {
            log.error("payment ghost exist {}", payment);
            return false;
        }
        paymentMapper.deleteById(payment.getId()); // 删除资质
        // 释放企业所有资源
        Company company = companyService.inWhichCompany(user);
        if (company == null) {
            log.error("company has been delete {}", user);
            return false;
        }
        removeRelateAndOtherData(company);
        String message = new MessageBuilder()
                .title("企业账户已释放")
                .type(MessageType.SIMPLE_MESSAGE)
                .body("您的企业超过一周未及时续费，资源已被释放。")
                .action(MessageAction.NONE_ACTION)
                .secret("EXPIRING_COMPANY")
                .build();
        // 只在过期当天发送消息
        notificationService.sendMessageToClient(message, root, user);
        return true;
    }

    /**
     * 清除企业数据是一项非常繁琐而复杂的工作对于所有拥有联表数据的表。受每张联表中级联外键的影响，应该先通过连锁查询获取子表中的数据，接着删除联表中的关联性外键数据，再通过联表获取的数据从子表中删除，以便于完成数据的删除和取消关联。
     * @param company 被处理企业对象
     */
    private void removeRelateAndOtherData(@NotNull Company company) {
        int companyId = company.getId();

        // extra_lifecycle_<id> :: 删除企业生命周期表
        // extra_product_<id> :: 删除企业产品表
        productService.dropProductTable(String.valueOf(companyId));

        // company_id 获取权限组和员工
        List<Permission> groups = relateMapper.getCompanyAllGroup(companyId, new RowBounds());
        List<Map<Object, Object>> stuffs = relateMapper.getCompanyAllMember(companyId, new RowBounds());
        for (Permission group : groups) {
            // relate_company_permission :: 解除企业和所有权限组绑定
            relateMapper.deleteGroupCompanyRelation(group.getId());
            // repo_permission_group :: 删除所有企业权限组（除系统超管）
            if (group.getId() != 1 || !group.getGroupName().equals("超级管理员")) {
                permissionMapper.deleteById(group);
            }
        }
        for (Map<Object, Object> stuff : stuffs) {
            String username = (String) stuff.get("username");
            int userId = userService.getUserByUsername(username).getId();
            // relate_user_permission :: 解除所有企业员工和权限组绑定
            relateMapper.deleteUserGroupRelation(userId);
            // relate_user_company :: 解除所有企业员工关联
            relateMapper.deleteUserCompanyRelation(userId);
        }

        // repo_company :: 删除企业信息
        companyMapper.deleteById(companyId);
    }

}
