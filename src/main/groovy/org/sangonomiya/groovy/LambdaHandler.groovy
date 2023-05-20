package org.sangonomiya.groovy

import java.util.function.BiConsumer
import java.util.function.BiFunction
import java.util.function.BiPredicate

/**
 * Lambda表达式工具通畅用来增强stream流
 * @author Dioxide.CN
 * @date 2023/4/4 23:09
 * @since 1.0
 */
class LambdaHandler {

    static class counter {
        int i = 0;
        int getAndIncrement(){
            return i++;
        }
    }

    static class Function {
        static <T, R> java.util.function.Function<T, R> with(BiFunction<T, Integer, R> biFunction) {
            counter counter = new counter()
            return (t) -> biFunction.apply(t as T, counter.getAndIncrement())
        }
    }

    static class Predicate {
        static <T> java.util.function.Predicate<T> with(BiPredicate<T, Integer> biPredicate) {
            counter counter = new counter();
            return (t) -> biPredicate.test(t as T, counter.getAndIncrement())
        }
    }

    static class Consumer {
        static <T> java.util.function.Consumer<T> with(BiConsumer<T, Integer> biConsumer) {
            counter counter = new counter();
            return (t) -> biConsumer.accept(t as T, counter.getAndIncrement())
        }
    }

}
