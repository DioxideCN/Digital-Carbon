package org.sangonomiya.app.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;

/**
 * 建立WebSocket配置
 * @author Dioxide.CN
 * @date 2023/3/19 22:41
 * @since 1.0
 */
@Configuration
public class WebSocketConfig {

    /**
     * 服务器节点
     * 如果使用独立的servlet容器，而不是直接使用springboot的内置容器，就不要注入ServerEndPoint
     */
    @Bean
    public ServerEndpointExporter serverEndpointExporter() {
        return new ServerEndpointExporter();
    }

}
