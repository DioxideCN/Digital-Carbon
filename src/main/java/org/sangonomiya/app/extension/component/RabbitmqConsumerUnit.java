package org.sangonomiya.app.extension.component;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.sangonomiya.app.config.RabbitmqConfig;
import org.sangonomiya.app.entity.Order;
import org.sangonomiya.app.extension.RedisAction;
import org.sangonomiya.app.mapper.OrderRecordMapper;
import org.sangonomiya.app.service.IUserService;
import org.sangonomiya.app.service.impl.PaymentServiceImpl;
import org.sangonomiya.app.entity.UserVO;
import org.sangonomiya.groovy.VerifyHandler;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

/**
 * 对死信队列的消息进行消费
 * @author Dioxide.CN
 * @date 2023/3/9 18:50
 * @since 1.0
 */
@Slf4j
@Component
public class RabbitmqConsumerUnit {

    @Resource
    private OrderRecordMapper orderRecordMapper;
    @Resource
    private IUserService userService;
    @Resource
    private RedisTemplate<String, String> redisTemplate;

    @Value("${alipay.timeout}")
    private long timeout;

    /**
     * 延迟消息消费者，这里的死信队列用来处理过期订单
     * <p>
     * 订单完成后由支付宝回调我们的后端接口，在回调处理接口中会更新订单状态
     * <p>
     * 而这里的死信消费队列会通过拆分死信消息得到订单号后去数据库中查询，若订单没有被回调
     * 处理接口进行修改则会被该死信消费队列处理为过期订单 {@link PaymentServiceImpl#BIZ_TIMEOUT}
     *
     * @param message 消息
     * @param channel 通信管道
     */
    @RabbitListener(queues = RabbitmqConfig.DEAD_QUEUE)
    public void paymentHandler(Message message, Channel channel){
        String msg = new String(message.getBody());
        String[] var1 = msg.split("[.]");
        if (var1.length != 3)
            log.error("can't resolve rabbitmq message in paymentHandler {}", msg);

        // 校验mq信息的合法性
        if (!VerifyHandler.of().username(var1[0]))
            log.error("can't resolve rabbitmq message's username in paymentHandler {}", var1[0]);
        if (!VerifyHandler.of().order(var1[1]))
            log.error("can't resolve rabbitmq message's order id in paymentHandler {}", var1[1]);

        // 分离数据
        String username = var1[0];
        String orderId = var1[1];
        UserVO user = userService.getUserByUsername(username);

        // 查找还未被处理的订单记录
        Order awaitOrder = orderRecordMapper.selectOne(new QueryWrapper<Order>()
                .eq("user_id", user.getId())
                .eq("order_num", orderId)
                .eq("biz_state", PaymentServiceImpl.BIZ_PAYING));

        if (awaitOrder == null) return; // 没有还未处理完的订单就直接返回

        awaitOrder.setBizState(PaymentServiceImpl.BIZ_TIMEOUT); // 修改为超时订单
        orderRecordMapper.update(awaitOrder, new UpdateWrapper<Order>()
                .eq("user_id", user.getId())
                .eq("order_num", orderId)
                .eq("biz_state", PaymentServiceImpl.BIZ_PAYING));
        // 对二级缓存模型进行更新并续签
        redisTemplate.opsForValue().set( // 强制更新redis状态如果存在直接覆盖更新
            RedisAction.order(username, orderId),
            "2", // 过期状态
            timeout * 2,
            TimeUnit.MILLISECONDS
        );

        log.info("user {}'s order {} has timeout", username, orderId);
    }

}
