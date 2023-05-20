package org.sangonomiya.app.extension.exception;

/**
 * @author Dioxide.CN
 * @date 2023/3/6 17:22
 * @since 1.0
 */
public class UnsupportedParamTypeException extends Exception {
    public UnsupportedParamTypeException() {
        super();
    }

    public UnsupportedParamTypeException(String message) {
        super(message);
    }
}
