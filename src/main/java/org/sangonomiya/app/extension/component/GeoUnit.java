package org.sangonomiya.app.extension.component;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;

@Component
public class GeoUnit {
    @Value("${geo.serverUrl}")
    private String serverUrl;

    @Value("${geo.key}")
    private String key;

    @Resource
    private RestTemplate restTemplate;

    /**
     * 根据地址字符串获取该地址的
     * @param address 需要查询的地址字符串
     * @return 返回一个本次查询结果的JSON
     */
    public String getGeoCode(String address){
        LinkedMultiValueMap<String, String> request = new LinkedMultiValueMap<>();
        request.set("key", key);
        request.set("address", address);
        return restTemplate.postForObject(serverUrl+"geocode/geo",request,String.class);
    }
}
