package com.manetdroid.meshify2.api.exceptions;

/**
 * Custom exception class for Meshify-related exceptions.
 */
public class MeshifyException extends RuntimeException {

    private final int errorCode;

    // Constructor for wrapping a generic Throwable.
    public MeshifyException(Throwable cause) {
        super(cause);
        this.errorCode = 100; // Default error code
    }

    // Constructor with an error code and a message
    public MeshifyException(int errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    // Getter method for retrieving the error code.
    public int getErrorCode() {
        return errorCode;
    }
}