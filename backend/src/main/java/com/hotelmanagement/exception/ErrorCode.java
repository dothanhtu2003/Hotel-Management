package com.hotelmanagement.exception;

import org.springframework.http.HttpStatus;

// HTTP mappings not specified per code in V1 are proposals documented in docs/m2-handoff.md.
public enum ErrorCode {
    VALIDATION_ERROR(400),
    INVALID_DATE_RANGE(400),
    CHECK_IN_DATE_IN_PAST(400),
    CAPACITY_EXCEEDED(400),
    DUPLICATE_SERVICE_ITEM(400),
    UNAUTHENTICATED(401),
    ACCESS_DENIED(403),
    ROOM_NOT_FOUND(404),
    GUEST_NOT_FOUND(404),
    BOOKING_NOT_FOUND(404),
    ROOM_NOT_AVAILABLE(409),
    ROOM_OUT_OF_SERVICE(409),
    ROOM_NOT_CLEAN(409),
    INVALID_STATUS_TRANSITION(409),
    CHECK_IN_NOT_ALLOWED_TODAY(409),
    CANCELLATION_NOT_ALLOWED(409),
    CHECK_OUT_DATE_NOT_SUPPORTED(409),
    SERVICE_NOT_AVAILABLE(409),
    BILL_CHANGED(409),
    EMAIL_ALREADY_EXISTS(409),
    RESOURCE_BUSY(409),
    ROOM_ON_HOLD(409),
    HOLD_EXPIRED(409),
    INVALID_HOLD(409),
    INTERNAL_ERROR(500),
    HOLD_SERVICE_UNAVAILABLE(503);

    private final HttpStatus status;
    ErrorCode(int status) { this.status = HttpStatus.valueOf(status); }
    public HttpStatus status() { return status; }
}
