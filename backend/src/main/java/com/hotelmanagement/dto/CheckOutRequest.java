package com.hotelmanagement.dto;

import java.math.BigDecimal;
import com.hotelmanagement.enums.PaymentMethod;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.AssertTrue;

public record CheckOutRequest(
        @NotNull @DecimalMin("0") @Digits(integer = 18, fraction = 0) BigDecimal expectedTotalAmount,
        @NotNull PaymentMethod paymentMethod,
        @NotNull @AssertTrue Boolean paymentConfirmed
) {
}
