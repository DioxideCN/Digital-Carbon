package org.sangonomiya.app.core.lazy;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

/**
 * 第一次访问时需要计算的值的代理对象
 * @param <T> 值的类型
 *
 * @author Dioxide.CN
 * @date 2023/3/6 21:06
 * @since 1.0
 */
public interface Lazy<T> extends Supplier<T> {

    /**
     * 构造 lazy-initialized Lazy初始化对象
     * @param supplier 该supplier不能为空且会在第一时间进行调用
     */
    static <T> Lazy<T> of(@NotNull Supplier<T> supplier) {
        return new Lazy.Fast<>(supplier);
    }

    /**
     * 构造一个线程安全的 thread-safe Lazy初始化对象
     * @param supplier 该supplier不能为空且会在第一时间进行调用
     */
    static <T> Lazy<T> concurrentOf(@NotNull Supplier<T> supplier) {
        return new Lazy.Concurrent<>(supplier);
    }

    /**
     * 线程不安全的解决方案（只考虑性能）
     */
    final class Fast<T> implements Lazy<T>
    {
        private Supplier<T> supplier;
        private T instance;

        private Fast(Supplier<T> supplier) {
            this.supplier = supplier;
        }

        @Nullable
        @Override
        public final T get() {
            if (supplier != null) {
                instance = supplier.get();
                supplier = null;
            }
            return instance;
        }
    }

    /**
     * 线程安全的解决方案（不考虑性能）
     */
    final class Concurrent<T> implements Lazy<T>
    {
        private volatile Object lock = new Object();
        private volatile Supplier<T> supplier;
        private volatile T instance;

        private Concurrent(Supplier<T> supplier) {
            this.supplier = supplier;
        }

        @Nullable
        @Override
        public final T get() {
            // 如果锁设置为空的操作在非空校验和同步操作两者之间发生
            // 则需要将锁复制到局部变量中以防止发生空指针异常
            Object localLock = this.lock;
            if (supplier != null) {
                // localLock 在这里不为空
                // 在复制锁后 supplier 必然不为空，并且都具有原子性
                synchronized (localLock) {
                    if (supplier != null) {
                        instance = supplier.get();
                        supplier = null;
                        this.lock = null;
                    }
                }
            }
            return instance;
        }
    }

}
