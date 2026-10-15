package com.hotelmanagement.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import com.hotelmanagement.enums.BookingStatus;
import tools.jackson.databind.annotation.JsonSerialize;

public record BookingResponse(
        Long id,
        String bookingCode,
        Long guestId,
        String guestName,
        Long roomId,
        String roomNumber,
        LocalDate checkInDate,
        LocalDate checkOutDate,
        int guestCount,
        long nightCount,
        @JsonSerialize(using = VndSerializer.class) BigDecimal nightlyRate,
        @JsonSerialize(using = VndSerializer.class) BigDecimal roomAmount,
        String currency,
        BookingStatus bookingStatus,
        Instant createdAt,
        Instant checkedInAt,
        Instant checkedOutAt,
        Instant cancelledAt
) {
}
