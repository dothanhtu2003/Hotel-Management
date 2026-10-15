package com.hotelmanagement.contract;

import static org.junit.jupiter.api.Assertions.*;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;

class OpenApiContractTest {
    private static Map<String, Object> spec;
    private static Map<String, Object> schemas;

    @BeforeAll
    static void readContract() throws IOException {
        var options = new LoaderOptions();
        options.setAllowDuplicateKeys(false);
        try (Reader reader = Files.newBufferedReader(Path.of("..", "docs", "api-contract.yaml"), StandardCharsets.UTF_8)) {
            spec = map(new Yaml(new SafeConstructor(options)).load(reader));
            schemas = map(map(spec.get("components")).get("schemas"));
        }
    }

    @Test
    void allReferencesResolveLocally() {
        assertEquals("3.0.3", spec.get("openapi"));
        checkReferences(spec);
    }

    @Test
    void dtoFieldsMatchSchemaWithoutAddingWrappers() throws ClassNotFoundException {
        var names = List.of("RoomResponse", "AvailableRoomResponse", "CreateRoomHoldRequest", "RoomHoldResponse",
                "BookingResponse", "HotelServiceResponse", "ServiceOrderItemRequest", "CreateServiceOrderRequest",
                "ServiceOrderItemResponse", "ServiceOrderResponse", "UpdateHousekeepingStatusRequest",
                "HousekeepingRoomResponse", "CheckOutRequest", "BillPreviewResponse", "InvoiceItemResponse", "InvoiceResponse");
        for (String name : names) {
            var type = Class.forName("com.hotelmanagement.dto." + name);
            var fields = Arrays.stream(type.getRecordComponents()).map(java.lang.reflect.RecordComponent::getName)
                    .collect(Collectors.toSet());
            assertEquals(fields, map(map(schemas.get(name)).get("properties")).keySet(), name);
        }
        var guest = map(map(schemas.get("GuestCreateBookingRequest")).get("properties"));
        var frontDesk = map(map(schemas.get("FrontDeskCreateBookingRequest")).get("properties"));
        assertFalse(guest.containsKey("guestId"));
        assertTrue(frontDesk.containsKey("guestId"));
        assertTrue(list(map(schemas.get("FrontDeskCreateBookingRequest")).get("required")).contains("guestId"));
    }

    @Test
    void statusAndErrorEnumsMatchJava() throws ClassNotFoundException {
        for (String name : List.of("Role", "BookingStatus", "RoomStatus", "HousekeepingStatus", "ServiceOrderStatus",
                "PaymentStatus", "PaymentMethod", "InvoiceItemType", "ErrorCode")) {
            var packageName = name.equals("ErrorCode") ? "exception" : "enums";
            var constants = Class.forName("com.hotelmanagement." + packageName + "." + name).getEnumConstants();
            var values = Arrays.stream(constants).map(value -> ((Enum<?>) value).name()).collect(Collectors.toSet());
            assertEquals(values, new HashSet<>(list(map(schemas.get(name)).get("enum"))), name);
        }
    }

    @Test
    void operationsHaveRolesAndMutationsRequireCsrfWithSession() {
        var operationIds = new HashSet<String>();
        int m2 = 0;
        int m3 = 0;
        for (Object pathValue : map(spec.get("paths")).values()) {
            for (var entry : map(pathValue).entrySet()) {
                var operation = map(entry.getValue());
                assertTrue(operationIds.add((String) operation.get("operationId")), "Duplicate operationId");
                var roles = list(operation.get("x-roles"));
                assertFalse(roles.isEmpty());
                if (operation.get("x-owner").equals("M2")) m2++; else m3++;
                var security = list(operation.get("security"));
                if (Set.of("post", "patch").contains(entry.getKey())) {
                    assertEquals(1, security.size());
                    var requirements = map(security.getFirst());
                    assertTrue(requirements.containsKey("CsrfToken"));
                    if (!roles.contains("PUBLIC")) assertTrue(requirements.containsKey("SessionCookie"));
                }
                var responses = map(operation.get("responses"));
                if (responses.containsKey("204")) assertFalse(map(responses.get("204")).containsKey("content"));
            }
        }
        assertEquals(19, m2);
        assertEquals(9, m3);
    }

    @Test
    void moneyPagesAndBillChangedFollowContract() {
        assertEquals("string", map(schemas.get("Money")).get("type"));
        for (String name : List.of("AvailableRoomPage", "BookingPage", "HotelServicePage", "ServiceOrderPage",
                "HousekeepingRoomPage", "InvoicePage")) {
            assertEquals(Set.of("items", "page", "size", "totalElements"),
                    map(map(schemas.get(name)).get("properties")).keySet());
        }
        var params = map(map(spec.get("components")).get("parameters"));
        assertEquals(0, map(map(params.get("Page")).get("schema")).get("default"));
        assertEquals(20, map(map(params.get("Size")).get("schema")).get("default"));
        assertEquals(100, map(map(params.get("Size")).get("schema")).get("maximum"));
        var checkout = map(map(map(spec.get("paths")).get("/api/bookings/{bookingId}/check-out")).get("post"));
        var content = map(map(map(checkout.get("responses")).get("409")).get("content"));
        var examples = map(map(content.get("application/json")).get("examples"));
        var value = map(map(examples.get("billChanged")).get("value"));
        assertEquals("BILL_CHANGED", value.get("code"));
        assertInstanceOf(String.class, map(value.get("details")).get("currentTotalAmount"));
    }

    private void checkReferences(Object node) {
        if (node instanceof Map<?, ?> values) {
            if (values.containsKey("$ref")) {
                String reference = (String) values.get("$ref");
                assertTrue(reference.startsWith("#/components/"), reference);
                Object target = spec;
                for (String segment : reference.substring(2).split("/")) {
                    target = map(target).get(segment.replace("~1", "/").replace("~0", "~"));
                    assertNotNull(target, reference);
                }
            }
            values.values().forEach(this::checkReferences);
        } else if (node instanceof List<?> values) {
            values.forEach(this::checkReferences);
        }
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> map(Object value) { return (Map<String, Object>) value; }
    @SuppressWarnings("unchecked")
    private static List<Object> list(Object value) { return (List<Object>) value; }
}
