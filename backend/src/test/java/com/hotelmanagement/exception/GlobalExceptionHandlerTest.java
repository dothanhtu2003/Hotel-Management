package com.hotelmanagement.exception;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.time.Clock;
import java.time.Instant;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

class GlobalExceptionHandlerTest {
    private final Clock clock = mock(Clock.class);
    private final GlobalExceptionHandler handler = new GlobalExceptionHandler(clock);

    @Test
    void billChangedReturns409AndCurrentTotal() {
        var now = Instant.parse("2026-10-15T04:00:00Z");
        when(clock.instant()).thenReturn(now);
        var result = handler.business(new BusinessException(ErrorCode.BILL_CHANGED,
                "Tổng tiền đã thay đổi.", Map.of("currentTotalAmount", "1500000")));
        assertEquals(HttpStatus.CONFLICT, result.getStatusCode());
        assertEquals("1500000", result.getBody().details().get("currentTotalAmount"));
        assertEquals(now, result.getBody().timestamp());
        assertTrue(result.getBody().fieldErrors().isEmpty());
    }

    @Test
    void mapsLockFailuresToResourceBusy() {
        var result = handler.busy(new CannotAcquireLockException("private database information"));
        assertEquals(HttpStatus.CONFLICT, result.getStatusCode());
        assertEquals(ErrorCode.RESOURCE_BUSY, result.getBody().code());
        assertFalse(result.getBody().message().contains("private"));
    }

    @Test
    void hidesUnexpectedExceptionDetails() {
        var result = handler.unexpected(new IllegalStateException("password=secret"));
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, result.getStatusCode());
        assertFalse(result.getBody().message().contains("secret"));
    }

    @Test
    void keepsFrameworkHttpStatus() {
        assertEquals(HttpStatus.METHOD_NOT_ALLOWED,
                handler.unexpected(new ResponseStatusException(HttpStatus.METHOD_NOT_ALLOWED)).getStatusCode());
    }
}
