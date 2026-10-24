package com.hotelmanagement.policy;

import static org.junit.jupiter.api.Assertions.*;
import java.math.BigDecimal;
import java.time.*;
import com.hotelmanagement.config.BusinessTimeConfiguration;
import com.hotelmanagement.enums.*;
import com.hotelmanagement.exception.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;

class RoomAvailabilityPolicyTest {
    private final Clock clock = Clock.fixed(Instant.parse("2026-10-14T17:30:00Z"),
            ZoneId.of("Asia/Ho_Chi_Minh"));
    private final RoomAvailabilityPolicy policy = new RoomAvailabilityPolicy(clock);

    @ParameterizedTest
    @CsvSource({
        "2026-11-01,2026-11-03,true",
        "2026-11-02,2026-11-04,true",
        "2026-10-31,2026-11-02,true",
        "2026-11-02,2026-11-03,true",
        "2026-10-31,2026-11-04,true",
        "2026-11-01,2026-11-02,true",
        "2026-10-31,2026-11-03,true",
        "2026-11-03,2026-11-05,false",
        "2026-10-30,2026-11-01,false"
    })
    void usesHalfOpenDateIntervals(LocalDate start, LocalDate end, boolean expected) {
        assertEquals(expected, policy.overlaps(LocalDate.of(2026, 11, 1),
                LocalDate.of(2026, 11, 3), start, end));
    }

    @ParameterizedTest
    @CsvSource({"2026-10-15,2026-10-15", "2026-10-16,2026-10-15"})
    void rejectsEmptyOrReversedStay(LocalDate start, LocalDate end) {
        assertCode(ErrorCode.INVALID_DATE_RANGE, () -> policy.validateStay(start, end, 1));
    }

    @Test
    void usesVietnamDayRatherThanUtcDay() {
        assertEquals(ZoneId.of("Asia/Ho_Chi_Minh"), new BusinessTimeConfiguration().businessClock().getZone());
        assertCode(ErrorCode.CHECK_IN_DATE_IN_PAST, () ->
                policy.validateStay(LocalDate.of(2026, 10, 14), LocalDate.of(2026, 10, 16), 1));
        assertDoesNotThrow(() ->
                policy.validateStay(LocalDate.of(2026, 10, 15), LocalDate.of(2026, 10, 16), 1));
    }

    @Test
    void rejectsNonPositiveGuestCount() {
        assertCode(ErrorCode.VALIDATION_ERROR, () ->
                policy.validateStay(LocalDate.of(2026, 10, 15), LocalDate.of(2026, 10, 16), 0));
    }

    @ParameterizedTest
    @EnumSource(BookingStatus.class)
    void onlyConfirmedAndCheckedInBlockAvailability(BookingStatus status) {
        assertEquals(status == BookingStatus.CONFIRMED || status == BookingStatus.CHECKED_IN,
                policy.blocksAvailability(status));
    }

    @Test
    void validatesCapacityAndRoomStatus() {
        assertDoesNotThrow(() -> policy.validateRoom(RoomStatus.ACTIVE, 2, 2));
        assertCode(ErrorCode.CAPACITY_EXCEEDED, () -> policy.validateRoom(RoomStatus.ACTIVE, 2, 3));
        assertCode(ErrorCode.ROOM_OUT_OF_SERVICE, () -> policy.validateRoom(RoomStatus.OUT_OF_SERVICE, 2, 1));
    }

    @Test
    void calculatesFullBookedAmountForHistoricalStayWithoutRounding() {
        var start = LocalDate.of(2026, 10, 1);
        var end = LocalDate.of(2026, 10, 3);
        assertEquals(2, policy.nightCount(start, end));
        assertEquals(new BigDecimal("1200000"), policy.roomAmount(start, end, new BigDecimal("600000.00")));
        assertThrows(ArithmeticException.class, () -> policy.roomAmount(start, end, new BigDecimal("1.5")));
    }

    @Test
    void rejectsMissingDatesAndInvalidHistoricalRange() {
        var date = LocalDate.of(2026, 10, 15);
        assertCode(ErrorCode.INVALID_DATE_RANGE, () -> policy.validateStay(null, date, 1));
        assertCode(ErrorCode.INVALID_DATE_RANGE, () -> policy.validateStay(date, null, 1));
        assertCode(ErrorCode.INVALID_DATE_RANGE, () -> policy.nightCount(date, date));
        assertCode(ErrorCode.INVALID_DATE_RANGE, () -> policy.nightCount(date, date.minusDays(1)));
    }

    @ParameterizedTest
    @CsvSource({"2028-02-28,2028-03-01,2", "2026-12-31,2027-01-01,1", "2026-10-15,2026-10-16,1"})
    void calculatesNightsAcrossCalendarBoundaries(LocalDate start, LocalDate end, long nights) {
        assertEquals(nights, policy.nightCount(start, end));
        assertEquals(BigDecimal.valueOf(nights * 150000L), policy.roomAmount(start, end, new BigDecimal("150000")));
    }

    @Test
    void handlesFreeRoomsAndRejectsInvalidDatabasePrices() {
        var date = LocalDate.of(2026, 10, 15);
        assertEquals(BigDecimal.ZERO, policy.roomAmount(date, date.plusDays(2), BigDecimal.ZERO));
        assertThrows(IllegalArgumentException.class, () -> policy.roomAmount(date, date.plusDays(1), new BigDecimal("-1")));
        assertThrows(NullPointerException.class, () -> policy.roomAmount(date, date.plusDays(1), null));
    }

    @ParameterizedTest
    @CsvSource({"0", "-1"})
    void rejectsNonPositiveCountEvenWhenRoomHasCapacity(int count) {
        assertCode(ErrorCode.VALIDATION_ERROR, () -> policy.validateRoom(RoomStatus.ACTIVE, 2, count));
    }

    private void assertCode(ErrorCode expected, org.junit.jupiter.api.function.Executable action) {
        assertEquals(expected, assertThrows(BusinessException.class, action).code());
    }
}
