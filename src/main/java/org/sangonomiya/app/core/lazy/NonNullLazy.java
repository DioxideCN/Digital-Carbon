package org.sangonomiya.app.core.lazy;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * 第一次访问时需要计算的值的代理对象
 * @param <T> 值的类型
 *
 * @author Dioxide.CN
 * @date 2023/3/6 21:02
 * @since 1.0
 */
public interface NonNullLazy<T> extends NonNullSupplier<T> {
    /**
     * 构造 lazy-initialized Lazy初始化对象
     * @param supplier 该supplier不能为空且会在第一时间进行调用
     */
    static <T> NonNullLazy<T> of(@NotNull NonNullSupplier<T> supplier)
    {
        Lazy<T> lazy = Lazy.of(supplier::get);
        return () -> Objects.requireNonNull(lazy.get());
    }

    /**
     * 构造一个线程安全的 lazy-initialized Lazy初始化对象
     * @param supplier 该supplier不能为空且会在第一时间进行调用
     */
    static <T> NonNullLazy<T> concurrentOf(@NotNull NonNullSupplier<T> supplier)
    {
        Lazy<T> lazy = Lazy.concurrentOf(supplier::get);
        return () -> Objects.requireNonNull(lazy.get());
    }
}
