package org.sangonomiya.kotlin

import org.springframework.util.ObjectUtils
import java.util.stream.Collector
import java.util.stream.Collectors

/**
 *
 * @author Dioxide.CN
 * @date 2023/4/7 16:45
 * @since 1.0
 */
class Pair<L, R> private constructor(left: L, right: R) {
    private val leftValue: L
    private val rightValue: R

    init {
        leftValue = left
        rightValue = right
    }

    fun left(): L {
        return leftValue
    }

    fun right(): R {
        return rightValue
    }

    override fun hashCode(): Int {
        var result = ObjectUtils.nullSafeHashCode(leftValue)
        result = 31 * result + ObjectUtils.nullSafeHashCode(rightValue)
        return result
    }

    override fun equals(other: Any?): Boolean {
        return if (this === other) {
            true
        } else if (other !is Pair<*, *>) {
            false
        } else {
            ObjectUtils.nullSafeEquals(leftValue, other.leftValue) &&
                    ObjectUtils.nullSafeEquals(rightValue, other.rightValue)
        }
    }

    override fun toString(): String {
        return "<${leftValue.toString()}, ${rightValue.toString()}>"
    }

    companion object {
        /**
         * 创建空的Pair
         * @return 返回两边都为空的Pair对象
         */
        @JvmStatic
        fun <L, R> empty(): Pair<L?, R?> {
            return Pair(null, null)
        }

        /**
         * 创建Pair
         * @param left 左侧值
         * @param right 右侧值
         * @return 返回Pair对象
         */
        @JvmStatic
        fun <L, R> of(left: L, right: R): Pair<L, R> {
            return Pair(left, right)
        }

        /**
         * 镜像创建Pair
         * @param right 左侧右边值
         * @param left 右侧左边值
         * @return 返回左右镜像的Pair对象
         */
        @JvmStatic
        fun <L, R> flip(right: L, left: R): Pair<R, L> {
            return Pair(left, right)
        }

        /**
         * 将Pair转换为Map
         * @return 转换为Map的对象
         */
        @JvmStatic
        fun <S, T> toMap(): Collector<Pair<S, T>, *, Map<S, T>> {
            return Collectors.toMap(
                { obj: Pair<S, T> -> obj.left() }
            ) { obj: Pair<S, T> -> obj.right() }
        }
    }
}
