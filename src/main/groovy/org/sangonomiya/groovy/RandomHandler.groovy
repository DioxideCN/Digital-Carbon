package org.sangonomiya.groovy

import java.text.DecimalFormat

/**
 * 随机工具脚本
 * @author Dioxide.CN
 * @date 2023/4/4 23:25
 * @since 1.0
 */
class RandomHandler {

    final static char[] CHARS = new char[52]
    final static int[] NUMS = new int[]{0,1,2,3,4,5,6,7,8,9}

    private static final String BASIC = "1234567890ABCDEFGHIJKLMNOPQRSTUVWXYZ"
    private static final Random random = new Random()

    static{
        for(char i = 'a'; i <= ('z' as Character); i++)
            CHARS[i - 97] = i

        for(char i = 'A'; i <= ('Z' as Character); i++)
            CHARS[i - 39] = i
    }

    /**
     * 在[min,max)区间生成随机Double
     * @param min 最小值
     * @param max 最大值
     * @param digits 保留小数位数
     */
    static Double randomDouble(int min, int max, int digits){
        if(digits < 1 || min > max)
            return null

        def ran = new Random()
        double decimal = ran.nextDouble()
        int integer = randomInt(min,max)
        double num = integer + decimal

        return Double.parseDouble(new DecimalFormat("0." + "0".repeat(digits)).format(num))
    }

    /**
     * 在[min,max)区间生成随机Float
     * @param min 最小值
     * @param max 最大值
     * @param digits 保留小数位数
     */
    static Float randomFloat(int min, int max, int digits){
        if(digits < 1 || min > max)
            return null

        def ran = new Random()
        float decimal = ran.nextFloat()
        int integer = randomInt(min,max)
        float num = (float) (integer + decimal)

        return Float.parseFloat(new DecimalFormat("0." + "0".repeat(digits)).format(num))
    }

    /**
     * 在[min,max)区间生成随机Int
     * @param min 最小值
     * @param max 最大值
     */
    static Integer randomInt(int min, int max){
        if(min > max){
            min ^= max
            max ^= min
            min ^= max
        }
        return new Random().nextInt(max - min) + min
    }

    /**
     * 在给定的数组中，随机取一个元素
     * @param objs 给定一个数组
     * @return 数组中的随机元素
     */
    static <T> T randomElement(T[] objs){
        if(objs == null || objs.length == 0)
            return null

        def ran = new Random()
        int ranIndex = ran.nextInt(objs.length)

        return objs[ranIndex]
    }

    /**
     * 生成随机字符串
     * @param length 生成随机字符串的长度
     * @param useChar true 使用字母等生成字符串 false 使用数字123等生成字符串
     */
    static String randomStr(int length, boolean useChar){
        if(length < 1)
            return null

        def ran = new Random()
        def builder = new StringBuilder()

        for(int i = 0; i < length;i++) {
            if(useChar) {
                int ranIndex = ran.nextInt(CHARS.length)
                builder.append(CHARS[ranIndex])
            } else {
                int ranIndex = ran.nextInt(NUMS.length)
                if (i == 0 && ranIndex == 0)
                    ranIndex = 1
                builder.append(NUMS[ranIndex])
            }
        }
        return builder.toString()
    }

    static String randomStrLen(int len) {
        String str = ""
        for (int i = 0; i < len; i++)
            str = drawStrLen(str)
        return str
    }

    /**
     * 字符串拼接
     */
    static String drawStrLen(String randomString){
        //从randString中获取char并强转为string
        String randString = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz"
        String rand = getRandomString(random.nextInt(randString.length()))
        //拼接randomString
        randomString += rand
        return randomString
    }

    /**
     * 获取随机的字符
     * @param num 随机的字符串长度
     */
    static String getRandomString(int num) {
        char[] basicArray = BASIC.toCharArray()
        def random = new Random()
        char[] result = new char[num]

        for (int i = 0; i < result.length; i++) {
            int index = random.nextInt(100) % (basicArray.length)
            result[i] = basicArray[index]
        }

        return new String(result)
    }

}
