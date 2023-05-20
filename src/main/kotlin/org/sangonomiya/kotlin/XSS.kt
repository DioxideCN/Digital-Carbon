package org.sangonomiya.kotlin

/**
 *
 * @author Dioxide.CN
 * @date 2023/4/8 10:40
 * @since 1.0
 */
class XSS {
    companion object {
        fun String.filter(): String {
            return this.replace("""<(\S*?)[^>]*>.*?|<.*?/>""".toRegex(), "")
        }
    }
}
