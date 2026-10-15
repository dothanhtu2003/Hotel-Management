package com.hotelmanagement.dto;

import java.math.BigDecimal;
import tools.jackson.databind.annotation.JsonSerialize;

public record ServiceOrderItemResponse(
        Long id,
        Long hotelServiceId,
        String serviceName,
        int quantity,
        @JsonSerialize(using = VndSerializer.class) BigDecimal unitPrice,
        @JsonSerialize(using = VndSerializer.class) BigDecimal lineAmount
) {
}
