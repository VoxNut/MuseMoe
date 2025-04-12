package com.javaweb.exception;

public class NetworkUnavailableException extends Exception {

    public NetworkUnavailableException(String message) {
        super(message);
    }

    public NetworkUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
