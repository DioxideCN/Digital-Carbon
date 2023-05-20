package org.sangonomiya.groovy

import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.regex.Pattern

/**
 * 数据校验工具脚本
 * @author Dioxide.CN
 * @date 2023/4/4 23:33
 * @since 1.0
 */
class VerifyHandler {

    static CheckType of() {
        return new CheckType()
    }

    @SuppressWarnings("all")
    static class CheckType {

        /**
         * 校验用户名的合法性
         * @param username 用户名
         * @return true 用户名格式正确 false 用户名格式错误
         */
        boolean username(String username) {
            // 判断是否为空
            if (username == null)
                return false

            // 判断长度
            if (username.length() < 3 || username.length() > 12)
                return false

            // 正则匹配
            String pattern = ~'^[A-Za-z0-9_-]{3,12}$'
            return Pattern.matches(pattern, username)
        }

        /**
         * 校验密码的格式<br/>
         * 1. 必须包含大写字母和小写字母<br/>
         * 2. 必须包含数字<br/>
         * 3. 可以使用中划线和下划线<br/>
         * 4. 长度在8到16位之间（包含8和16）
         * @param password 被校验的密码
         * @return true 密码符合规范 false 密码不符合规范
         */
        boolean password(String password) {
            if (password == null || password.length() == 0)
                return false

            String regex = ~'^(?=.*\\d)(?=.*[a-z])(?=.*[A-Z])[a-zA-Z0-9_-]{8,16}$'
            return Pattern.matches(regex, password)
        }

        /**
         * 校验手机号的合法性，使用移动、电信、联通通用号段正则校验
         * @param phone 手机号
         * @return true 手机号格式正确 false 手机号格式错误
         */
        boolean phone(String phone) {
            try {
                if (phone == null || phone.isEmpty()) return false
                // 数字类型转换失败时直接中断
                Long.parseLong(phone)

                String pattern = ~'^1(3[0-9]|4[01456879]|5[0-35-9]|6[2567]|7[0-8]|8[0-9]|9[0-35-9])\\d{8}$'
                return Pattern.matches(pattern, phone)
            } catch (NumberFormatException var2) {
                return false
            }
        }

        /**
         * 校验电子邮箱地址的合法性
         * @param email 电子邮箱地址
         * @return true 邮箱地址格式正确 false 手机号格式错误
         */
        boolean email(String email) {
            if (!email.contains("@")) return false

            String pattern = ~'^[a-z0-9A-Z]+[- | a-z0-9A-Z . _]+@([a-z0-9A-Z]+(-[a-z0-9A-Z]+)?\\.)+[a-z]{2,}$'
            return Pattern.matches(pattern, email)
        }

        /**
         * 校验URL地址的合法性
         * @param url 地址
         * @return true 格式正确 false 格式错误
         */
        boolean url(String url) {
            String pattern = ~'^(http:\\/\\/|https:\\/\\/)[a-z0-9]+([\\-\\.]{1}[a-z0-9]+)*\\.[a-z]{2,5}(:[0-9]{1,5})?(\\/.*)?|^((http:\\/\\/|https:\\/\\/)?([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\.){3}([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])(:\\d{0,5})?(\\/.*)?$'
            return Pattern.matches(pattern, url)
        }

        /**
         * 校验公司/企业名称的合法性
         * @param companyName 公司/企业名称
         * @return true 格式正确 false 格式错误
         */
        boolean company(String companyName) {
            if (companyName == null)
                return false
            if (companyName.length() < 2 || companyName.length() > 64)
                return false

            String pattern = ~'^[\\u4e00-\\u9fa5A-Za-z0-9\\S_-]{3,64}$'
            return Pattern.matches(pattern, companyName)
        }

        boolean order(String orderId) {
            try {
                SimpleDateFormat awaitDateFormat = new SimpleDateFormat("HHmmssSSS")
                Date awaitDate = awaitDateFormat.parse(orderId)
                return true
            } catch (ParseException ignored) {
                return false
            }
        }

    }

}
