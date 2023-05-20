//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package org.sangonomiya.app.service.impl;

import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.jsonwebtoken.lang.Assert;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;
import javax.annotation.Nullable;
import javax.annotation.Resource;

import org.jetbrains.annotations.NotNull;
import org.sangonomiya.app.core.Response;
import org.sangonomiya.app.core.ResponseBounce;
import org.sangonomiya.app.entity.Company;
import org.sangonomiya.app.entity.RSAObject;
import org.sangonomiya.app.entity.UserVO;
import org.sangonomiya.app.extension.RedisAction;
import org.sangonomiya.app.extension.annotation.AzygoService;
import org.sangonomiya.app.extension.annotation.CompanyOperation;
import org.sangonomiya.app.extension.annotation.RequestConsistency;
import org.sangonomiya.app.extension.component.CloudFlareUnit;
import org.sangonomiya.app.extension.component.TencentUnit;
import org.sangonomiya.app.mapper.GenerateMapper;
import org.sangonomiya.app.mapper.ProductMapper;
import org.sangonomiya.app.mapper.RSAMapper;
import org.sangonomiya.app.service.ICollectorService;
import org.sangonomiya.app.service.ICompanyService;
import org.sangonomiya.app.service.IUserService;
import org.sangonomiya.groovy.*;
import org.sangonomiya.kotlin.Pair;
import org.sangonomiya.kotlin.XSS;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CollectorServiceImpl extends ServiceImpl<RSAMapper, RSAObject> implements ICollectorService {
    @Resource
    private ICompanyService companyService;
    @Resource
    private IUserService userService;
    @Resource
    private ProductMapper productMapper;
    @Resource
    private GenerateMapper generateMapper;

    @Resource
    private CloudFlareUnit cloudFlareUnit;

    @Resource
    @SuppressWarnings("all")
    private TencentUnit tencentUnit;

    @Value("${alipay.retry}")
    private int maximumRetry;

    /**
     * 发送腾讯验证码
     * @param username 用户名
     * @param companyName 企业名称
     * @param ticket 票据
     * @param randstr 随机字符串
     * @return 返回是否发送验证码成功
     */
    @Override
    @RequestConsistency
    @CompanyOperation(operator = false)
    @AzygoService(keyPos = {0})
    public ResponseBounce<Object> sendCreateCode(String username, String companyName, String ticket, String randstr) {
        UserVO user = userService.getUserByUsername(username);
        Assert.notNull(user);
        String phone = user.getPhone();

        // 非法请求
        Long tencentCode = tencentUnit.TextureCaptcha(ticket, randstr);

        if (tencentCode != 1L) // 当且仅当人机验证结果为1L时放行
            return Response.fail("非法请求");

        return userService.defaultEventCodeSendAction(
                phone, RedisAction.RSA_GENERATE);
    }

    /**
     * 发送CloudFlare验证码
     * @param username 用户名
     * @param companyName 企业名称
     * @param token token
     * @return 返回是否发送验证码成功
     */
    @Override
    @RequestConsistency
    @CompanyOperation(operator = false)
    @AzygoService(keyPos = {0})
    public ResponseBounce<Object> sendCFCreateCode(String username, String companyName, String token) {
        UserVO user = userService.getUserByUsername(username);
        Assert.notNull(user);
        String phone = user.getPhone();

        // 非法请求
        if (!cloudFlareUnit.siteVerify(token))
            return Response.fail("非法请求");

        return userService.defaultEventCodeSendAction(
                phone, RedisAction.RSA_GENERATE);
    }

    /**
     * 获取企业所有应用信息
     * @param username 用户名
     * @param companyName 企业名称
     * @return 返回企业名下所有创建的应用数组
     */
    @Override
    @RequestConsistency
    @CompanyOperation(operator = false)
    public ResponseBounce<Object> getAllApplication(String username, String companyName) {
        UserVO administrator = this.userService.getUserByUsername(username);
        Assert.notNull(administrator);
        Company company = this.companyService.getCompanyByName(companyName);
        Assert.notNull(company);
        List<RSAObject> applicationList =
                this.baseMapper.selectList(
                        new QueryWrapper<RSAObject>()
                                .eq("company_id", company.getId()));

        for (RSAObject application : applicationList) {
            application
                    .setCompanyId(null)
                    .setAppPublicKey(null)
                    .setComPrivateKey(null)
                    .setComPublicKey(null);
        }
        return Response.success(applicationList);
    }

    /**
     * 具体获取某个应用
     * @param username 用户名
     * @param companyName 企业名称
     * @param appId 应用ID
     * @return 返回应用ID指向的应用对象
     */
    @Override
    @RequestConsistency
    @CompanyOperation(operator = false)
    public ResponseBounce<Object> getSpecificApplication(String username, String companyName, String appId) {
        UserVO administrator = this.userService.getUserByUsername(username);
        Assert.notNull(administrator);
        Company company = this.companyService.getCompanyByName(companyName);
        Assert.notNull(company);

        RSAObject application = this.baseMapper.selectOne(new QueryWrapper<RSAObject>()
                .eq("company_id", company.getId())
                .eq("app_id", appId));
        if (application == null)
            return Response.fail("应用不存在");
        application.setComPrivateKey(null); // 只返回公钥给前端让开发者自行加密
        return Response.success(application);
    }

    /**
     * 应用操作需要的手机验证
     * @param username 用户名
     * @param companyName 企业名称
     * @param code 验证码
     * @return 返回是否验证成功
     */
    @Override
    @RequestConsistency
    @CompanyOperation(operator = false)
    public ResponseBounce<Object> verifyAccountAction(String username, String companyName, String code) {
        UserVO administrator = this.userService.getUserByUsername(username);
        Assert.notNull(administrator);
        Company company = this.companyService.getCompanyByName(companyName);
        Assert.notNull(company);

        if (this.userService.isInvalidCode(RedisAction.phone(administrator.getPhone(), RedisAction.RSA_GENERATE), code)) {
            return Response.fail("验证码错误");
        }
        return Response.success();
    }

    /**
     * 创建应用
     * @param username 用户名
     * @param companyName 企业名称
     * @param title 应用名称
     * @param imgId 应用类型
     * @param appPublicKey 应用公钥（不允许有值）
     * @param remark 应用备注
     * @param notifyUrl 回调地址（不允许有值）
     * @param code 验证码
     * @return 返回是否创建应用成功
     */
    @Override
    @RequestConsistency
    @CompanyOperation(operator = false)
    @AzygoService(keyPos = {1})
    public ResponseBounce<Object> generateRSAPair(String username, String companyName, String title, Integer imgId, String appPublicKey, String remark, String notifyUrl, String code) {
        UserVO administrator = this.userService.getUserByUsername(username);
        Assert.notNull(administrator);
        Company company = this.companyService.getCompanyByName(companyName);
        Assert.notNull(company);

        if (!VerifyHandler.of().company(title)) {
            return Response.fail("非法的应用名称");
        } else if (DataHandler.isAvailable(appPublicKey) || DataHandler.isAvailable(notifyUrl)) {
            return Response.fail("不安全的数据类型");
        } else {
            int count =
                    this.baseMapper.selectCount(
                            new QueryWrapper<RSAObject>()
                                    .eq("company_id", company.getId())).intValue();
            if (count >= 10) {
                return Response.fail("最多只能创建10个应用");
            } else {
                remark = XSS.Companion.filter(remark);

                if (this.userService.isInvalidCode(RedisAction.phone(administrator.getPhone(), RedisAction.RSA_GENERATE), code)) {
                    return Response.fail("验证码错误");
                } else {
                    final String appId = this.generateAppId(count);
                    if (appId == null) {
                        return Response.fail("应用创建失败请重试");
                    } else {
                        this.baseMapper.insert(
                                new RSAObject(
                                        company.getId(),
                                        title,
                                        imgId,
                                        appId,
                                        appPublicKey,
                                        "",
                                        "",
                                        remark,
                                        notifyUrl,
                                        false,
                                        DateHandler.getDateString(),
                                        0,
                                        null
                                )
                        );
                        return Response.success(new HashMap<String, String>() {
                            {
                                this.put("appId", appId);
                            }
                        });
                    }
                }
            }

        }
    }

    /**
     * 设置RSA密钥对
     * @param username 用户名
     * @param companyName 企业名称
     * @param appId 应用ID
     * @param appPublicKey 应用公钥
     * @return 返回是否RSA秘钥设置成功
     */
    @Override
    @RequestConsistency
    @CompanyOperation(operator = false)
    @AzygoService(keyPos = {1, 2})
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public ResponseBounce<Object> doGenerateRSAPair(String username, String companyName, String appId, String appPublicKey) {
        if (!(DataHandler.isAvailable(appPublicKey) && RSAHandler.verify(appPublicKey)))
            return Response.fail("不安全的数据类型");

        RSAObject newApplication = new RSAObject();
        final Pair<String, String> keyPair = RSAHandler.genKeyPair();
        newApplication
                .setAppPublicKey(appPublicKey)
                .setComPublicKey(keyPair.left())
                .setComPrivateKey(keyPair.right());
        if (doUpdateApplicationAction(companyName, appId, newApplication)) {
            return Response.success();
        } else {
            return Response.fail("应用不存在");
        }
    }

    /**
     * 更改应用基本信息
     * @param username 用户名
     * @param companyName 企业名称
     * @param appId 应用ID
     * @param title 应用标题
     * @param remark 应用简介
     * @return 返回基本信息是否设置成功
     */
    @Override
    @RequestConsistency
    @CompanyOperation(operator = false)
    @AzygoService(keyPos = {1, 2})
    @SuppressWarnings("all")
    public ResponseBounce<Object> modifyApplicationDefaultInformation(String username, String companyName, String appId, @Nullable String title, @Nullable String remark) {
        if (!DataHandler.isAvailable(title) && !DataHandler.isAvailable(remark))
            return Response.fail("非法的数据源");
        if (DataHandler.isAvailable(title))
            title = XSS.Companion.filter(title);
        if (DataHandler.isAvailable(remark))
            remark = XSS.Companion.filter(remark);
        RSAObject newApplication = new RSAObject();
        newApplication.setTitle(title).setRemark(remark);
        if (doUpdateApplicationAction(companyName, appId, newApplication)) {
            return Response.success();
        } else {
            return Response.fail("应用不存在");
        }
    }

    /**
     * 设置应用网关（回调地址）
     * @param username 用户名
     * @param companyName 企业名称
     * @param appId 应用ID
     * @param notifyUrl 回调地址URL
     * @return 返回应用网关是否设置成功
     */
    @Override
    @RequestConsistency
    @CompanyOperation(operator = false)
    @AzygoService(keyPos = {1, 2})
    public ResponseBounce<Object> doSetNotifyUrl(String username, String companyName, String appId, String notifyUrl) {
        RSAObject newApplication = new RSAObject();

        if (DataHandler.isAvailable(notifyUrl) && !VerifyHandler.of().url(notifyUrl)) {
            return Response.fail("应用回调地址错误");
        } else if (notifyUrl.isBlank() || notifyUrl.isEmpty()) {
            newApplication.setNotifyUrl("");
        }else {
            newApplication.setNotifyUrl(notifyUrl);
        }

        if (doUpdateApplicationAction(companyName, appId, newApplication)) {
            return Response.success();
        } else {
            return Response.fail("应用不存在");
        }
    }

    /**
     * 通用应用信息更新方法
     * @param companyName 企业名称
     * @param appId 应用ID
     * @param newApplication 包含新的信息的新应用对象
     * @return 返回是否update成功
     */
    private boolean doUpdateApplicationAction(@NotNull final String companyName, @NotNull final String appId, @NotNull final RSAObject newApplication) {
        Company company = this.companyService.getCompanyByName(companyName);
        Assert.notNull(company);

        RSAObject application = this.baseMapper.selectOne(new QueryWrapper<RSAObject>()
                .eq("company_id", company.getId())
                .eq("app_id", appId));
        if (application == null)
            return false;

        if (newApplication.getAppPublicKey() != null)
            application.setAppPublicKey(newApplication.getAppPublicKey());
        if (newApplication.getComPublicKey() != null)
            application.setComPublicKey(newApplication.getComPublicKey());
        if (newApplication.getComPrivateKey() != null)
            application.setComPrivateKey(newApplication.getComPrivateKey());
        if (newApplication.getRemark() != null)
            application.setRemark(newApplication.getRemark());
        if (newApplication.getNotifyUrl() != null)
            application.setNotifyUrl(newApplication.getNotifyUrl());
        if (newApplication.getTitle() != null)
            application.setTitle(newApplication.getTitle());
        if (newApplication.isEnable())
            newApplication.setEnable(true);

        this.baseMapper.update(application, new UpdateWrapper<RSAObject>()
                .eq("company_id", application.getCompanyId())
                .eq("app_id", application.getAppId()));
        return true;
    }

    /**
     * 生成应用ID
     * @param count 企业已有应用数量
     * @return 返回生成的应用ID
     */
    @Nullable
    private String generateAppId(int count) {
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
        String time = format.format(new Date());
        String index = count < 9 ? "0" + (count + 1) : "10";
        String random = RandomHandler.randomInt(1000, 10000).toString();
        String appId = time + index + random;
        int retryCount = -1;

        while(retryCount != this.maximumRetry) {
            random = RandomHandler.randomInt(1000, 10000).toString();
            appId = time + index + random;
            ++retryCount;
            if (this.baseMapper.selectOne(
                    new QueryWrapper<RSAObject>()
                            .eq("app_id", appId)) == null) {
                break;
            }
        }

        return (this.baseMapper.selectOne(
                new QueryWrapper<RSAObject>()
                        .eq("app_id", appId)) != null ? null : appId);
    }

    /**
     * 删除应用
     * @param username 用户名
     * @param companyName 企业名称
     * @param appId 应用ID
     * @return 返回应用是否删除成功
     */
    @Override
    @RequestConsistency
    @CompanyOperation(operator = false)
    @AzygoService(keyPos = {1, 2})
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public ResponseBounce<Object> deleteApplication(String username, String companyName, String appId) {
        Company company = this.companyService.getCompanyByName(companyName);
        Assert.notNull(company);

        RSAObject application = this.baseMapper.selectOne(
                new QueryWrapper<RSAObject>()
                        .eq("company_id", company.getId())
                        .eq("app_id", appId));
        if (application == null)
            return Response.fail("应用不存在");

        this.baseMapper.delete(new QueryWrapper<RSAObject>()
                        .eq("company_id", company.getId())
                        .eq("app_id", appId));
        return Response.success();
    }

    /**
     * 设置应用绑定的产品
     * @param username 用户名
     * @param companyName 企业名称
     * @param appId 应用ID
     * @param productId 产品ID
     * @return 返回是否绑定产品成功
     */
    @Override
    @RequestConsistency
    @CompanyOperation(operator = false)
    @AzygoService(keyPos = {1, 2})
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public ResponseBounce<Object> setBindingProduct(String username, String companyName, String appId, String productId) {
        if (!DataHandler.isAvailable(productId))
            return Response.fail("产品错误");

        Company company = this.companyService.getCompanyByName(companyName);
        Assert.notNull(company);
        if (productMapper.selectProductById(company.getId().toString(), Integer.parseInt(productId)) == null)
            return Response.fail("产品不存在");

        RSAObject application = this.baseMapper.selectOne(
                new QueryWrapper<RSAObject>()
                        .eq("company_id", company.getId())
                        .eq("app_id", appId));
        if (application == null)
            return Response.fail("应用不存在");
        application.setBindingProductId(productId);

        this.baseMapper.update(application, new UpdateWrapper<RSAObject>()
                .eq("company_id", application.getCompanyId())
                .eq("app_id", application.getAppId()));
        return Response.success();
    }

    /**
     * 启用/关闭碳排放计算能力
     * @param username 用户名
     * @param companyName 企业名称
     * @param appId 应用ID
     * @param isEnable 是否启用
     * @return 返回启用/禁用成功
     */
    @Override
    @RequestConsistency
    @CompanyOperation(operator = false)
    @AzygoService(keyPos = {1, 2})
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public ResponseBounce<Object> enableAbility(String username, String companyName, String appId, boolean isEnable) {
        Company company = this.companyService.getCompanyByName(companyName);
        Assert.notNull(company);

        RSAObject application = this.baseMapper.selectOne(
                new QueryWrapper<RSAObject>()
                        .eq("company_id", company.getId())
                        .eq("app_id", appId));
        if (application == null)
            return Response.fail("应用不存在");
        application.setAbilityId(isEnable ? 1 : 0);

        this.baseMapper.update(application, new UpdateWrapper<RSAObject>()
                .eq("company_id", application.getCompanyId())
                .eq("app_id", application.getAppId()));
        return Response.success();
    }

    /**
     * 发布/上线应用
     * @param username 用户名
     * @param companyName 企业名称
     * @param appId 应用ID
     * @return 返回是否发布成功
     */
    @Override
    @RequestConsistency
    @CompanyOperation(operator = false)
    @AzygoService(keyPos = {1, 2})
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public ResponseBounce<Object> publishApplication(String username, String companyName, String appId) {
        // 发布/上线应用
        Company company = this.companyService.getCompanyByName(companyName);
        Assert.notNull(company);

        RSAObject application = this.baseMapper.selectOne(
                new QueryWrapper<RSAObject>()
                        .eq("company_id", company.getId())
                        .eq("app_id", appId));
        if (application == null)
            return Response.fail("应用不存在");

        if (!DataHandler.isAvailable(application.getAppPublicKey(), application.getComPublicKey(), application.getComPrivateKey()))
            return Response.fail("应用需要设置接口加签方式后才能上线");

        if (application.isEnable())
            return Response.fail("应用已处于上线状态");

        application.setEnable(true);
        this.baseMapper.update(application, new UpdateWrapper<RSAObject>()
                .eq("company_id", application.getCompanyId())
                .eq("app_id", application.getAppId()));
        if (application.getBindingProductId() == null)
            return Response.info(201, "应用上线成功但需要设置绑定产品才能正常使用");
        else
            return Response.info(200, "应用上线成功");
    }

    /**
     * 下线应用
     * @param username 用户名
     * @param companyName 企业名称
     * @param appId 应用ID
     * @return 返回是否下线成功
     */
    @Override
    @RequestConsistency
    @CompanyOperation(operator = false)
    @AzygoService(keyPos = {1, 2})
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public ResponseBounce<Object> archiveApplication(String username, String companyName, String appId) {
        // 下线应用
        Company company = this.companyService.getCompanyByName(companyName);
        Assert.notNull(company);

        RSAObject application = this.baseMapper.selectOne(
                new QueryWrapper<RSAObject>()
                        .eq("company_id", company.getId())
                        .eq("app_id", appId));
        if (application == null)
            return Response.fail("应用不存在");

        if (!application.isEnable())
            return Response.fail("应用已处于下线状态");

        application.setEnable(false);
        this.baseMapper.update(application, new UpdateWrapper<RSAObject>()
                .eq("company_id", application.getCompanyId())
                .eq("app_id", application.getAppId()));
        return Response.info(200, "应用下线成功");
    }

    @Override
    @AzygoService(keyPos = {0})
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public ResponseBounce<Object> applicationPortal(String appId, String encryptData) {
        RSAObject application = this.baseMapper.selectOne(new QueryWrapper<RSAObject>()
                .eq("app_id", appId));
        if (application == null) // 应用不存在
            return Response.info(400, "APP_NOT_EXIST");
        if (application.getBindingProductId() == null)
            return Response.info(400, "NO_PRODUCT_FOUNT");

        // 使用企业私钥进行解密
        String decrypt;
        try {
            decrypt = RSAHandler.decrypt(encryptData, application.getComPrivateKey());
        } catch (Exception ignored) {
            return Response.info(400, "NOT_OWNER");
        }
        if (decrypt == null) // 未知错误
            return Response.info(400, "UNEXPECTED_ERROR");

        JSONObject data;
        try {
            data = JSONObject.parseObject(decrypt);
        } catch (Exception ignored) {
            // 错误的data数据格式
            return Response.info(400, "ILLEGAL_DATA_FORMAT");
        }
        if (data == null) // 未知错误
            return Response.info(400, "UNEXPECTED_ERROR");

        // 解析data中的unit（单位量/每次调用计量增加多少）
        BigDecimal unit = new BigDecimal("1.0");
        if (data.get("unit") != null)
            unit = (BigDecimal) data.get("unit");
        if (unit.doubleValue() == 0.0)
            return Response.info(400, "ILLEGAL_UNIT");

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat yearFmt = new SimpleDateFormat("yyyy");
        SimpleDateFormat monthFmt = new SimpleDateFormat("MM");
        SimpleDateFormat dayFmt = new SimpleDateFormat("dd");

        String year = yearFmt.format(calendar.getTime());
        String month = monthFmt.format(calendar.getTime());
        String day = dayFmt.format(calendar.getTime());
        generateMapper.updateDataStatistic(
                year,
                month,
                day,
                application.getCompanyId(),
                application.getAppId(),
                unit.doubleValue());
        return Response.info(200, "SUCCESS");
    }

    @Override
    @RequestConsistency
    @CompanyOperation(operator = false)
    public ResponseBounce<Object> getApplicationStatistic(String username, String companyName, String appId, String year, String month, int productId) {
        Company company = this.companyService.getCompanyByName(companyName);
        Assert.notNull(company);
        String yyyyMM = year + month;

        RSAObject application = this.baseMapper.selectOne(
                new QueryWrapper<RSAObject>()
                        .eq("company_id", company.getId())
                        .eq("app_id", appId));
        if (!DateHandler.isBeforeToday(yyyyMM, "yyyyMM"))
            return Response.fail("该月数据未生成");
        if (application == null)
            return Response.fail("应用不存在");
        if (!application.isEnable())
            return Response.fail("应用未上线");
        if (application.getBindingProductId() == null)
            return Response.fail("应用未绑定产品");
        if (!application.getBindingProductId().equals(String.valueOf(productId)))
            return Response.fail("应用绑定产品不匹配");

        Map<String, Double> map;
        try {
            map = generateMapper.getDataStatistic(year, month, company.getId(), appId, productId);
        } catch (Exception ignored) {
            return Response.fail("该月数据表不存在");
        }
        if (map == null)
            return Response.fail("本月无记录数据");
        List<Double> dataList = map.entrySet().stream()
                .sorted(Comparator.comparing(entry -> Integer.parseInt(entry.getKey())))
                .map(Map.Entry::getValue)
                .toList();
        return Response.success(dataList);
    }
}
