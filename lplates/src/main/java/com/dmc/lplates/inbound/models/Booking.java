package com.dmc.lplates.inbound.models;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalTime;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class Booking {

    private Long lessonId;
    private LocalDate scheduledDate;
    private LocalTime scheduledTime;
    private int durationMinutes;
    private String status;
    private String paymentStatus;
    private BigDecimal price;
    private String currency;
    private String lessonType;
    private String notes;
    private String edtModule;
    private Boolean edtCompleted;
    private String cancellationReason;
    private Timestamp createdAt;
    private Timestamp updatedAt;
    private Long instructorId;
    private Long studentId;

    public Booking() {
    }
}
