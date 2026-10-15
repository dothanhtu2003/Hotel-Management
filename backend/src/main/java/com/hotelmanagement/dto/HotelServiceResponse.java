package com.hotelmanagement.dto;

import java.math.BigDecimal;
import tools.jackson.databind.annotation.JsonSerialize;

public record HotelServiceResponse(
        Long id,
        String name,
        @JsonSerialize(using = VndSerializer.class) BigDecimal unitPrice,
        String currency
) {
}
