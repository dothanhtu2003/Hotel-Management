package com.hotelmanagement.dto;

import java.math.BigDecimal;
import com.hotelmanagement.enums.InvoiceItemType;
import tools.jackson.databind.annotation.JsonSerialize;

public record InvoiceItemResponse(
        InvoiceItemType itemType,
        String description,
        int quantity,
        @JsonSerialize(using = VndSerializer.class) BigDecimal unitPrice,
        @JsonSerialize(using = VndSerializer.class) BigDecimal lineAmount
) {
}
