package com.hotelmanagement.dto;

import com.hotelmanagement.enums.HousekeepingStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateHousekeepingStatusRequest(
        @NotNull HousekeepingStatus housekeepingStatus
) {
}
