package org.sangonomiya.app.extension.component;

import com.aliyun.auth.credentials.Credential;
import com.aliyun.auth.credentials.provider.StaticCredentialProvider;
import com.aliyun.sdk.service.dysmsapi20170525.AsyncClient;
import com.aliyun.sdk.service.dysmsapi20170525.models.SendSmsRequest;
import com.aliyun.sdk.service.dysmsapi20170525.models.SendSmsResponse;
import darabonba.core.client.ClientOverrideConfiguration;
import org.sangonomiya.groovy.RandomHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * 短信服务工具
 */
@Component
@SuppressWarnings("all")
public class SMSUnit {

    @Value("${sms.accessKeyId}")
    private String accessKeyId;
    @Value("${sms.accessKeySecret}")
    private String accessKeySecret;
    @Value("${sms.signName}")
    private String signName;
    @Value("${sms.templateCode}")
    private String templateCode;
    @Value("${sms.region}")
    private String region;

    private StaticCredentialProvider provider;

    /**
     * 发送短信验证码
     * @param phoneNumber 目标手机号
     * @param code 验证码（2-6位数字或字母）
     * @return Response，可转为Json格式
     */
    public SendSmsResponse sendCode(String phoneNumber , String code){
        checkProvider();
        AsyncClient client = AsyncClient.builder()
                .region(region) // Region ID
                .credentialsProvider(provider)
                .overrideConfiguration(
                        ClientOverrideConfiguration.create()
                                .setEndpointOverride("dysmsapi.aliyuncs.com")
                )
                .build();

        SendSmsRequest sendSmsRequest = SendSmsRequest.builder()
                .signName(signName)
                .templateCode(templateCode)
                .phoneNumbers(phoneNumber)
                .templateParam("{\"code\":"+code+"}").build();
        SendSmsResponse resp;
        try{
            CompletableFuture<SendSmsResponse> response = client.sendSms(sendSmsRequest);
            resp = response.get();
        }catch (InterruptedException | ExecutionException err){
            err.printStackTrace();
            resp = null;
        }
        client.close();
        return resp;
    }

    /**
     * 自动生成验证码，发送短信验证码，不推荐，无法获取到Response
     * @param phoneNumber 目标手机号
     * @return 生成的验证码
     */
    public String sendCode(String phoneNumber){
        String code = RandomHandler.randomStr(6,false);
        sendCode(phoneNumber, code);
        return code;
    }

    private void checkProvider(){
        if(provider != null) return;
        provider = StaticCredentialProvider.create(Credential.builder()
                .accessKeyId(accessKeyId)
                .accessKeySecret(accessKeySecret)
                .build());
    }

}
