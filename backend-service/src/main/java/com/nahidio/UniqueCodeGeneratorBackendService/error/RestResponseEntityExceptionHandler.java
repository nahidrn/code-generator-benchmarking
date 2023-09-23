package com.nahidio.UniqueCodeGeneratorBackendService.error;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import com.nahidio.UniqueCodeGeneratorBackendService.entity.ErrorMessage;

@ControllerAdvice
@ResponseStatus
public class RestResponseEntityExceptionHandler
        extends ResponseEntityExceptionHandler {

    @ExceptionHandler(CodeGenerationErrorException.class)
    public ResponseEntity<ErrorMessage> codeGeneratorError(CodeGenerationErrorException exception,
                                                    WebRequest request) {
        ErrorMessage message = new ErrorMessage(HttpStatus.INTERNAL_SERVER_ERROR,
                exception.getMessage());

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(message);
    }
    
    @ExceptionHandler(InvalidNumberOfCodeRequestedException.class)
    public ResponseEntity<ErrorMessage> invalidCumberOfCodeRequested(InvalidNumberOfCodeRequestedException exception,
                                                    WebRequest request) {
        ErrorMessage message = new ErrorMessage(HttpStatus.INTERNAL_SERVER_ERROR,
                exception.getMessage());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(message);
    }
}