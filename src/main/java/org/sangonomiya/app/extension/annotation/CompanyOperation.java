package org.sangonomiya.app.extension.annotation;

import org.sangonomiya.app.service.impl.CompanyServiceImpl;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * {@link CompanyOperation} 是取代以前 @LegalCompany 的新的企业操作业务切面
 * <p>
 * 该注解通常在 {@link CompanyServiceImpl} 的业务层方法上切入，在操作前会进行安全认证
 * <p>
 * 该注解需要提供 username 和 company_name 方法参数的索引，切面会自动进行参数认证。
 * 默认参数列表中 0 和 1 索引位置的为指定的用户和指定的企业。
 *
 * @author Dioxide.CN
 * @date 2023/3/7 14:53
 * @since 1.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface CompanyOperation {
    int[] value() default {0, 1};
    boolean operator() default true;
}
