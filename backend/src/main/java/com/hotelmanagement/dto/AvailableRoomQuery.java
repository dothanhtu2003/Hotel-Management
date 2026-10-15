package com.hotelmanagement.dto;

import java.time.LocalDate;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.format.annotation.DateTimeFormat;

public class AvailableRoomQuery extends PageQuery {
    @NotNull
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate checkInDate;
    @NotNull
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate checkOutDate;
    @NotNull @Positive
    private Integer guestCount;

    public LocalDate getCheckInDate() { return checkInDate; }
    public void setCheckInDate(LocalDate value) { checkInDate = value; }
    public LocalDate getCheckOutDate() { return checkOutDate; }
    public void setCheckOutDate(LocalDate value) { checkOutDate = value; }
    public Integer getGuestCount() { return guestCount; }
    public void setGuestCount(Integer value) { guestCount = value; }
}
