package com.hotelmanagement.policy;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import com.hotelmanagement.enums.BookingStatus;
import com.hotelmanagement.enums.RoomStatus;
import com.hotelmanagement.exception.BusinessException;
import com.hotelmanagement.exception.ErrorCode;
import org.springframework.stereotype.Component;

@Component
public class RoomAvailabilityPolicy {
    private final Clock clock;
    public RoomAvailabilityPolicy(Clock clock) { this.clock = clock; }

    public void validateStay(LocalDate checkInDate, LocalDate checkOutDate, int guestCount) {
        if (checkInDate == null || checkOutDate == null || !checkOutDate.isAfter(checkInDate)) {
            throw new BusinessException(ErrorCode.INVALID_DATE_RANGE, "Ngày trả phải sau ngày nhận.");
        }
        if (checkInDate.isBefore(LocalDate.now(clock))) {
            throw new BusinessException(ErrorCode.CHECK_IN_DATE_IN_PAST, "Ngày nhận không được trước hôm nay.");
        }
        if (guestCount <= 0) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "Số khách phải là số nguyên dương.");
        }
    }

    public boolean overlaps(LocalDate existingStart, LocalDate existingEnd,
                            LocalDate requestedStart, LocalDate requestedEnd) {
        return existingStart.isBefore(requestedEnd) && existingEnd.isAfter(requestedStart);
    }

    public boolean blocksAvailability(BookingStatus status) {
        return status == BookingStatus.CONFIRMED || status == BookingStatus.CHECKED_IN;
    }

    public void validateRoom(RoomStatus status, int capacity, int guestCount) {
        if (status != RoomStatus.ACTIVE) {
            throw new BusinessException(ErrorCode.ROOM_OUT_OF_SERVICE, "Phòng không hoạt động.");
        }
        if (guestCount <= 0) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "Số khách phải là số nguyên dương.");
        }
        if (guestCount > capacity) {
            throw new BusinessException(ErrorCode.CAPACITY_EXCEEDED, "Số khách vượt sức chứa phòng.");
        }
    }

    public long nightCount(LocalDate checkInDate, LocalDate checkOutDate) {
        if (checkInDate == null || checkOutDate == null || !checkOutDate.isAfter(checkInDate)) {
            throw new BusinessException(ErrorCode.INVALID_DATE_RANGE, "Ngày trả phải sau ngày nhận.");
        }
        return ChronoUnit.DAYS.between(checkInDate, checkOutDate);
    }

    // Historical bookings may start before today, so pricing does not call validateStay.
    public BigDecimal roomAmount(LocalDate checkInDate, LocalDate checkOutDate, BigDecimal nightlyRate) {
        Objects.requireNonNull(nightlyRate, "nightlyRate");
        if (nightlyRate.signum() < 0) {
            throw new IllegalArgumentException("Nightly rate must be non-negative");
        }
        return nightlyRate.setScale(0, java.math.RoundingMode.UNNECESSARY)
                .multiply(BigDecimal.valueOf(nightCount(checkInDate, checkOutDate)));
    }
}
