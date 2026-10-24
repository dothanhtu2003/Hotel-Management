package com.hotelmanagement.dto;

import java.math.BigDecimal;
import tools.jackson.databind.annotation.JsonSerialize;

public record BillPreviewResponse(
        Long bookingId,
        long nightCount,
        @JsonSerialize(using = VndSerializer.class) BigDecimal roomAmount,
        @JsonSerialize(using = VndSerializer.class) BigDecimal serviceAmount,
        @JsonSerialize(using = VndSerializer.class) BigDecimal totalAmount,
        String currency,
        boolean canCheckOut
) {
}
