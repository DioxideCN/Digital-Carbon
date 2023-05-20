package org.sangonomiya.app.extension.aspect;

import io.jsonwebtoken.lang.Assert;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.sangonomiya.app.core.Response;
import org.sangonomiya.app.entity.Company;
import org.sangonomiya.app.entity.Permission;
import org.sangonomiya.app.entity.UserVO;
import org.sangonomiya.app.extension.annotation.CompanyOperation;
import org.sangonomiya.app.service.ICompanyService;
import org.sangonomiya.app.service.IPermissionService;
import org.sangonomiya.app.service.IUserService;
import org.sangonomiya.app.service.impl.PermissionServiceImpl;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.lang.reflect.Method;

/**
 * @author Dioxide.CN
 * @date 2023/2/28 22:34
 * @since 1.0
 */
@Aspect
@Component
@Slf4j
public class AspectCompanyService {

    @Resource
    private IUserService userService;
    @Resource
    private ICompanyService companyService;
    @Resource
    private IPermissionService permissionService;

    @Pointcut("@annotation(org.sangonomiya.app.extension.annotation.CompanyOperation)")
    public void companyOperation() {}

    /**
     * companyOperation处理用户和企业是否具有对应关系
     * 当operator为true时还必须拥有admin.super.*权限
     * @param joinPoint 切点方法类
     */
    @Around("companyOperation()")
    public Object companyOperationAction(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        CompanyOperation anno = method.getAnnotation(CompanyOperation.class);

        String username = (String) joinPoint.getArgs()[anno.value()[0]];
        Object companyObject = joinPoint.getArgs()[anno.value()[1]];
        String companyName = null;

        if(companyObject instanceof Integer)
            companyName = companyService.getCompanyById((Integer) companyObject).getCompanyName();
        if(companyObject instanceof String)
            companyName = (String) companyObject;

        UserVO awaitAdmin = userService.getUserByUsername(username);
        if (awaitAdmin == null)
            return Response.fail("无效的用户信息");

        // 一大堆查询操作
        Permission awaitGroup = userService.getUserPermissionGroup(awaitAdmin);
        Assert.notNull(awaitGroup);
        Company awaitCompany;
        if (awaitGroup.getGroupName().equals("超级管理员")) {
            awaitCompany = companyService.inWhichCompany(awaitAdmin);
        } else {
            awaitCompany = permissionService.getPermissionGroupFromWhichCompany(awaitGroup);
        }

        if (awaitCompany == null) return Response.fail("用户没有企业资料");

        if (!awaitCompany.getCompanyName().equals(companyName))
            return Response.fail("用户不属于该企业");

        if (anno.operator() && !awaitGroup.getPermissions().equals(PermissionServiceImpl.groups[0]))
            return Response.fail("无权限使用");

        return joinPoint.proceed();
    }

}
