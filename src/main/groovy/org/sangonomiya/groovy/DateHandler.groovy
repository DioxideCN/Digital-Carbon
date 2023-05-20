package org.sangonomiya.groovy

import java.text.DateFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.concurrent.TimeUnit

/**
 * 时间类脚本
 * @author Dioxide.CN
 * @date 2023/4/4 22:52
 * @since 1.0
 */
class DateHandler {

    /**
     * 获取时间字符串，用来插入数据库的 <code>datetime</code> 类型字段
     * @return 调用时时间字符串
     */
    static String getDateString() {
        def format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        return format.format(new Date())
    }

    /**
     * 判断String日期是否在今天之前
     * @param value 被判断的日期String
     * @return true 已过期 false 未过期
     */
    static boolean isBeforeToday(String value) {
        def today = new Date()
        def dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        try {
            def expireDate = dateFormat.parse(value)
            return expireDate.before(today)
        } catch (ParseException e) {
            throw new RuntimeException(e)
        }
    }

    /**
     * 判断String日期是否在今天之前
     * @param value 被判断的日期String
     * @return true 已过期 false 未过期
     */
    static boolean isBeforeToday(String value, String format) {
        def today = new Date()
        def dateFormat = new SimpleDateFormat(format)
        try {
            def expireDate = dateFormat.parse(value)
            return expireDate.before(today)
        } catch (ParseException e) {
            throw new RuntimeException(e)
        }
    }

    /**
     * 计算两个日期之间相差的天数（不分先后顺序）
     * @param day1 第一个日期
     * @param day2 第二个日期
     * @return 返回两个日期之间的天数差
     */
    static int calcJetLag(String day1, String day2) {
        def dft = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            def star = dft.parse(day1)
            def endDay = dft.parse(day2)

            long starTime = star.getTime()
            long endTime = endDay.getTime()
            long num = Math.abs(endTime - starTime)

            return num / 24 / 60 / 60 / 1000
        } catch (ParseException e) {
            throw new RuntimeException(e)
        }
    }

    /**
     * 计算两个日期之间差（不分先后顺序）
     * @param day1 第一个日期
     * @param day2 第二个日期
     * @return 返回两个日期之间的时间差
     */
    static long calcMillionJetLag(String day1, String day2) {
        def dft = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            def star = dft.parse(day1)
            def endDay = dft.parse(day2)

            long starTime = star.getTime()
            long endTime = endDay.getTime()
            long num = Math.abs(endTime - starTime)

            return num
        } catch (ParseException e) {
            throw new RuntimeException(e)
        }
    }

    /**
     * 从当前日期开始增加year年
     * @param year 购买时长
     * @return 返回增加后的日期string
     */
    static String increaseYear(int year) {
        def format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")

        def rightNow = Calendar.getInstance()
        rightNow.add(Calendar.YEAR, year)

        def resDate = rightNow.getTime()
        return format.format(resDate)
    }

    /**
     * 从start日期开始增加year年
     * @param start 起始日期字符串
     * @param year 购买时长
     * @return 返回增加后的日期string
     */
    static String increaseYear(String start, int year) {
        def format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        try {
            def startDate = format.parse(start)

            def calendar = Calendar.getInstance()
            calendar.setTime(startDate)
            calendar.add(Calendar.YEAR, year)

            return format.format(calendar.getTime())
        } catch (ParseException e) {
            throw new RuntimeException(e)
        }
    }

    /**
     * 从start日期开始增加day天
     * @param start 起始日期字符串
     * @param day 购买时长
     * @return 返回增加后的日期string
     */
    static String increaseDay(String start, int day) {
        def format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        try {
            def startDate = format.parse(start)

            def calendar = Calendar.getInstance()
            calendar.setTime(startDate)
            calendar.add(Calendar.DATE, day)

            return format.format(calendar.getTime())
        } catch (ParseException e) {
            throw new RuntimeException(e)
        }
    }

}
