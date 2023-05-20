package org.sangonomiya.app.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.jsonwebtoken.lang.Assert;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.sangonomiya.app.core.Response;
import org.sangonomiya.app.core.ResponseBounce;
import org.sangonomiya.app.entity.Company;
import org.sangonomiya.app.entity.Permission;
import org.sangonomiya.app.extension.RedisAction;
import org.sangonomiya.app.extension.annotation.AzygoService;
import org.sangonomiya.app.extension.annotation.RequestConsistency;
import org.sangonomiya.app.extension.component.CloudFlareUnit;
import org.sangonomiya.app.extension.component.JwtUnit;
import org.sangonomiya.app.extension.component.SMSUnit;
import org.sangonomiya.app.extension.component.TencentUnit;
import org.sangonomiya.app.mapper.RelateMapper;
import org.sangonomiya.app.mapper.UserMapper;
import org.sangonomiya.app.service.IUserService;
import org.sangonomiya.app.entity.UserVO;
import org.sangonomiya.groovy.DataHandler;
import org.sangonomiya.groovy.DateHandler;
import org.sangonomiya.groovy.VerifyHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nullable;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 用户业务逻辑Bean
 * @author Dioxide.CN
 * @date 2023/2/28 14:28
 * @since 1.0
 */
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, UserVO> implements IUserService {

    @Resource
    private UserMapper userMapper;
    @Resource
    private RelateMapper relateMapper;
    @Resource
    private UserDetailsService userDetailsService;
    @Resource
    private PasswordEncoder passwordEncoder;
    @Resource
    private RedisTemplate<String, String> redisTemplate;

    @Resource
    private CloudFlareUnit cloudFlareUnit;

    @Resource
    private SMSUnit smsUnit;
    @Resource
    private TencentUnit tencentUnit;
    @Resource
    private JwtUnit jwtUnit;
    @Value("${jwt.tokenHead}")
    private String tokenHead;

    /* 基本账号信息操作业务 */

    /**
     * 用户使用账号登录并返回一个有效的token
     * @param username 登录用户名
     * @param password 登录密码
     * @param request http请求
     * @return 返回结果集封装体
     */
    @Override
    public ResponseBounce<Object> loginWithUsernameAction(String username, String password, HttpServletRequest request) {
        if (!VerifyHandler.of().username(username))
            return Response.fail("用户名格式错误");
        UserVO awaitUser = getUserByUsername(username);
        if (awaitUser == null || !awaitUser.isEnable())
            return Response.fail("用户不存在或已被禁用");

        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        return normalLoginAction(userDetails, password);
    }

    /**
     * 用户使用邮箱登录并返回一个有效的token
     * @param email 用户邮箱
     * @param password 登录密码
     * @param request http请求
     * @return 返回结果集封装体
     */
    @Override
    public ResponseBounce<Object> loginWithEmailAction(String email, String password, HttpServletRequest request) {
        if (!VerifyHandler.of().email(email))
            return Response.fail("邮箱格式错误");
        UserVO awaitUser = getUserByEmail(email);
        if (awaitUser == null || !awaitUser.isEnable())
            return Response.fail("用户不存在或已被禁用");

        UserDetails userDetails = userDetailsService.loadUserByUsername(awaitUser.getUsername());
        return normalLoginAction(userDetails, password);
    }

    /**
     * 用户使用手机和验证码登录并返回一个有效的token
     * @param phone 用户联系方式
     * @param code 手机验证码
     * @param request http请求
     * @return 返回结果集封装体
     */
    @Override
    public ResponseBounce<Object> loginWithPhoneAction(String phone, String code, HttpServletRequest request) {
        if (!VerifyHandler.of().phone(phone))
            return Response.fail("手机号格式错误");

        UserVO awaitUser = getUserByPhone(phone);
        if (isInvalidCode(RedisAction.phone(phone, RedisAction.LOGIN), code))
            return Response.fail("验证码错误");
        if (awaitUser == null || !awaitUser.isEnable())
            return Response.fail("用户不存在或已被禁用");

        UserDetails userDetails = userDetailsService.loadUserByUsername(awaitUser.getUsername());
        return phoneLoginAction(userDetails, phone);
    }

    /**
     * 用户注册并返回注册成功的用户数据
     * @param username 注册用户名
     * @param password 注册密码
     * @param email 注册邮箱非必须
     * @param phone 注册手机号
     * @param request http请求
     * @return 返回结果集封装体
     */
    @Override
    @AzygoService(keyPos = {0})
    public ResponseBounce<Object> registerAction(String username, String password, String email, String phone, String code, HttpServletRequest request) {
        if(StringUtils.isBlank(username) || StringUtils.isBlank(password)) return Response.fail("参数不能为空");

        // 用户名和手机号在数据库中是否存在
        if (getUserByUsername(username) != null
                || getUserByPhone(phone) != null)
            return Response.fail("用户已存在");

        //判断密码强度
        if(!VerifyHandler.of().password(password)) return Response.fail("密码不符合规范");

        // 短信验证
        if (isInvalidCode(RedisAction.phone(phone, RedisAction.REGISTER), code))
            return Response.fail("验证码错误");
        redisTemplate.delete(RedisAction.phone(phone, RedisAction.REGISTER));

        UserVO awaitUser = new UserVO();
        awaitUser.setUsername(username);
        awaitUser.setEmail(email);
        awaitUser.setPassword(passwordEncoder.encode(password));
        awaitUser.setCreateTime(DateHandler.getDateString());
        awaitUser.setPhone(phone);
        awaitUser.setEnable(true);

        userMapper.insert(awaitUser); // 插入用户数据
        return Response.success();
    }

    /**
     * 用户修改联系方式
     * @param username 用户名
     * @param newPhone 新手机号
     * @param code 验证码
     * @return 是否修改成功的结果
     */
    @Override
    @RequestConsistency
    public ResponseBounce<Object> changePhoneAction(String username, String password, String newPhone, String code) {
        UserVO user = getUserByUsername(username);
        if (!safeMatchPassword(password, user.getPassword()))
            return Response.fail("密码错误");

        if (user.getPhone().equals(newPhone)) return Response.fail("新旧手机号未改变");
        if (isInvalidCode(RedisAction.phone(newPhone, RedisAction.CHANGING), code)) return Response.fail("验证码错误");

        redisTemplate.delete(RedisAction.phone(newPhone, RedisAction.CHANGING));
        user.setPhone(newPhone);
        this.updateById(user);
        return Response.success();
    }

    /**
     * 用户修改个人信息
     * @param username 用户名
     * @param city 所在城市
     * @param gender 性别
     * @param realname 姓名
     * @param portrait 头像id
     * @return 返回修改是否成功的结果封装体
     */
    @Override
    @RequestConsistency
    public ResponseBounce<Object> changeProfileAction(String username, String city, String gender, String realname, Integer portrait) {
        UserVO user = getUserByUsername(username);
        if (DataHandler.isAvailable(username, city, gender, realname) && portrait == null)
            return Response.fail("用户资料错误");

        // 当有字段不为空时允许修改该字段
        if (DataHandler.isAvailable(city)) user.setCity(city);
        if (DataHandler.isAvailable(gender)) user.setGender(gender);
        if (DataHandler.isAvailable(realname)) user.setRealname(realname);
        if (portrait != null) user.setPortrait(portrait);
        userMapper.updateById(user);
        return Response.success();
    }

    /**
     * 用户修改密码操作，该操作需要用户登录后进入个人信息主页才能修改
     * redis-server "/Users/dioxide/Environment/redis-7.0.5/redis.conf"
     *
     * @param username 操作用户
     * @param oldPassword 旧密码
     * @param newPassword 新密码
     * @return 返回是否修改成功的结果封装体
     */
    @Override
    @RequestConsistency
    @AzygoService(keyPos = {0})
    public ResponseBounce<Object> changePasswordByLoginUserAction(String username, String oldPassword, String newPassword) {
        // 这里用户已经存在了所以直接断言
        UserVO user = getUserByUsername(username);
        Assert.notNull(user);

        // 旧密码与新密码相同则直接中断业务
        if (oldPassword.equals(newPassword))
            return Response.fail("旧密码与新密码不能相同");
        //判断密码强度
        if(!VerifyHandler.of().password(newPassword))
            return Response.fail("密码不符合规范");
        if (!passwordEncoder.matches(oldPassword, user.getPassword()))
            return Response.fail("密码错误");

        user.setPassword(passwordEncoder.encode(newPassword));
        userMapper.updateById(user);
        return Response.success();
    }

    /**
     * 用户找回密码操作
     *
     * @param username 用户名
     * @param newPassword 新密码
     * @param phone 手机号
     * @param code 手机验证码
     * @return 是否修改成功的结果封装体
     */
    @Override
    @AzygoService(keyPos = {0})
    public ResponseBounce<Object> findBackPasswordAction(String username, String newPassword, String phone, String code) {
        if (isInvalidCode(RedisAction.phone(phone, RedisAction.FIND_PASSWORD), code))
            return Response.fail("验证码错误");

        UserVO user = userMapper.selectOne(new QueryWrapper<UserVO>()
                .eq("username", username)
                .eq("phone", phone));

        if(user == null) return Response.fail("用户不存在");

        //判断密码强度
        if(!VerifyHandler.of().password(newPassword)) return Response.fail("密码不符合规范");

        //删除redis
        redisTemplate.delete(RedisAction.phone(phone, RedisAction.FIND_PASSWORD));

        user.setPassword(passwordEncoder.encode(newPassword));
        userMapper.updateById(user);
        return Response.success();
    }

    /**
     * 当已登录的用户发起对类似于修改手机号、修改企业信息等高危操作时需要调用此接口获取短信验证码
     *
     * @param username 用户名
     * @param phone 手机号（不一定是用户也不一定需要提供如不提供使用用户自己的）
     * @param action 操作类型
     * @param ticket 腾讯云验证码回调后的票据
     * @param randstr 腾讯云验证码回调后的随机字符串
     * @return 是否发送成功的结果封装体
     */
    @Override
    @RequestConsistency
    @AzygoService(keyPos = {0})
    public ResponseBounce<Object> eventCodeSendBeforeUserCheckAction(String username, String phone, String action, String ticket, String randstr) {
        UserVO user = getUserByUsername(username);

        // 非法请求
        Long tencentCode = tencentUnit.TextureCaptcha(ticket, randstr);

        if (tencentCode != 1L) // 当且仅当人机验证结果为1L时放行
            return Response.fail("非法请求");
        return defaultEventCodeSendAction(
                DataHandler.isAvailable(phone) ? phone : user.getPhone(),
                action);
    }

    /**
     * 当已登录的用户发起对类似于修改手机号、修改企业信息等高危操作时需要调用此接口获取短信验证码
     * CloudFlare重写版
     *
     * @param username 用户名
     * @param phone 手机号（不一定是用户也不一定需要提供如不提供使用用户自己的）
     * @param action 操作类型
     * @param token CloudFlare返回的token
     * @return 是否发送成功的结果封装体
     */
    @Override
    @RequestConsistency
    @AzygoService(keyPos = {0})
    public ResponseBounce<Object> eventCFCodeSendBeforeUserCheckAction(String username, String phone, String action, String token) {
        UserVO user = getUserByUsername(username);

        if (!cloudFlareUnit.siteVerify(token))
            return Response.fail("非法请求,人机验证不通过");
        return defaultEventCodeSendAction(
                DataHandler.isAvailable(phone) ? phone : user.getPhone(),
                action);
    }

    /**
     * 直接发送手机验证码
     *
     * @param phone 手机号
     * @param action 获取验证码的操作类型
     * @param ticket 腾讯云验证码回调后的票据
     * @param randstr 腾讯云验证码回调后的随机字符串
     * @return 返回结果集封装体
     */
    @Override
    public ResponseBounce<Object> eventCodeSendAction(String username, String phone, String action, String ticket, String randstr) {

        // 手机号归属认证
        if (action.equals(RedisAction.FIND_PASSWORD)) {
            if (!DataHandler.isAvailable(username))
                return Response.fail("用户名不能为空");

            UserVO user = getUserByUsername(username);
            if (user == null)
                return Response.fail("用户不存在");
            if (!user.getPhone().equals(phone)) {
                return Response.fail("手机号错误");
            }
        }

        // 非法请求
        Long tencentCode = tencentUnit.JigsawCaptcha(ticket, randstr);
        if (tencentCode != 1L) // 当且仅当人机验证结果为1L时放行
            return Response.fail("非法请求");

        return defaultEventCodeSendAction(phone, action);
    }

    /**
     * CloudFlare发送验证码请求
     * @param phone 手机号
     * @param action 获取验证码的操作类型
     * @param token CloudFlare返回的Token
     * @return 返回结果集封装体
     */
    @Override
    public ResponseBounce<Object> eventCFCodeSendAction(String username, String phone, String action, String token) {

        // 手机号归属认证
        if (action.equals(RedisAction.FIND_PASSWORD)) {
            if (!DataHandler.isAvailable(username))
                return Response.fail("用户名不能为空");

            UserVO user = getUserByUsername(username);
            if (user == null)
                return Response.fail("用户不存在");
            if (!user.getPhone().equals(phone)) {
                return Response.fail("手机号错误");
            }
        }

        if (!cloudFlareUnit.siteVerify(token)) // 当且仅当人机验证结果为1L时放行
            return Response.fail("非法请求，人机验证不通过");

        return defaultEventCodeSendAction(phone, action);
    }

    @Override
    public ResponseBounce<Object> defaultEventCodeSendAction(String phone, String action) {
        // 校验手机号的合法性
        if (!VerifyHandler.of().phone(phone))
            return Response.fail("手机格式错误");

        // 从Redis中查询验证码是否已过期未过期才能发送
        if (redisTemplate.opsForValue().get(RedisAction.phone(phone, action)) != null)
            return Response.fail("请勿重复发送验证码");

        // 调用SMSUtil进行验证码发送
        String code = smsUnit.sendCode(phone);

        // Redis进行缓存数据
        Boolean var1 = redisTemplate.opsForValue()
                .setIfAbsent(RedisAction.phone(phone, action), code, 2, TimeUnit.MINUTES);
        if (Boolean.FALSE.equals(var1)) {
            log.error("error occurred when insert data to redis {}", phone);
            return Response.fail("未知错误");
        }

        return Response.success("验证码已发送");
    }

    /* 与权限组Permission相关的业务 */

    /**
     * 获取用户所属的权限组
     * @param username 用户名
     * @return 返回用户所属的权限组结果集
     */
    @Override
    @Nullable
    public Permission getUserPermissionGroup(String username) {
        UserVO user = getUserByUsername(username);

        //判断用户是否为空
        if(user == null) return null;
        return relateMapper.getUserPermissionGroup(user.getId());
    }

    @Override
    @Nullable
    public Permission getUserPermissionGroup(UserVO user) {
        //判断用户是否为空
        if(user == null) return null;
        return relateMapper.getUserPermissionGroup(user.getId());
    }

    /**
     * 根据用户名获取其权限组所属的公司，该业务支持Spring事务回滚。
     * 所以不推荐先"获取用户权限组"再"通过权限组获取公司"的分步操作。
     * @param username 用户名
     * @return 返回用户所在权限组的所属公司对象结果集
     */
    @Override
    @Transactional(isolation=Isolation.READ_COMMITTED)
    public ResponseBounce<Object> getUserPermissionGroupFromWhichCompany(String username) {
        Permission permission = getUserPermissionGroup(username);
        if (permission == null)
            return Response.fail("用户未分配权限组");
        Company company = relateMapper.getPermissionFromWhichCompany(permission.getId());
        return Response.success(company);
    }

    /* 根据一些字段获取用户对象的相关方法 */

    /**
     * Mybatis-plus依据username在库中查询数据不对外发布
     * @param username 指定的查询的username
     * @return 返回一个对应username的UserVO对象
     */
    @Override
    public UserVO getUserByUsername(String username) {
        return userMapper.selectOne(new QueryWrapper<UserVO>().eq("username", username));
    }

    /**
     * Mybatis-plus依据phone在库中查询数据不对外发布
     * @param phone 指定的查询的phone
     * @return 返回一个对应phone的UserVO对象
     */
    @Override
    public UserVO getUserByPhone(String phone) {
        return userMapper.selectOne(new QueryWrapper<UserVO>().eq("phone", phone));
    }

    /**
     * Mybatis-plus依据email在库中查询数据不对外发布
     * @param email 指定的查询的email
     * @return 返回一个对应email的UserVO对象
     */
    @Override
    public UserVO getUserByEmail(String email) {
        return userMapper.selectOne(new QueryWrapper<UserVO>().eq("email", email));
    }

    /**
     * Mybatis-plus依据user id在库中查询数据对外发布
     * @param id 指定的查询的user id
     * @return 返回一个对应email的UserVO对象
     */
    @Override
    public UserVO getUserById(int id) {
        return userMapper.selectOne(new QueryWrapper<UserVO>()
                .eq("id", id)
                .select("username"));
    }

    /**
     * 从redis中校验传递的验证码是否无效
     * @param phoneAction 手机号
     * @param code 收到的验证码
     * @return true 无效的验证码 false 有效的验证码
     */
    @Override
    public boolean isInvalidCode(String phoneAction, @NotNull String code) {
        if (code.trim().length() != 6) return true;
        try {
            Long.parseLong(code);
            String correctCode = redisTemplate.opsForValue().get(phoneAction);
            // 验证码过期或两者不相等属于无效的验证码
            return !code.equals(correctCode);
        } catch (NumberFormatException var1) {
            return true;
        }
    }

    /**
     * 安全校验密码的正确性
     * @param rawPassword 明文密码
     * @param encodedPassword 匹配目标密文
     * @return true 密码正确 false 密码错误
     */
    @SuppressWarnings("all")
    private boolean safeMatchPassword(@NotNull String rawPassword, @NotNull String encodedPassword) {
        if (!VerifyHandler.of().password(rawPassword))
            return false;
        if (rawPassword == null || encodedPassword == null)
            return false;

        return passwordEncoder.matches(rawPassword, encodedPassword);
    }

    /**
     * 用户名或邮箱登录
     * @param userDetails userDetails对象
     * @param password 密码
     * @return 响应结果
     */
    private ResponseBounce<Object> normalLoginAction(UserDetails userDetails, String password) {
        // 用户名或密码错误
        if (!safeMatchPassword(password, userDetails.getPassword()))
            return Response.fail("用户名或密码错误");

        return defaultLoginAction(userDetails);
    }

    /**
     * 手机验证码登录
     * @param userDetails userDetails对象
     * @param phone 登录的手机号
     * @return 响应结果
     */
    private ResponseBounce<Object> phoneLoginAction(UserDetails userDetails, String phone) {
        redisTemplate.delete(RedisAction.phone(phone, RedisAction.LOGIN));
        return defaultLoginAction(userDetails);
    }

    /**
     * 通用登录操作
     * @param userDetails userDetails对象
     * @return 响应结果
     */
    private ResponseBounce<Object> defaultLoginAction(UserDetails userDetails) {
        // 更新security登录用户对象
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);

        String token = jwtUnit.generateToken(userDetails);
        Map<String, Object> tokenMap = new HashMap<>();
        tokenMap.put("token", token);
        tokenMap.put("tokenHead", tokenHead);

        UserVO user = getUserByUsername(userDetails.getUsername());
        user.setPassword(null); // 隐式返回
        user.setId(-1); // 隐式
        tokenMap.put("userDetail", user);
        return Response.success(tokenMap);
    }

}
