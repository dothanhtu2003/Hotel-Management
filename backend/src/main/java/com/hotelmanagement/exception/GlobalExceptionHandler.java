package com.hotelmanagement.exception;

import java.time.Clock;
import java.util.List;
import java.util.Map;
import jakarta.validation.ConstraintViolationException;
import jakarta.persistence.LockTimeoutException;
import jakarta.persistence.PessimisticLockException;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.bind.MissingServletRequestParameterException;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private final Clock clock;
    public GlobalExceptionHandler(Clock clock) { this.clock = clock; }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiError> business(BusinessException exception) {
        return response(exception.code(), exception.getMessage(), List.of(), exception.details());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> validation(MethodArgumentNotValidException exception) {
        var errors = exception.getBindingResult().getFieldErrors().stream()
                .map(error -> new ApiError.FieldError(error.getField(), error.getDefaultMessage()))
                .toList();
        return response(ErrorCode.VALIDATION_ERROR, "Dữ liệu không hợp lệ.", errors, Map.of());
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiError> constraint(ConstraintViolationException exception) {
        var errors = exception.getConstraintViolations().stream()
                .map(error -> new ApiError.FieldError(error.getPropertyPath().toString(), error.getMessage()))
                .toList();
        return response(ErrorCode.VALIDATION_ERROR, "Dữ liệu không hợp lệ.", errors, Map.of());
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class,
            MissingServletRequestParameterException.class, HandlerMethodValidationException.class})
    public ResponseEntity<ApiError> malformed(Exception exception) {
        return response(ErrorCode.VALIDATION_ERROR, "Định dạng hoặc tham số không hợp lệ.", List.of(), Map.of());
    }

    // Do not include optimistic locking or transaction serialization failures.
    // Verify the exact MySQL/Hibernate translation with integration tests when M3 is available.
    @ExceptionHandler({CannotAcquireLockException.class, LockTimeoutException.class, PessimisticLockException.class})
    public ResponseEntity<ApiError> busy(Exception exception) {
        return response(ErrorCode.RESOURCE_BUSY, "Tài nguyên đang bận. Vui lòng thử lại.", List.of(), Map.of());
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiError> unauthenticated(AuthenticationException exception) {
        return response(ErrorCode.UNAUTHENTICATED, "Bạn cần đăng nhập.", List.of(), Map.of());
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiError> forbidden(AccessDeniedException exception) {
        return response(ErrorCode.ACCESS_DENIED, "Bạn không có quyền thực hiện thao tác này.", List.of(), Map.of());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> unexpected(Exception exception) {
        // Preserve framework statuses (e.g. 404/405), while hiding implementation details.
        var status = exception instanceof org.springframework.web.ErrorResponse error
                ? error.getStatusCode() : ErrorCode.INTERNAL_ERROR.status();
        return ResponseEntity.status(status).body(new ApiError(ErrorCode.INTERNAL_ERROR,
                "Không thể xử lý yêu cầu.", List.of(), Map.of(), clock.instant()));
    }

    @ExceptionHandler(org.springframework.dao.DataAccessException.class)
    public ResponseEntity<ApiError> database(org.springframework.dao.DataAccessException exception) {
        return response(ErrorCode.INTERNAL_ERROR, "Không thể xử lý yêu cầu.", List.of(), Map.of());
    }

    private ResponseEntity<ApiError> response(ErrorCode code, String message,
                                              List<ApiError.FieldError> fields, Map<String, Object> details) {
        return ResponseEntity.status(code.status())
                .body(new ApiError(code, message, fields, details, clock.instant()));
    }
}
