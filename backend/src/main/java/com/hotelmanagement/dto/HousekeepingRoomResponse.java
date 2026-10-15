package com.hotelmanagement.dto;

import com.hotelmanagement.enums.RoomStatus;
import com.hotelmanagement.enums.HousekeepingStatus;

public record HousekeepingRoomResponse(
        Long roomId,
        String roomNumber,
        RoomStatus roomStatus,
        HousekeepingStatus housekeepingStatus,
        boolean isOccupied
) {
}
