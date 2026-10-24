package com.hotelmanagement.dto;

import java.math.BigDecimal;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ValueSerializer;

public class VndSerializer extends ValueSerializer<BigDecimal> {
    @Override
    public void serialize(BigDecimal value, JsonGenerator generator, SerializationContext context) {
        generator.writeString(value.setScale(0, java.math.RoundingMode.UNNECESSARY).toPlainString());
    }
}
