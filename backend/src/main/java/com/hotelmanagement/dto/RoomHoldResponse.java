package com.hotelmanagement.dto;

import java.time.Instant;
import java.time.LocalDate;

public record RoomHoldResponse(
        String holdToken,
        Long roomId,
        LocalDate checkInDate,
        LocalDate checkOutDate,
        int guestCount,
        Instant expiresAt
) {
    @Override
    public String toString() { return "RoomHoldResponse[roomId=" + roomId + ", holdToken=[REDACTED]]"; }
}
