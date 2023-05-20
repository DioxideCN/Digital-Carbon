package org.sangonomiya.app.extension.component;

import com.alibaba.fastjson2.JSONObject;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipayTradePrecreateRequest;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.response.AlipayTradePrecreateResponse;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.google.gson.Gson;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import javax.annotation.PostConstruct;

/**
 * @author Dioxide.CN
 * @date 2023/3/8 08:10
 * @since 1.0
 */
@Component
public class AlipayUnit {

    @Value("${alipay.appId}")
    private String appId;
    @Value("${alipay.privateKey}")
    private String privateKey;
    @Value("${alipay.publicKey}")
    private String publicKey;
    @Value("${alipay.encryptKey}")
    private String encryptKey;
    @Value("${alipay.serverUrl}")
    private String serverUrl;

    private AlipayClient alipayClient;

    @PostConstruct
    public void generateAlipayClient() {
        this.alipayClient = new DefaultAlipayClient(
                serverUrl,
                appId,
                privateKey,
                "json",
                "GBK",
                publicKey,
                encryptKey);
    }

    /**
     * 向支付宝发起创建订单请求，通过配置注入应用ID、应用私钥、支付宝公钥构造完整的订单请求
     * <p>
     * 这些订单
     *
     * @param orderId 订单号
     * @param goodsName 商品名称
     * @param subject 商品标题
     * @param price 总价
     * @return 返回创建订单的响应参数
     */
    public AlipayTradePrecreateResponse createOrder(String orderId, String goodsName, String subject, double price) {
        AlipayTradePrecreateRequest request = new AlipayTradePrecreateRequest();
        // 设置订单
        request.setNotifyUrl("http://106.14.64.130:8080/api/payment/callback");

        JSONObject bizContent = new JSONObject();
        bizContent.put("out_trade_no", orderId);
        bizContent.put("goods_name", goodsName);
        bizContent.put("subject", subject);
        bizContent.put("total_amount", price);
        bizContent.put("quantity", 1);
        request.setBizContent(bizContent.toString());

        // 向支付宝平台发起订单生成请求
        AlipayTradePrecreateResponse response;
        try {
            response = alipayClient.execute(request);
        } catch (AlipayApiException e) {
            throw new RuntimeException(e);
        }

        if(response.isSuccess())
            return response;
        return null;
    }

    /**
     * 查询交易订单的订单状态，并返回查询的结果，这个方法会被前端每5秒调用一次
     * @param orderId 订单编号，在 {@link #createOrder} 执行之前被生成
     * @return 返回订单查询结果 null 表示订单不存在或请求失败
     */
    @Nullable
    public AlipayTradeQueryResponse queryOrder(String orderId) {
        AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
        JSONObject bizContent = new JSONObject();
        bizContent.put("out_trade_no", orderId);
        request.setBizContent(bizContent.toString());

        AlipayTradeQueryResponse response;
        try {
            response = alipayClient.execute(request);
        } catch (AlipayApiException e) {
            throw new RuntimeException(e);
        }

        if(response.isSuccess())
            return response;
        return null;
    }

}
