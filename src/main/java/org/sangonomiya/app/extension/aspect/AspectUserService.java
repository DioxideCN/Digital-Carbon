package org.sangonomiya.app.extension.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.sangonomiya.app.core.Response;
import org.sangonomiya.app.extension.annotation.RequestConsistency;
import org.sangonomiya.app.extension.component.JwtUnit;
import org.sangonomiya.app.service.IUserService;
import org.sangonomiya.app.entity.UserVO;
import org.sangonomiya.groovy.VerifyHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;

/**
 * @author Dioxide.CN
 * @date 2023/2/28 22:34
 * @since 1.0
 */
@Aspect
@Component
@Slf4j
public class AspectUserService {

    @Resource
    private IUserService userService;
    @Resource
    private JwtUnit jwtUnit;

    @Value("${jwt.tokenHeader}")
    private String tokenHeader;
    @Value("${jwt.tokenHead}")
    private String tokenHead;

    @Pointcut("@annotation(org.sangonomiya.app.extension.annotation.RequestConsistency)")
    public void requestConsistency() {}

    /**
     * 请求接口前校验Token信息中的User信息和请求获取数据的目标来源User是否一致
     * @param joinPoint 切点方法类
     */
    @Around("requestConsistency()")
    public Object requestConsistency(ProceedingJoinPoint joinPoint) throws Throwable {
        String authHeader = separateAuthorization();
        if (authHeader == null || !authHeader.startsWith(tokenHead))
            return Response.fail("用户未授权");

        String authToken = authHeader.substring(tokenHead.length());
        String username = jwtUnit.getUserNameFromToken(authToken);

        if (!VerifyHandler.of().username(username))
            return Response.fail("用户名格式错误");
        if(SecurityContextHolder.getContext().getAuthentication() == null)
            return Response.fail("请先登录");

        UserVO user = userService.getUserByUsername(username);
        if (user == null || !user.isEnable())
            return Response.fail("用户不存在或被禁用");

        // 同源校验
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        RequestConsistency anno = method.getAnnotation(RequestConsistency.class);

        String awaitUsername = (String) joinPoint.getArgs()[anno.value()];
        if (!username.equals(awaitUsername))
            return Response.fail("请求用户不一致");

        return joinPoint.proceed();
    }

    private String separateAuthorization() {
        ServletRequestAttributes sra = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
        HttpServletRequest request = sra.getRequest();
        return request.getHeader(tokenHeader);
    }

}
