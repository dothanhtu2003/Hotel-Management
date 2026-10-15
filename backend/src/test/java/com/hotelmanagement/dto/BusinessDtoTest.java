package com.hotelmanagement.dto;

import static org.junit.jupiter.api.Assertions.*;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import com.hotelmanagement.enums.*;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.*;
import tools.jackson.databind.json.JsonMapper;

class BusinessDtoTest {
    private static jakarta.validation.ValidatorFactory factory;
    private static Validator validator;
    private final JsonMapper mapper = JsonMapper.builder().findAndAddModules().build();

    @BeforeAll
    static void setup() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }
    @AfterAll
    static void close() { factory.close(); }

    @Test
    void serializesVndAsPlainIntegerString() {
        var dto = new HotelServiceResponse(1L, "Laundry", new BigDecimal("1.2000E+6"), "VND");
        var json = mapper.readTree(mapper.writeValueAsString(dto));
        assertTrue(json.get("unitPrice").isString());
        assertEquals("1200000", json.get("unitPrice").asString());
    }

    @Test
    void preservesIsOccupiedFieldName() {
        var dto = new HousekeepingRoomResponse(1L, "101", RoomStatus.ACTIVE, HousekeepingStatus.CLEAN, true);
        var json = mapper.readTree(mapper.writeValueAsString(dto));
        assertTrue(json.has("isOccupied"));
        assertFalse(json.has("occupied"));
    }

    @Test
    void distinguishesAbsentGuestIdFromExplicitNull() {
        assertFalse(mapper.readValue("{}", CreateBookingRequest.class).isGuestIdProvided());
        assertTrue(mapper.readValue("{\"guestId\":null}", CreateBookingRequest.class).isGuestIdProvided());
        assertEquals(7L, mapper.readValue("{\"guestId\":7}", CreateBookingRequest.class).getGuestId());
    }

    @Test
    void validatesNestedItemsIncludingNullElements() {
        assertFalse(validator.validate(new CreateServiceOrderRequest(List.of())).isEmpty());
        assertFalse(validator.validate(new CreateServiceOrderRequest(
                List.of(new ServiceOrderItemRequest(1L, 0)))).isEmpty());
        assertFalse(validator.validate(new CreateServiceOrderRequest(Arrays.asList((ServiceOrderItemRequest) null))).isEmpty());
    }

    @Test
    void checkoutRequiresConfirmationAndNonNegativeWholeMoney() {
        assertTrue(validator.validate(new CheckOutRequest(new BigDecimal("1200000"), PaymentMethod.CASH, true)).isEmpty());
        assertFalse(validator.validate(new CheckOutRequest(BigDecimal.ZERO, PaymentMethod.CASH, false)).isEmpty());
        assertFalse(validator.validate(new CheckOutRequest(new BigDecimal("-1"), PaymentMethod.CASH, true)).isEmpty());
        assertFalse(validator.validate(new CheckOutRequest(new BigDecimal("1.5"), PaymentMethod.CASH, true)).isEmpty());
        assertFalse(validator.validate(new CheckOutRequest(null, null, null)).isEmpty());
    }

    @Test
    void pageDefaultsAndBoundsMatchContract() {
        var query = new PageQuery();
        assertEquals(0, query.getPage());
        assertEquals(20, query.getSize());
        query.setSize(101);
        assertFalse(validator.validate(query).isEmpty());
        query.setSize(100);
        query.setPage(-1);
        assertFalse(validator.validate(query).isEmpty());
    }

    @Test
    void holdResponseDoesNotExposeTokenInToString() {
        var dto = new RoomHoldResponse("secret-value", 1L, null, null, 1, null);
        assertFalse(dto.toString().contains("secret-value"));
    }
}
