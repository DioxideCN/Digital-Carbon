package org.sangonomiya.app.extension.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.sangonomiya.app.core.Response;
import org.sangonomiya.app.extension.annotation.AzygoService;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Dioxide.CN
 * @date 2023/3/21 18:50
 * @since 1.0
 */
@Aspect
@Component
@Slf4j
public class AspectLogicalService {

    private static final ConcurrentHashMap<String, Method> lockingMap = new ConcurrentHashMap<>();

    @Pointcut("@annotation(org.sangonomiya.app.extension.annotation.AzygoService)")
    public void azygoServicePoint() {}

    @Around("azygoServicePoint()")
    public Object resolveAzygoServiceLogical(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        AzygoService anno = method.getAnnotation(AzygoService.class);

        StringBuilder buffer = new StringBuilder(method.getName());
        for (int index : anno.keyPos()) {
            if (index > joinPoint.getArgs().length - 1)
                throw new ArrayIndexOutOfBoundsException();

            // 构造唯一键
            buffer.append(joinPoint.getArgs()[index].toString());
        }

        Method awaitMethod = lockingMap.get(buffer.toString());
        if (awaitMethod == null) {
            // 集合中不存在该唯一接口线程
            lockingMap.put(buffer.toString(), method);
        } else if (awaitMethod.getName().equals(method.getName())) {
            // 存在且相同直接屏蔽
            return Response.fail("请勿频繁操作");
        }

        Object result = joinPoint.proceed();
        lockingMap.remove(buffer.toString()); // 运行完毕移出阻塞队列
        return result;
    }

}
