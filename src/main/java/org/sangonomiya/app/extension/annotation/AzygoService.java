package org.sangonomiya.app.extension.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * {@link AzygoService} 命名来源于奇静脉(Azygos Vein)一词，它一般用于修饰串行接口业务。
 * <p>
 * 该注解可以使得一个业务方法线程原子化，即：在该程序中只能存在一个线程正在运行被该注解修饰的业务。
 * <p>
 * 特殊的 {@link AzygoService} 可以配合属性 {@link AzygoService#keyPos()} 实现
 * 关键字标记，即对该方法的参数进行原子化标记。
 *
 * @author Dioxide.CN
 * @date 2023/3/21 18:23
 * @since 1.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface AzygoService {
    int[] keyPos() default {};
}
