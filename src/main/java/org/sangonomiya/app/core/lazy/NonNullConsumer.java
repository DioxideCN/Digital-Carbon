package org.sangonomiya.app.core.lazy;

import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

/**
 * 等同于 {@link Consumer} 但不允许为空
 *
 * @see Consumer
 *
 * @author Dioxide.CN
 * @date 2023/3/6 21:28
 * @since 1.0
 */
@FunctionalInterface
public interface NonNullConsumer<T> {
    void accept(@NotNull T t);
}
