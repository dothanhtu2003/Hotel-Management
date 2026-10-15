package com.hotelmanagement.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import com.hotelmanagement.enums.ServiceOrderStatus;
import tools.jackson.databind.annotation.JsonSerialize;

public record ServiceOrderResponse(
        Long id,
        Long bookingId,
        ServiceOrderStatus serviceOrderStatus,
        List<ServiceOrderItemResponse> items,
        @JsonSerialize(using = VndSerializer.class) BigDecimal totalAmount,
        String currency,
        Instant createdAt
) {
    public ServiceOrderResponse { if (items != null) items = List.copyOf(items); }
}
