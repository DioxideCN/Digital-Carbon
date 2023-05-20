package org.sangonomiya.app.extension.component;

import com.alibaba.fastjson2.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;

@Component
public class CloudFlareUnit {

    @Value("${cloudflare.url}")
    private String url;
    @Value("${cloudflare.secretKey}")
    private String secretKey;

    @Resource
    private RestTemplate restTemplate;

    public boolean siteVerify(String response){
        try{
            LinkedMultiValueMap<String, String> request = new LinkedMultiValueMap<>();
            request.set("secret",secretKey);
            request.set("response",response);
            String result = restTemplate.postForObject(url,request,String.class);
            JSONObject json = JSONObject.parse(result);
            if(json == null) return false;
            return json.getBoolean("success");
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }
}
