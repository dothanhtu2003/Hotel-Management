package com.hotelmanagement.dto;

import java.util.List;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotEmpty;

public record CreateServiceOrderRequest(
        @NotEmpty List<@NotNull @Valid ServiceOrderItemRequest> items
) {
    public CreateServiceOrderRequest {
        if (items != null) items = java.util.Collections.unmodifiableList(new java.util.ArrayList<>(items));
    }
}
