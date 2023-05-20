package org.sangonomiya.app.core.lazy;

import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

/**
 * 等同于 {@link Supplier} 但不允许为空
 * 但是必须是一个有效的 Supplier
 *
 * @see Supplier
 *
 * @author Dioxide.CN
 * @date 2023/3/6 21:00
 * @since 1.0
 */
@FunctionalInterface
public interface NonNullSupplier<T>
{
    @NotNull T get();
}
