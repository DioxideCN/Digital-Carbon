package org.sangonomiya.app.extension.component;

import com.tencentcloudapi.captcha.v20190722.CaptchaClient;
import com.tencentcloudapi.captcha.v20190722.models.DescribeCaptchaResultRequest;
import com.tencentcloudapi.captcha.v20190722.models.DescribeCaptchaResultResponse;
import com.tencentcloudapi.common.Credential;
import com.tencentcloudapi.common.exception.TencentCloudSDKException;
import com.tencentcloudapi.common.profile.ClientProfile;
import com.tencentcloudapi.common.profile.HttpProfile;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * @author Dioxide.CN
 * @date 2023/3/17 23:37
 * @since 1.0
 */
@Component
@SuppressWarnings("all")
public class TencentUnit {

    @Value("${tencent.serverUrl}")
    private String ServerUrl;
    @Value("${tencent.secretId}")
    private String SecretId;
    @Value("${tencent.secretKey}")
    private String SecretKey;

    /**
     * 拼图滑块验证码的TencentCaptcha验证码后端校验工具，其返回的 CaptchaCode::Long 参数解释如下：<br>
     * 1 OK 验证通过<br>
     * 7 captcha no match 传入的Randstr不合法，请检查Randstr是否与前端返回的Randstr一致<br>
     * 8 ticket expired 传入的Ticket已过期（Ticket有效期5分钟），请重新生成Ticket、Randstr进行校验<br>
     * 9 ticket reused 传入的Ticket被重复使用，请重新生成Ticket、Randstr进行校验<br>
     * 15 decrypt fail 传入的Ticket不合法，请检查Ticket是否与前端返回的Ticket一致<br>
     * 16 appid-ticket mismatch 传入的CaptchaAppId错误，请检查CaptchaAppId是否与前端传入的CaptchaAppId一致，并且保障CaptchaAppId是从验证码控制台【验证管理】->【基础配置】中获取<br>
     * 21 diff 票据校验异常，可能的原因是（1）若Ticket包含terror前缀，一般是由于用户网络较差，导致前端自动容灾，而生成了容灾票据，业务侧可根据需要进行跳过或二次处理。（2）若Ticket不包含terror前缀，则是由于验证码风控系统发现请求有安全风险，业务侧可根据需要进行拦截。<br>
     * 100 appid-secretkey-ticket mismatch 参数校验错误，（1）请检查CaptchaAppId与AppSecretKey是否正确，CaptchaAppId、AppSecretKey需要在验证码控制台【验证管理】>【基础配置】中获取（2）请检查传入的Ticket是否由传入的CaptchaAppId生成<br>
     * <br>
     * 更详细的需要参考腾讯官方文档 <a href="https://cloud.tencent.com/document/product/1110/36926">验证码 核查验证码票据结果(Web及APP)</a>
     *
     * @param ticket 前端验证后传递票据
     * @param randomStr 前端验证后传递随机字符串
     * @return Long CaptchaCode
     */
    public Long JigsawCaptcha(String ticket, String randomStr) {
        if (ticket == null || randomStr == null) return -1L;
        return catpcha(
                "192477170",
                "GgaqKprLMZMYjzYcog0I8V1oQ",
                ticket,
                randomStr);
    }

    /**
     * 文字点选验证码的TencentCaptcha验证码后端校验工具，其返回的 CaptchaCode::Long 参数解释如下：<br>
     * 1 OK 验证通过<br>
     * 7 captcha no match 传入的Randstr不合法，请检查Randstr是否与前端返回的Randstr一致<br>
     * 8 ticket expired 传入的Ticket已过期（Ticket有效期5分钟），请重新生成Ticket、Randstr进行校验<br>
     * 9 ticket reused 传入的Ticket被重复使用，请重新生成Ticket、Randstr进行校验<br>
     * 15 decrypt fail 传入的Ticket不合法，请检查Ticket是否与前端返回的Ticket一致<br>
     * 16 appid-ticket mismatch 传入的CaptchaAppId错误，请检查CaptchaAppId是否与前端传入的CaptchaAppId一致，并且保障CaptchaAppId是从验证码控制台【验证管理】->【基础配置】中获取<br>
     * 21 diff 票据校验异常，可能的原因是（1）若Ticket包含terror前缀，一般是由于用户网络较差，导致前端自动容灾，而生成了容灾票据，业务侧可根据需要进行跳过或二次处理。（2）若Ticket不包含terror前缀，则是由于验证码风控系统发现请求有安全风险，业务侧可根据需要进行拦截。<br>
     * 100 appid-secretkey-ticket mismatch 参数校验错误，（1）请检查CaptchaAppId与AppSecretKey是否正确，CaptchaAppId、AppSecretKey需要在验证码控制台【验证管理】>【基础配置】中获取（2）请检查传入的Ticket是否由传入的CaptchaAppId生成<br>
     * <br>
     * 更详细的需要参考腾讯官方文档 <a href="https://cloud.tencent.com/document/product/1110/36926">验证码 核查验证码票据结果(Web及APP)</a>
     *
     * @param ticket 前端验证后传递票据
     * @param randomStr 前端验证后传递随机字符串
     * @return Long CaptchaCode
     */
    public Long TextureCaptcha(String ticket, String randomStr) {
        if (ticket == null || randomStr == null) return -1L;
        return catpcha(
                "191148854",
                "ux9sOMC7cYTnZpOvnmQh1IfOS",
                ticket,
                randomStr);
    }

    private Long catpcha(String captchaAppId, String appSecretKey, String ticket, String randomStr) {
        try{

            Credential cred = new Credential(SecretId, SecretKey);
            // 实例化一个http选项 可选的 没有特殊需求可以跳过
            HttpProfile httpProfile = new HttpProfile();
            httpProfile.setEndpoint(ServerUrl);

            // 实例化一个client选项 可选的 没有特殊需求可以跳过
            ClientProfile clientProfile = new ClientProfile();
            clientProfile.setHttpProfile(httpProfile);

            // 实例化要请求产品的client对象 clientProfile是可选的
            CaptchaClient client = new CaptchaClient(cred, "", clientProfile);

            // 实例化一个请求对象 每个接口都会对应一个request对象
            DescribeCaptchaResultRequest req = new DescribeCaptchaResultRequest();
            req.setCaptchaType(9L);
            req.setUserIp("127.0.0.1");

            /* 灾容票据验签 */
            req.setTicket(ticket);
            req.setRandstr(randomStr);

            /* 基础设置 */
            req.setCaptchaAppId(Long.valueOf(captchaAppId));
            req.setAppSecretKey(appSecretKey);

            // 返回的resp是一个DescribeCaptchaResultResponse的实例，与请求对象对应
            DescribeCaptchaResultResponse resp = client.DescribeCaptchaResult(req);
            return resp.getCaptchaCode();

        } catch (TencentCloudSDKException e) {

            e.printStackTrace();
            return 400L;

        }
    }

}
