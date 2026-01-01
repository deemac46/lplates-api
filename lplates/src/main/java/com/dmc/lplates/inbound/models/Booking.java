package com.dmc.lplates.inbound.models;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.sql.Timestamp;

@Setter
@Getter
public class Booking {

    private Long bookingId;
    private Long instructorId;
    private Long userId;
    private Long resourceId;
    private Timestamp startTime;
    private Timestamp endTime;
    private String status;
    private BigDecimal totalPrice;
    private String currency;
    private String notes;
    private Timestamp createdAt;
    private Timestamp updatedAt;
    public Booking() {
    }


}
