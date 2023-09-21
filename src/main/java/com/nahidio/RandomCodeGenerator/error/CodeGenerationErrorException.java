package com.nahidio.RandomCodeGenerator.error;

public class CodeGenerationErrorException extends Exception{

    public CodeGenerationErrorException() {
        super();
    }

    public CodeGenerationErrorException(String message) {
        super(message);
    }

    public CodeGenerationErrorException(String message, Throwable cause) {
        super(message, cause);
    }

    public CodeGenerationErrorException(Throwable cause) {
        super(cause);
    }

    protected CodeGenerationErrorException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}