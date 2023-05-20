package org.sangonomiya.app.service;

import org.jetbrains.annotations.NotNull;
import org.sangonomiya.app.core.ResponseBounce;
import org.sangonomiya.app.entity.Payment;
import org.sangonomiya.app.extension.annotation.RequestConsistency;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * @author Dioxide.CN
 * @date 2023/3/8 7:10
 * @since 1.0
 */
@Service
public interface IPaymentService {
    @Transactional(isolation= Isolation.READ_COMMITTED)
    @RequestConsistency
    ResponseBounce<Object> sendBuyRequestAndGetQRCodeAction(String username, int year);

    // 订单状态监听者 一般供给给前端进行校验在redis中的订单是否完成支付完成支付后再插入数据库中，如果超时则取消订单并从redis中删除记录
    @RequestConsistency
    ResponseBounce<Object> checkBizContentState(String username, String orderId);

    String alipayCallbackHandler(HttpServletRequest request);

    boolean hasHoldCompanyAccount(String username);

    boolean canDeletePaymentWhenExpire(int paymentId);

    Payment getPaymentByUsername(@NotNull String username);
}
