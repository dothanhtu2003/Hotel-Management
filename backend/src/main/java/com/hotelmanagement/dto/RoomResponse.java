package com.hotelmanagement.dto;

import java.math.BigDecimal;
import com.hotelmanagement.enums.RoomStatus;
import tools.jackson.databind.annotation.JsonSerialize;

public record RoomResponse(
        Long id,
        String roomNumber,
        String roomType,
        int capacity,
        @JsonSerialize(using = VndSerializer.class) BigDecimal nightlyRate,
        String currency,
        RoomStatus roomStatus
) {
}
