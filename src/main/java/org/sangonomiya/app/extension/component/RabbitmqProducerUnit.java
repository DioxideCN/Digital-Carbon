package org.sangonomiya.app.extension.component;

import lombok.extern.slf4j.Slf4j;
import org.sangonomiya.app.config.RabbitmqConfig;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @author Dioxide.CN
 * @date 2023/3/9 18:53
 * @since 1.0
 */
@Slf4j
@Component
public class RabbitmqProducerUnit {

    @Resource
    private RabbitTemplate rabbitTemplate;

    public void send(String msg){
        rabbitTemplate.convertAndSend(
                RabbitmqConfig.DEFAULT_EXCHANGE,
                RabbitmqConfig.PAYMENT_QUEUE_ROUTING_KEY,
                msg);
    }

}
