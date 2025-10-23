package com.example.todo.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@RestControllerAdvice
public class RestExceptionHandler {
    private ResponseEntity<Object> builResponseEntity(ApiException apiException) {
        return new ResponseEntity<>(apiException, apiException.getStatus());
    }

    @ExceptionHandler({NotFoundException.class})
    public ResponseEntity<Object> handleNotFoundException(NotFoundException e) {
        ApiException apiException = new ApiException(NOT_FOUND
                , e.getMessage(), LocalDateTime.now());
        return builResponseEntity(apiException);
    }

    @ExceptionHandler({ForbiddenException.class})
    public ResponseEntity<Object> handleForbiddenException(ForbiddenException e) {
        ApiException apiException = new ApiException(FORBIDDEN
        , e.getMessage(), LocalDateTime.now());
        return builResponseEntity(apiException);
    }
}
