package com.hotelmanagement.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import com.hotelmanagement.enums.RoomStatus;
import tools.jackson.databind.annotation.JsonSerialize;

public record AvailableRoomResponse(
        Long id,
        String roomNumber,
        String roomType,
        int capacity,
        @JsonSerialize(using = VndSerializer.class) BigDecimal nightlyRate,
        String currency,
        RoomStatus roomStatus,
        LocalDate checkInDate,
        LocalDate checkOutDate,
        long nightCount,
        @JsonSerialize(using = VndSerializer.class) BigDecimal roomAmount
) {
}
