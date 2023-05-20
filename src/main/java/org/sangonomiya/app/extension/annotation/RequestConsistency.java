package org.sangonomiya.app.extension.annotation;

import org.sangonomiya.app.extension.aspect.ParamType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <code>value</code> 用于指定被检索的目标username索引位置，默认为方法第一个参数
 *
 * @author Dioxide.CN
 * @date 2023/3/6 21:56
 * @since 1.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface RequestConsistency {
    int value() default 0;
}
