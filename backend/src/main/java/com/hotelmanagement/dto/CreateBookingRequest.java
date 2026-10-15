package com.hotelmanagement.dto;

import java.time.LocalDate;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSetter;
import jakarta.validation.constraints.*;

public class CreateBookingRequest {
    @NotNull @Positive private Long roomId;
    @NotBlank private String holdToken;
    @NotNull private LocalDate checkInDate;
    @NotNull private LocalDate checkOutDate;
    @NotNull @Positive private Integer guestCount;
    @Positive private Long guestId;
    private boolean guestIdProvided;

    public Long getRoomId() { return roomId; }
    public void setRoomId(Long value) { roomId = value; }
    public String getHoldToken() { return holdToken; }
    public void setHoldToken(String value) { holdToken = value; }
    public LocalDate getCheckInDate() { return checkInDate; }
    public void setCheckInDate(LocalDate value) { checkInDate = value; }
    public LocalDate getCheckOutDate() { return checkOutDate; }
    public void setCheckOutDate(LocalDate value) { checkOutDate = value; }
    public Integer getGuestCount() { return guestCount; }
    public void setGuestCount(Integer value) { guestCount = value; }
    public Long getGuestId() { return guestId; }
    @JsonSetter("guestId")
    public void setGuestId(Long value) { guestIdProvided = true; guestId = value; }
    @JsonIgnore
    public boolean isGuestIdProvided() { return guestIdProvided; }

    // No generated toString: never expose the hold token in logs.
}
