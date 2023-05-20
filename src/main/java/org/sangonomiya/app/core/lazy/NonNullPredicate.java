package org.sangonomiya.app.core.lazy;

import org.jetbrains.annotations.NotNull;

import java.util.function.Predicate;

/**
 * 等同于 {@link Predicate} 但不允许为空
 *
 * @see Predicate
 *
 * @author Dioxide.CN
 * @date 2023/3/6 21:44
 * @since 1.0
 */
@FunctionalInterface
public interface NonNullPredicate<T> {
    boolean test(@NotNull T t);
}
