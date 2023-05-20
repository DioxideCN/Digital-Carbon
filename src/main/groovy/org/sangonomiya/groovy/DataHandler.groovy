package org.sangonomiya.groovy

import java.text.SimpleDateFormat

/**
 * 数据类脚本
 * @author Dioxide.CN
 * @date 2023/4/4 22:49
 * @since ${SINCE}
 */
class DataHandler {

    /**
     * 按时间戳生成一个订单号，年-月-日-时-分-秒-毫秒
     * <p>
     * 后期使用UUID和雪花算法生成订单号
     *
     * @return 生成的订单号
     */
    static String getOrderId() {
        def calendar = Calendar.getInstance()

        int year = calendar.get(Calendar.YEAR)
        int month = calendar.get(Calendar.MONTH) + 1
        int date = calendar.get(Calendar.DATE)

        def var2 = month < 10 ? "0${month}" : "${month}"
        def var3 = date < 10 ? "0${date}" : "${date}"

        def time = calendar.getTime()
        def format = new SimpleDateFormat("HHmmssSSS")
        def timeStr = format.format(time)

        return year + var2 + var3 + timeStr
    }

    /**
     * 字符串是否不为空且有有效内容
     * @param value 字符串
     * @return true 有效 false 无效
     */
    static boolean isAvailable(String value) {
        if (value == null) return false
        return !value.isBlank() && !value.isEmpty()
    }

    /**
     * 一组字符串是否不为空且有有效内容
     * @param value 一组字符串
     * @return true 有效 false 无效
     */
    static boolean isAvailable(String ...value) {
        boolean flag = true
        for (String s : value) {
            flag &= isAvailable(s)
        }
        return flag
    }

}
