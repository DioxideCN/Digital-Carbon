package org.sangonomiya.app.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.jsonwebtoken.lang.Assert;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.RowBounds;
import org.sangonomiya.app.core.NotificationHelper;
import org.sangonomiya.app.core.Response;
import org.sangonomiya.app.core.ResponseBounce;
import org.sangonomiya.app.core.message.MessageAction;
import org.sangonomiya.app.core.message.MessageBuilder;
import org.sangonomiya.app.entity.Approval;
import org.sangonomiya.app.entity.Company;
import org.sangonomiya.app.entity.UserVO;
import org.sangonomiya.app.extension.annotation.AzygoService;
import org.sangonomiya.app.extension.annotation.CompanyOperation;
import org.sangonomiya.app.extension.annotation.RequestConsistency;
import org.sangonomiya.app.mapper.ApprovalMapper;
import org.sangonomiya.app.mapper.RelateMapper;
import org.sangonomiya.app.service.IApprovalService;
import org.sangonomiya.app.service.ICompanyService;
import org.sangonomiya.app.service.INotificationService;
import org.sangonomiya.app.service.IUserService;
import org.sangonomiya.groovy.DateHandler;
import org.sangonomiya.kotlin.Pair;
import org.sangonomiya.kotlin.XSS;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author Dioxide.CN
 * @date 2023/4/8 9:12
 * @since 1.0
 */
@Slf4j
@Service
public class ApprovalServiceImpl extends ServiceImpl<ApprovalMapper, Approval> implements IApprovalService {

    @Resource
    private INotificationService notificationService;
    @Resource
    private IUserService userService;
    @Resource
    private ICompanyService companyService;
    @Resource
    private RelateMapper relateMapper;

    @Resource
    private NotificationHelper notificationHelper;

    @Value("${alipay.retry}")
    private int maximumRetry;

    @Override
    @RequestConsistency
    @CompanyOperation(operator = false)
    @AzygoService(keyPos = {0})
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public ResponseBounce<Object> submitApprovalAction(String username, String companyName, String title, Integer emergency_level, String content, boolean notifyAll) {
        UserVO submitter = userService.getUserByUsername(username);
        Assert.notNull(submitter);
        Company company = companyService.getCompanyByName(companyName);
        Assert.notNull(company);

        if (emergency_level < 0 || emergency_level > 3)
            return Response.fail("错误的信息");

        String message = new MessageBuilder()
                .title("「" + username + "」提交了审批")
                .body("企业成员「" + username + "」提交了审批，请尽快处理！")
                .action(MessageAction.NONE_ACTION)
                .secret("GOTO_APPROVAL")
                .key("approval_" + username)
                .build();

        // 半小时内只能通知一次管理员
        if (isRecentlySubmittedApproval(submitter, message) && notifyAll)
            return Response.fail("提交失败！30分钟内只能通知一次管理员");

        int count = 0;
        Approval insertedApproval = null;
        do {
            if (count == maximumRetry) break;
            // 使用Kotlin的伴生函数进行过滤
            title = XSS.Companion.filter(title);
            content = XSS.Companion.filter(content);

            Approval approval = new Approval()
                    .setTitle(title)
                    .setContent(content)
                    .setEmergencyLevel(emergency_level)
                    .setState(0)
                    .setCreateTime(DateHandler.getDateString());
            // 将approval插入数据库
            this.baseMapper.insert(approval);
            insertedApproval = this.baseMapper.selectOne(new QueryWrapper<Approval>()
                    .eq("title", title)
                    .eq("content", content)
                    .eq("emergency_level", emergency_level)
                    .eq("state", 0)
                    .eq("create_time", approval.getCreateTime()));
        } while (insertedApproval == null);
        if (insertedApproval == null) {
            log.error("error occurred while inserting new approval into database for {}", username);
            return Response.fail("未知错误");
        }

        // 联表更新数据
        relateMapper.doRelateApprovalToUserAndCompanyAction(
                insertedApproval.getId(),
                company.getId(),
                submitter.getId()
        );

        if (notifyAll) {
            // 获取所有管理员
            List<UserVO> operators = relateMapper.selectAllOperatorFromCompany(company.getId());
            if (operators.size() < 1)
                log.error("company {} has no operator", companyName);

            for (UserVO operator : operators) {
                // 向每个管理员发送消息
                notificationService.sendMessageToClient(
                        message, submitter, operator);
            }
        }
        return Response.success();
    }

    @Override
    @RequestConsistency
    @CompanyOperation(operator = false)
    public ResponseBounce<Object> getUserAllApproval(String username, String companyName, int perPageCount, int page) {
        if (perPageCount > 100) perPageCount = 100;

        UserVO submitter = userService.getUserByUsername(username);
        Assert.notNull(submitter);
        Company company = companyService.getCompanyByName(companyName);
        Assert.notNull(company);

        int startIndex = (page - 1) * perPageCount;

        List<Approval> approvals = relateMapper.selectUserAllApproval(submitter.getId(), new RowBounds(startIndex, perPageCount));
        if (approvals.size() < 1)
            return Response.fail("没有更多信息了");

        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("total", relateMapper.countUserAllApproval(submitter.getId()));
        resultMap.put("selected", approvals);

        return Response.success(resultMap);
    }

    @Override
    @RequestConsistency
    @CompanyOperation
    public ResponseBounce<Object> getCompanyAllApproval(String username, String companyName, int perPageCount, int page) {
        if (perPageCount > 100) perPageCount = 100;

        UserVO operator = userService.getUserByUsername(username);
        Assert.notNull(operator);
        Company company = companyService.getCompanyByName(companyName);
        Assert.notNull(company);

        int startIndex = (page - 1) * perPageCount;

        List<Map<Object, Object>> approvals = relateMapper.selectCompanyAllApproval(company.getId(), new RowBounds(startIndex, perPageCount));
        if (approvals.size() < 1)
            return Response.fail("没有更多信息了");
        // 具体化每个approval的提交用户
        for (Map<Object, Object> approval : approvals) {
            approval.put("createUser",
                    userService.getUserById(((Long) approval.get("createUser")).intValue()).getUsername());
        }

        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("total", relateMapper.countCompanyAllApproval(company.getId()));
        resultMap.put("selected", approvals);

        return Response.success(resultMap);
    }

    /**
     * 用户撤销审批请求
     * @param username 撤销发起人
     * @param companyName 企业名称
     * @param approvalId 审批表ID
     * @return 返回是否撤销审批成功
     */
    @Override
    @RequestConsistency
    @CompanyOperation(operator = false)
    @AzygoService(keyPos = {1, 2})
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public ResponseBounce<Object> revertApproval(String username, String companyName, int approvalId) {
        UserVO user = userService.getUserByUsername(username);
        Assert.notNull(user);
        Company company = companyService.getCompanyByName(companyName);
        Assert.notNull(company);

        if (approvalId < 1) return Response.fail("错误的数据源");
        Approval approval = this.baseMapper.selectById(approvalId);
        if (approval == null) return Response.fail("审批表不存在！");
        if (approval.getState() != 0) return Response.fail("该审批表已被审核！");

        // 级联删除
        this.baseMapper.deleteById(approvalId);
        return Response.success();
    }

    /**
     * 用户对审批表申请加急处理
     * @param username 用户
     * @param companyName 企业名称
     * @param approvalId 审批表ID
     * @param approvalTitle 审批表标题
     * @return 返回是否申请加急成功，如果失败一并返回失败原因
     */
    @Override
    @RequestConsistency
    @CompanyOperation(operator = false)
    @AzygoService(keyPos = {0})
    public ResponseBounce<Object> urgeApproval(String username, String companyName, int approvalId, String approvalTitle) {
        UserVO urgePoster = userService.getUserByUsername(username);
        Assert.notNull(urgePoster);
        Company company = companyService.getCompanyByName(companyName);
        Assert.notNull(company);

        String message = new MessageBuilder()
                .title("「" + username + "」申请加急审批")
                .body("企业成员「" + username + "」申请了对审批表「" + approvalTitle + "」的加急处理，请尽快前往处理！")
                .action(MessageAction.NONE_ACTION)
                .secret("GOTO_APPROVAL")
                .key("approval_" + username)
                .build();

        // 半小时内只能通知一次管理员
        if (isRecentlySubmittedApproval(urgePoster, message))
            return Response.fail("半小时内只能加急一次审批表。");

        // 获取所有管理员
        List<UserVO> operators = relateMapper.selectAllOperatorFromCompany(company.getId());
        if (operators.size() < 1)
            log.error("company {} has no operator", companyName);

        for (UserVO operator : operators) {
            // 向每个管理员发送消息
            notificationService.sendMessageToClient(
                    message, urgePoster, operator);
        }
        return Response.success();
    }

    @Override
    @RequestConsistency
    @CompanyOperation
    @AzygoService(keyPos = {1, 2})
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public ResponseBounce<Object> examineApproval(String username, String companyName, int approvalId, boolean isPassed) {
        UserVO operator = userService.getUserByUsername(username);
        Assert.notNull(operator);
        Company company = companyService.getCompanyByName(companyName);
        Assert.notNull(company);

        if (approvalId < 1) return Response.fail("错误的数据源");
        Approval approval = this.baseMapper.selectById(approvalId);
        if (approval == null) return Response.fail("审批表不存在！");
        if (approval.getState() != 0) return Response.fail("该审批表已被审核！");

        approval.setState(isPassed ? 1: 2);
        this.baseMapper.updateById(approval);
        return Response.success();
    }

    private boolean isRecentlySubmittedApproval(UserVO sender, String message) {
        return notificationHelper
                .source(Pair.of(message, sender))
                .time(30)
                .timeUnit(TimeUnit.MINUTES)
                .reverse()
                .same();
    }

}
