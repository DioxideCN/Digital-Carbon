package org.sangonomiya.app.extension;

import org.sangonomiya.app.extension.exception.IllegalRedisActionException;

/**
 * @author Dioxide.CN
 * @date 2023/3/8 17:57
 * @since 1.0
 */
public class RedisAction {

    static final String SPLIT = ".";

    public static final String REGISTER = "phoneRegister";
    public static final String LOGIN = "phoneLogin";
    public static final String FIND_PASSWORD = "phoneFindPassword";
    public static final String CHANGING = "phoneChanging";
    public static final String RSA_GENERATE = "rsaGenerate";

    public static String payment(String key) {
        return key + SPLIT + "payment";
    }

    public static String order(String key, String orderId) {
        return key + SPLIT + orderId + SPLIT + "order";
    }

    // 地理位置查询缓存
    public static String geo(String address) {
        return address + SPLIT + "geo";
    }

    public static String geoRes(String address) {
        return address + SPLIT + "geoRes";
    }

    /* 手机号的Redis Action类型 */
    public static String phone(String phone, String actionType) {
        return phone + SPLIT + actionType;
    }

    public static String getActionKey(String key) {
        try {
            String[] var1 = checkoutAction(key);
            return var1[1];
        } catch (IllegalRedisActionException e) {
            e.printStackTrace();
            return "";
        }
    }

    public static String getActionValue(String actionFull) {
        try {
            String[] var1 = checkoutAction(actionFull);
            return var1[0];
        } catch (IllegalRedisActionException e) {
            e.printStackTrace();
            return "";
        }
    }

    private static String[] checkoutAction(String actionFull) throws IllegalRedisActionException {
        if (!actionFull.contains("."))
            throw new IllegalRedisActionException();

        String[] var1 = actionFull.split("[.]");
        if (var1.length < 2)
            throw new IllegalRedisActionException();

        return var1;
    }

}
