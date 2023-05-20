package org.sangonomiya.kotlin.controller

import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import org.sangonomiya.app.core.ResponseBounce
import org.sangonomiya.app.service.IPaymentService
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import javax.annotation.Resource
import javax.servlet.http.HttpServletRequest

/**
 *
 * @author Dioxide.CN
 * @date 2023/4/8 16:54
 * @since 1.0
 */
@Api(tags = ["PaymentController"])
@RestController
@RequestMapping("/api/payment")
class PaymentController {

    @Resource
    private lateinit var paymentService: IPaymentService

    @ApiOperation(value = "发起订单请求")
    @PostMapping("/do-buy")
    fun initiateOrderRequest(
        @RequestParam("username") username: String,
        @RequestParam("year") year: Int
    ): ResponseBounce<Any> {
        return paymentService.sendBuyRequestAndGetQRCodeAction(
            username,
            year
        )
    }

    @ApiOperation(value = "查询订单状态")
    @PostMapping("/query-buy")
    fun queryOrderStateRequest(
        @RequestParam("username") username: String,
        @RequestParam("orderNum") orderNum: String
    ): ResponseBounce<Any> {
        return paymentService.checkBizContentState(
            username, orderNum
        )
    }

    @ApiOperation(value = "支付宝回调")
    @PostMapping("/callback")
    fun alipayCallbackAction(
        request: HttpServletRequest
    ): String {
        return paymentService.alipayCallbackHandler(request)
    }

}
