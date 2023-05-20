package org.sangonomiya.app.core;

/**
 * 高并发高吞吐业务单独处理
 * @author Dioxide.CN
 * @date 2023/3/6 16:56
 * @since 1.0
 */
public class ConcurrentHandler {

    private volatile int TRANSACTION_LEVEL = 0;

    // 对读写事务分离
    public void isolation() {
        this.TRANSACTION_LEVEL = 1;
    }

}
