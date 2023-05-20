package org.sangonomiya.app.core.lazy;

import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

/**
 * 等同于 {@link Function} 但不允许为空
 *
 * @see Function
 *
 * @author Dioxide.CN
 * @date 2023/3/6 21:43
 * @since 1.0
 */
@FunctionalInterface
public interface NonNullFunction<T, R> {
    @NotNull
    R apply(@NotNull T t);
}
