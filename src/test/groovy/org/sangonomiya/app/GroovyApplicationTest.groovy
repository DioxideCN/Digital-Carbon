package org.sangonomiya.app

import java.util.regex.Pattern

/**
 * @author Dioxide.CN
 * @date 2023/4/4 23:46
 * @since 1.0
 */
class GroovyApplicationTest {

    static void main(String[] args) {
        String regex = ~'^(?=.*\\d)(?=.*[a-z])(?=.*[A-Z])[a-zA-Z0-9_-]{8,16}$'
        println Pattern.matches(regex, "123456Aa")
    }

}
