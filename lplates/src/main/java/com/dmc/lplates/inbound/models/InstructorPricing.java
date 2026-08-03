package com.dmc.lplates.inbound.models;

import java.math.BigDecimal;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InstructorPricing {

    private Long id;
    private Integer durationMinutes;
    private BigDecimal price;
    private Long instructorId;

    public InstructorPricing() {
    }
}
