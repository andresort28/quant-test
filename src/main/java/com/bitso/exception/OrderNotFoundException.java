package com.bitso.exception;

/**
 * Exception when an Order is not found in the Exchange
 *
 * @author Andres Ortiz
 */
public class OrderNotFoundException extends Exception {

    public OrderNotFoundException(String message) {
        super(message);
    }
}