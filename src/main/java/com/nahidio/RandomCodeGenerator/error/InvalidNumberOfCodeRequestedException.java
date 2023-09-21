package com.nahidio.RandomCodeGenerator.error;

public class InvalidNumberOfCodeRequestedException extends Exception{

    public InvalidNumberOfCodeRequestedException() {
        super();
    }

    public InvalidNumberOfCodeRequestedException(String message) {
        super(message);
    }

    public InvalidNumberOfCodeRequestedException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidNumberOfCodeRequestedException(Throwable cause) {
        super(cause);
    }

    protected InvalidNumberOfCodeRequestedException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}