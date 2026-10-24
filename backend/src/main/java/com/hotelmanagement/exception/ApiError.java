package com.hotelmanagement.exception;

import java.time.Instant;
import java.util.List;
import java.util.Map;

public record ApiError(ErrorCode code, String message, List<FieldError> fieldErrors,
                       Map<String, Object> details, Instant timestamp) {
    public ApiError {
        fieldErrors = List.copyOf(fieldErrors);
        details = Map.copyOf(details);
    }
    public record FieldError(String field, String message) {}
}
