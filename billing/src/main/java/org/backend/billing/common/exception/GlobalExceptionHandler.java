package org.backend.billing.common.exception;

import org.backend.billing.common.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ApiException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<?> handle(ApiException e) {
        return ApiResponse.fail(e.getCode() + ": " + e.getMessage());
    }

    // 청구서 에러 처리
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiResponse<?> handleNotFound(IllegalArgumentException e) {
        return ApiResponse.fail("NOT_FOUND: " + e.getMessage());
    }

    @ExceptionHandler(IllegalStateException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<?> handleIllegalState(IllegalStateException e) {
        return ApiResponse.fail("BAD_REQUEST: " + e.getMessage());
    }

    // 기본 에러 값은 500 (Internal Server Error)
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiResponse<?> handleUnknown(Exception e) {
        return ApiResponse.fail("INTERNAL_ERROR: " + e.getMessage());
    }
}