package org.backend.billingbatch.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.backend.billingbatch.dto.ErrorResponse;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 예외별 상태 코드 저장소
    private static final Map<Class<? extends Exception>, HttpStatus> ERROR_MAP = Map.of(
            IllegalArgumentException.class, HttpStatus.NOT_FOUND,
            IllegalStateException.class, HttpStatus.BAD_REQUEST
    );

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleAllExceptions(Exception e, HttpServletRequest request) {
        // 기본값은 500 (Internal Server Error)
        HttpStatus status = ERROR_MAP.getOrDefault(e.getClass(), HttpStatus.INTERNAL_SERVER_ERROR);

        // 예외 클래스에 @ResponseStatus 어노테이션이 있는지 확인하고 있으면 그 값을 추출
        ResponseStatus responseStatus = AnnotatedElementUtils.findMergedAnnotation(e.getClass(), ResponseStatus.class);
        if (responseStatus != null) {
            status = responseStatus.value();
        }

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(status.value()) // ex. 404
                .error(status.getReasonPhrase())
                .message(e.getMessage()) // 에러 메시지
                .path(request.getRequestURI())
                .build();

        return new ResponseEntity<>(errorResponse, status);
    }
}
