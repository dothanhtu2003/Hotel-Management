package com.hotelmanagement.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record ServiceOrderItemRequest(
        @NotNull @Positive Long hotelServiceId,
        @NotNull @Positive Integer quantity
) {
}
