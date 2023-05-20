package org.sangonomiya.app.config;

import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Dioxide.CN
 * @date 2023/3/9 17:28
 * @since 1.0
 */
@Configuration
public class RabbitmqConfig {

    @Value("${alipay.timeout}")
    private long timeout;

    //普通交换机名称
    public static final String DEFAULT_EXCHANGE = "default.digital.carbon.exchange";
    //死信交换机名称
    public static final String DEAD_EXCHANGE = "dead.digital.carbon.exchange";
    // 普通队列名称
    public static final String PAYMENT_QUEUE = "payment.digital.carbon.queue";
    // 普通队列路由key
    public static final String PAYMENT_QUEUE_ROUTING_KEY = "payment.digital.carbon.routing.key";
    // 死信队列名称
    public static final String DEAD_QUEUE = "dead.digital.carbon.queue";
    // 死信队列路由key
    public static final String DEAD_QUEUE_ROUTING_KEY = "dead.digital.carbon.routing.key";

    /**
     * 声名 普通交换机（DEFAULT_EXCHANGE）
     * @return 普通交换机
     */
    @Bean
    public DirectExchange defaultExchange(){
        return new DirectExchange(DEFAULT_EXCHANGE);
    }

    /**
     * 声名 死信交换机（DEAD_EXCHANGE）
     * @return 死信交换机
     */
    @Bean
    public DirectExchange deadExchange(){
        return new DirectExchange(DEAD_EXCHANGE);
    }

    /**
     * 声名普通队列 QUEUE TTL为10s
     * @return 普通队列 QUEUE
     */
    @Bean
    public Queue paymentQueue(){
        //声名参数
        Map<String, Object> arguments = new HashMap<>(3);
        //设置死信交换机
        arguments.put("x-dead-letter-exchange", DEAD_EXCHANGE);
        //设置死信RoutingKey
        arguments.put("x-dead-letter-routing-key", DEAD_QUEUE_ROUTING_KEY);
        //设置 TTL 10s
        arguments.put("x-message-ttl", timeout);

        return QueueBuilder.durable(PAYMENT_QUEUE).withArguments(arguments).build();
    }

    /**
     * 声名死信队列 QUEUE_DEAD
     * @return 死信队列 QUEUE_DEAD
     */
    @Bean
    public Queue deadQueue(){
        return QueueBuilder.durable(DEAD_QUEUE).build();
    }

    /**
     * 普通队列 通过 routingKey PAYMENT_QUEUE_ROUTING_KEY 绑定 普通交换机
     * @param paymentQueue 普通队列
     * @param defaultExchange 普通交换机
     * @return 绑定
     */
    @Bean
    public Binding queueABindingX(Queue paymentQueue, DirectExchange defaultExchange){
        return BindingBuilder.bind(paymentQueue).to(defaultExchange).with(PAYMENT_QUEUE_ROUTING_KEY);
    }

    /**
     * 死信队列 通过 routingKey QUEUE_DEAD_ROUTING_KEY 绑定 死信交换机
     * @param deadQueue 死信队列
     * @param deadExchange 死信交换机
     * @return 绑定
     */
    @Bean
    public Binding queueDBindingX(Queue deadQueue, DirectExchange deadExchange){
        return BindingBuilder.bind(deadQueue).to(deadExchange).with(DEAD_QUEUE_ROUTING_KEY);
    }

}
