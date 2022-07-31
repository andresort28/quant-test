package com.bitso.exception;

/**
 * Exception for not supported message in the Exchange
 */
public class MessageNotSupportedException extends Exception {

    public MessageNotSupportedException(String message) {
        super(message);
    }
}
