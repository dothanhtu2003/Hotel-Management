package com.hotelmanagement.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import com.hotelmanagement.enums.PaymentMethod;
import com.hotelmanagement.enums.PaymentStatus;
import tools.jackson.databind.annotation.JsonSerialize;

public record InvoiceResponse(
        Long id,
        String invoiceNumber,
        Long bookingId,
        String guestName,
        String roomNumber,
        LocalDate checkInDate,
        LocalDate checkOutDate,
        List<InvoiceItemResponse> items,
        @JsonSerialize(using = VndSerializer.class) BigDecimal roomAmount,
        @JsonSerialize(using = VndSerializer.class) BigDecimal serviceAmount,
        @JsonSerialize(using = VndSerializer.class) BigDecimal totalAmount,
        String currency,
        PaymentStatus paymentStatus,
        PaymentMethod paymentMethod,
        Instant paidAt
) {
    public InvoiceResponse { if (items != null) items = List.copyOf(items); }
}
