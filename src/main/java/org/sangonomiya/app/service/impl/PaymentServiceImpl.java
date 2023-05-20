package org.sangonomiya.app.service.impl;

import com.alipay.api.response.AlipayTradePrecreateResponse;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.jsonwebtoken.lang.Assert;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.sangonomiya.app.core.Response;
import org.sangonomiya.app.core.ResponseBounce;
import org.sangonomiya.app.entity.Order;
import org.sangonomiya.app.entity.Payment;
import org.sangonomiya.app.extension.RedisAction;
import org.sangonomiya.app.extension.annotation.RequestConsistency;
import org.sangonomiya.app.extension.component.AlipayUnit;
import org.sangonomiya.app.extension.component.RabbitmqProducerUnit;
import org.sangonomiya.app.mapper.OrderRecordMapper;
import org.sangonomiya.app.mapper.PaymentMapper;
import org.sangonomiya.app.mapper.RelateMapper;
import org.sangonomiya.app.mapper.UserMapper;
import org.sangonomiya.app.service.ICompanyService;
import org.sangonomiya.app.service.IPaymentService;
import org.sangonomiya.app.service.IUserService;
import org.sangonomiya.app.entity.UserVO;
import org.sangonomiya.groovy.DataHandler;
import org.sangonomiya.groovy.DateHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * @author Dioxide.CN
 * @date 2023/3/8 7:09
 * @since 1.0
 */
@Service
@Slf4j
public class PaymentServiceImpl extends ServiceImpl<PaymentMapper, Payment> implements IPaymentService {

    @Resource
    private ICompanyService companyService;
    @Resource
    private IUserService userService;
    @Resource
    private RedisTemplate<String, String> redisTemplate;
    @Resource
    private AlipayUnit alipayUnit;
    @Resource
    private RelateMapper relateMapper;
    @Resource
    private PaymentMapper paymentMapper;
    @Resource
    private UserMapper userMapper;
    @Resource
    private OrderRecordMapper orderRecordMapper;
    @Resource
    private RabbitmqProducerUnit rabbitmqProducerUnit;

    @Value("${alipay.retry}")
    private int maximumRetry;
    @Value("${alipay.timeout}")
    private long timeout;
    @Value("${alipay.appId}")
    private String appId;

    public static final int BIZ_PAYING = 0; // 订单支付中
    public static final int BIZ_SUCCESS = 1; // 订单完成
    public static final int BIZ_TIMEOUT = 2; // 订单超时

    /**
     * 没错又是我重构的这个接口，这个创建订单接口重构后拥有非常精细的订单创建规则。
     * 现在前端无论是第一次购买、续费还是回购都是直接调用的这个业务方法，下面将花一点时间来介绍它是怎么实现的，以免后面忘记了。
     * <p>
     * 前端无论在什么地方创建订单逻辑都是依据 <code>创建订单 -> 回收订单创建消息 -> 唤起订单监听者 -> 完成订单操作</code> 流程来完成支付的。
     * 当前端将操作用户的 username 和购买时长 year 传递过来后，该业务会先进行操作一致性认证。
     * <p>
     * 再通过方法 {@link PaymentServiceImpl#hasHoldCompanyAccount(String)} 判断用户名下在 <code>repo_payment</code> 数据库中是否有未过期的账户资格并按一下三种情况进行操作：
     * <p>
     * 1. 有未过期的企业账户资格 -> 委派给 {@link PaymentServiceImpl#renewalServiceAndUpdateDatabase(UserVO, int)} 方法创建一个续费订单放入缓存池中
     * <p>
     * 2. 没有企业账户资格（包括已过期的状态） -> 委派给 {@link PaymentServiceImpl#createNewAccountOrderForUser(UserVO, int)} 方法创建一个购买订单放入缓存池中
     * <p>
     * 3. 作为企业员工与企业账户进行挂钩 -> 不允许创建任何订单
     * <p>
     * 在每个委派方法中都有自己的重复订单创建判断规则，他们会在对应的方法上以 javadoc 的形式被详细描述。
     *
     * @param username 账号名
     * @param year     购买时长
     * @return 返回订单是否创建成功
     * @author Dioxide.CN
     */
    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    @RequestConsistency
    public ResponseBounce<Object> sendBuyRequestAndGetQRCodeAction(String username, int year) {
        UserVO user = userService.getUserByUsername(username);
        Assert.notNull(user);

        // 前端用户有企业账户后不予以显示开通企业账户界面只显示续费操作界面
        if (hasHoldCompanyAccount(username)) {
            // 判断用户有企业账户委派给续费操作
            return renewalServiceAndUpdateDatabase(user, year);
        } else {
            // 没有企业账户但是属于其他企业体系中则不允许任何购买操作
            if (companyService.hasInCompany(user))
                return Response.fail("您已属于一个企业账户中");

            // 没有企业账户委派给购买操作
            return createNewAccountOrderForUser(user, year);
        }
        // 返回的订单信息会被包裹在 data:{} 中
    }

    /**
     * 重构后的创建购买订单业务会先生成订单号再进行一次订单存在性判断后再插入订单信息
     * <p>
     * 该类型的方法被优化成在插入前判断订单存在性，减少了订单存在性的判断此时优化了性能
     *
     * @param user 购买用户
     * @param year 购买时长
     * @return 支付宝订单创建结果
     */
    private ResponseBounce<Object> createNewAccountOrderForUser(UserVO user, int year) {
        log.info("user is now creating license {}", user.getUsername());
        String orderId = DataHandler.getOrderId();

        // 检测有无重复的购买企业资质订单
        if (hasOrderRecordInDBForPaying(user.getId()))
            return Response.fail("您还有未处理的购买订单");

        return Response.success(doCreateOrderForUser(orderId, user, year, "数碳智能基础版套餐"));
    }

    /**
     * 对username用户的企业账户创建续签订单，若账户过期只能重新购买而不是续签
     * <p>
     * 该类型的方法被优化成在插入前判断订单存在性，减少了订单存在性的判断此时优化了性能
     *
     * @param user 续签用户
     * @param year 续签时长
     */
    private ResponseBounce<Object> renewalServiceAndUpdateDatabase(UserVO user, int year) {
        log.info("user is now renewing license {}", user.getUsername());
        String orderId = DataHandler.getOrderId();

        // 检测有无重复的续费企业资质订单
        if (hasOrderRecordInDBForPaying(user.getId()))
            return Response.fail("您还有未处理的续费订单");

        return Response.success(doCreateOrderForUser(orderId, user, year, "数碳智能基础版续费"));
    }

    /**
     * 创建订单操作，该业务会失败重试，如果不希望重试需要在配置中将重试次数设置为 0
     *
     * @param orderId 订单号
     * @param user    创建订单的用户
     * @param year    购买时长
     * @return 支付宝响应结果
     */
    private AlipayTradePrecreateResponse doCreateOrderForUser(String orderId, UserVO user, int year, String message) {
        // 将记录插入订单数据库
        orderRecordMapper.insert(new Order()
                .setOrderNum(orderId)
                .setBizState(BIZ_PAYING)
                .setUserId(user.getId())
                .setBuyYear(year)
                .setCreateTime(DateHandler.getDateString()));

        // 若创建订单并会自动进行重试
        int count = 0;
        AlipayTradePrecreateResponse orderResponse = null;
        do { // 最多重试10次订单创建
            if (count == maximumRetry) break;
            orderResponse = alipayUnit.createOrder(
                    orderId,
                    message + "-" + year + "年",
                    message + "-" + year + "年",
                    0.01
            );
            count++;
        } while (orderResponse == null);

        // 这里开始不再使用redis来缓存订单，应该在rabbitmq中进行30分钟的延时缓冲
        StringJoiner delayMessage = new StringJoiner(".");
        delayMessage.add(user.getUsername());
        delayMessage.add(orderId);
        delayMessage.add(String.valueOf(year));
        rabbitmqProducerUnit.send(delayMessage.toString());

        return orderResponse;
    }

    /**
     * 只查询用户名下是否已经有一个未完成支付的订单
     *
     * @param user_id 用户唯一id
     * @return true 用户有未完成的订单 false 用户没有未完成的订单
     */
    private boolean hasOrderRecordInDBForPaying(int user_id) {
        List<Order> orders = relateMapper.queryUserAllOrder(user_id);
        int var2 = 0;
        for (Order order : orders) {
            if (var2 != 0)
                break;
            // 存在未完成支付的订单
            if (order.getBizState() == BIZ_PAYING)
                var2++;
        }
        return var2 != 0;
    }

    /**
     * 订单状态监听者 这是一个非常完美的二级缓存架构
     * 下面将花一点时间来介绍它是怎么设计并实现的以免后面忘记了
     * <p>
     * 前端发起订单请求后 {@link PaymentServiceImpl#sendBuyRequestAndGetQRCodeAction(String, int)}
     * 方法会将订单信息以 <code>username.order_id.year</code> 的形式加入mq管道中
     * <p>
     * 前端接收到订单创建成功的response后将开始以每n秒查询一次的速度向 checkBizContentState(String, String)
     * 方法发起订单状态查询请求 该方法首先会去redis中查找订单状态
     * 当发现不存在时才会去数据库中进行订单状态查找 与此同时将查询到的状态同步更新至 redis 中并将过期时间续签 订单超时时间的2倍 毫秒
     * <p>
     * 与此同时Rabbitmq的死信队列会不断监听即将过期的订单消息 当被消费者消费后mq会同步更新redis和数据库中的订单状态
     * <p>
     * 以上就完成了对订单状态的流转和更新，其中还有支付宝订单完成回调也会同步更新redis和数据库中的订单状态
     *
     * @param username 用户名
     * @param orderId  订单号
     * @return 返回订单状态
     * @author Dioxide.CN
     */
    @Override
    @RequestConsistency
    public ResponseBounce<Object> checkBizContentState(String username, String orderId) {
        UserVO user = userService.getUserByUsername(username);
        Assert.notNull(user);

        // 这里先从redis里面查询订单状态 这里势必会发生redis缓存击穿问题
        String paymentState = redisTemplate.opsForValue().get(RedisAction.order(username, orderId));

        // 如果没有再去数据库中查订单状态 这里势必会发生redis缓存穿透问题
        int state = -1;
        if (paymentState == null) {
            Order awaitOrder = orderRecordMapper.selectOne(new QueryWrapper<Order>()
                    .eq("user_id", user.getId())
                    .eq("order_num", orderId));

            // 如果数据库中还没有那就是订单不存在
            if (awaitOrder == null)
                return Response.fail("订单不存在");

            // 并将数据库中的订单状态缓冲至redis中形成二级缓存有效时间为1分钟
            // redis缓存过期后势必会发生缓存雪崩问题
            Boolean trigger = redisTemplate.opsForValue().setIfAbsent(
                    RedisAction.order(username, orderId),
                    String.valueOf(awaitOrder.getBizState()),
                    timeout * 2, // 更新2倍的超时时间缓存
                    TimeUnit.MILLISECONDS
            );
            if (Boolean.FALSE.equals(trigger))
                log.error("error occurred when insert new order record into redis.");

            state = awaitOrder.getBizState();
        } else {
            // 分离redis的订单信息得到订单状态
            try {
                state = Integer.parseInt(paymentState);
                // 这里续签是为了防止redis缓存击穿和穿透
                redisTemplate.opsForValue().set( // 续签redis缓存只续签1个超时单位
                        RedisAction.order(username, orderId),
                        paymentState,
                        timeout,
                        TimeUnit.MILLISECONDS
                );
            } catch (NumberFormatException e) {
                log.error("error occurred when converting redis value to int");
            }
        }

        if (state == -1)
            return Response.fail("订单不存在");
        return Response.success(state);
    }

    /**
     * 支付宝支付完成后接口回调事件处理
     * <p>
     * 回调文档见阿里支付宝开发者文档 <a href="https://opendocs.alipay.com/open/194/103296">异步通知说明 - 支付宝文档中心</a>
     *
     * @param request 支付宝回调参数列表
     */
    @Override
    public String alipayCallbackHandler(HttpServletRequest request) {
        // 拿到支付宝回调的所有参数
        Map<String, String[]> requestParams = request.getParameterMap();
        Map<String, String> params = new HashMap<>();
        for (String key : requestParams.keySet()) {
            String[] values = requestParams.get(key);
            String valueStr = "";
            for (int i = 0; i < values.length; i++) {
                valueStr = (i == values.length - 1) ? valueStr + values[i] : valueStr + values[i] + ",";
            }
            params.put(key, valueStr);
        }
        // 应用验签
        if (!appId.equals(params.get("app_id")))
            log.error("receive wrong app notify from alipay");

        // 将支付成功的订单插入数据库中保存
        Order orderRecord = orderRecordMapper.selectOne(new QueryWrapper<Order>()
                .eq("order_num", params.get("out_trade_no"))
                .eq("biz_state", BIZ_PAYING));
        if (orderRecord == null) {
            log.warn("biz {} has done in repo_order_record", params.get("out_trade_no"));
            return "success";
        }

        // 获取该订单的所有者对象
        UserVO user = userMapper.selectOne(new QueryWrapper<UserVO>()
                .eq("id", orderRecord.getUserId()));

        // 获取是否已经存在过的订单
        Payment existPayment = paymentMapper.selectOne(new QueryWrapper<Payment>()
                .eq("hold_user_id", user.getId()));
        if (existPayment == null) { // 新增为新的记录
            existPayment = new Payment();
            existPayment.setHoldUserId(orderRecord.getUserId());
            existPayment.setEndingDate(DateHandler.increaseYear(orderRecord.getBuyYear()));
            paymentMapper.insert(existPayment);
        } else { // 续签原有的记录
            // 这里不区分是否过期的原因是在下单的时候已经被 hasOrderRecordInDBForPaying() 方法解构了
            existPayment.setEndingDate(DateHandler.increaseYear(existPayment.getEndingDate(), orderRecord.getBuyYear()));
            paymentMapper.update(existPayment, new UpdateWrapper<Payment>()
                    .eq("hold_user_id", user.getId())
                    .eq("id", existPayment.getId()));
        }

        // 同时更新repo_order_record和redis中的订单状态刷新缓存
        orderRecord.setBizState(BIZ_SUCCESS);
        orderRecordMapper.update(orderRecord, new UpdateWrapper<Order>()
                .eq("user_id", user.getId())
                .eq("order_num", orderRecord.getOrderNum())
                .eq("biz_state", PaymentServiceImpl.BIZ_PAYING));
        redisTemplate.opsForValue().set( // 强制更新redis状态如果存在直接覆盖更新
                RedisAction.order(user.getUsername(), orderRecord.getOrderNum()),
                String.valueOf(BIZ_SUCCESS), // 订单交易成功
                timeout * 2,
                TimeUnit.MILLISECONDS
        );

        return "success";
    }

    /**
     * 用户是否拥有企业账户，如果过期会直接删除并返回false
     *
     * @param username 用户名
     * @return true 用户拥有企业账户 false 用户未拥有企业账户
     */
    @Override
    public boolean hasHoldCompanyAccount(String username) {
        // 该user一定是存在的是受注解@RequestConsistency强制监听的
        Payment payment = getPaymentByUsername(username);
        if (payment == null)
            return false;
        return !canDeletePaymentWhenExpire(payment.getId());
    }

    /**
     * 判断并删除paymentId过期的订单记录
     *
     * @param paymentId 订单ID
     * @return 是否删除成功
     */
    @Override
    public boolean canDeletePaymentWhenExpire(int paymentId) {
        Date today = new Date();
        Payment awaitPayment = paymentMapper.selectOne(new QueryWrapper<Payment>()
                .eq("id", paymentId)
                .select("ending_date"));

        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date expireDate = dateFormat.parse(awaitPayment.getEndingDate());
            if (expireDate.before(today)) { // 已过期直接删除
                paymentMapper.deleteById(paymentId);
                return true;
            }
            return false; // 未过期
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 通过username获取Payment信息
     *
     * @param username 所指向的用户不允许为空
     * @return 查询到的Payment记录
     */
    @Override
    public Payment getPaymentByUsername(@NotNull String username) {
        UserVO user = userService.getUserByUsername(username);
        Assert.notNull(user);
        return relateMapper.findUserWhoHoldCompanyAccount(user.getId());
    }

}
