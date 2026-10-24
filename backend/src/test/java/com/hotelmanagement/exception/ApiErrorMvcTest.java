package com.hotelmanagement.exception;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import com.hotelmanagement.dto.CheckOutRequest;
import jakarta.validation.Valid;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.*;

class ApiErrorMvcTest {
    // Test-only endpoint to verify MVC deserialization, validation and advice together.
    // This does not claim security/session/CSRF coverage.
    @RestController
    static class ValidationEndpoint {
        @PostMapping("/validation")
        CheckOutRequest validate(@Valid @RequestBody CheckOutRequest request) { return request; }

        @GetMapping("/failures/{kind}")
        void failure(@PathVariable String kind) {
            throw switch (kind) {
                case "spring-lock" -> new org.springframework.dao.CannotAcquireLockException("private-db-data");
                case "jpa-timeout" -> new jakarta.persistence.LockTimeoutException("private-db-data");
                case "jpa-lock" -> new jakarta.persistence.PessimisticLockException("private-db-data");
                case "optimistic" -> new org.springframework.dao.OptimisticLockingFailureException("private-db-data");
                case "serialization" -> new org.springframework.dao.CannotSerializeTransactionException("private-db-data");
                case "concurrency" -> new org.springframework.dao.ConcurrencyFailureException("private-db-data");
                default -> new org.springframework.dao.DataIntegrityViolationException("private-db-data");
            };
        }
    }

    private final MockMvc mvc = MockMvcBuilders.standaloneSetup(new ValidationEndpoint())
            .setControllerAdvice(new GlobalExceptionHandler(
                    Clock.fixed(Instant.parse("2026-10-15T04:00:00Z"), ZoneOffset.UTC)))
            .build();

    @Test
    void beanValidationReturnsContractWithoutRejectedValues() throws Exception {
        mvc.perform(post("/validation").contentType(MediaType.APPLICATION_JSON)
                .content("{\"expectedTotalAmount\":\"100\",\"paymentMethod\":\"CASH\",\"paymentConfirmed\":false}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.fieldErrors[0].field").value("paymentConfirmed"))
                .andExpect(jsonPath("$.details").isMap())
                .andExpect(jsonPath("$.timestamp").value("2026-10-15T04:00:00Z"))
                .andExpect(jsonPath("$.trace").doesNotExist());
    }

    @Test
    void invalidEnumReturnsSafe400() throws Exception {
        mvc.perform(post("/validation").contentType(MediaType.APPLICATION_JSON)
                .content("{\"expectedTotalAmount\":\"100\",\"paymentMethod\":\"SECRET\",\"paymentConfirmed\":true}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("SECRET"))));
    }

    @Test
    void malformedJsonReturnsContract() throws Exception {
        mvc.perform(post("/validation").contentType(MediaType.APPLICATION_JSON).content("{"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.fieldErrors").isArray());
    }

    @ParameterizedTest
    @CsvSource({"spring-lock,409,RESOURCE_BUSY", "jpa-timeout,409,RESOURCE_BUSY", "jpa-lock,409,RESOURCE_BUSY",
            "optimistic,500,INTERNAL_ERROR", "serialization,500,INTERNAL_ERROR",
            "concurrency,500,INTERNAL_ERROR", "integrity,500,INTERNAL_ERROR"})
    void onlyKnownLockFailuresReturnResourceBusy(String kind, int status, String code) throws Exception {
        mvc.perform(get("/failures/" + kind))
                .andExpect(status().is(status))
                .andExpect(jsonPath("$.code").value(code))
                .andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("private-db-data"))));
    }
}
