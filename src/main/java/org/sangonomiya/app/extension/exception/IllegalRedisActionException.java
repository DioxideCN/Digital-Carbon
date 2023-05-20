package org.sangonomiya.app.extension.exception;

/**
 * @author Dioxide.CN
 * @date 2023/3/13 8:49
 * @since 1.0
 */
public class IllegalRedisActionException extends Exception {
    public IllegalRedisActionException() {
        super();
    }

    public IllegalRedisActionException(String message) {
        super(message);
    }
}
