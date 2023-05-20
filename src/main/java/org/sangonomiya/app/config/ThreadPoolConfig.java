package org.sangonomiya.app.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @author Dioxide.CN
 * @date 2023/3/8 15:21
 * @since 1.0
 */
@Getter
@Setter
@Configuration
public class ThreadPoolConfig {

    @Value("${task.pool.corePoolSize}")
    private int corePoolSize;
    @Value("${task.pool.maxPoolSize}")
    private int maxPoolSize;
    @Value("${task.pool.keepAliveSeconds}")
    private int keepAliveSeconds;
    @Value("${task.pool.queueCapacity}")
    private int queueCapacity;

}
