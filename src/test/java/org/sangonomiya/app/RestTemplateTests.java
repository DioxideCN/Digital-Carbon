package org.sangonomiya.app;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.sangonomiya.app.extension.component.CloudFlareUnit;
import org.sangonomiya.app.extension.component.GeoUnit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class RestTemplateTests {
    @Autowired
    private RestTemplate restTemplate;

    @Resource
    private GeoUnit geoUnit;

    @Resource
    CloudFlareUnit cloudFlareUnit;

    @Test
    public void cfTest(){
        String token="";
        System.out.println(cloudFlareUnit.siteVerify(token));
    }}
