package com.dmc.lplates.inbound.models;

import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;
import java.util.List;

@Getter
@Setter
public class Instructor {


    private Long instructorId;
    private long licenseId;
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private String specialization;
    private String transmission;
    private Double rating;
    private String carMake;
    private String carModel;
    private List<String> locations;
    private Timestamp createdAt;
    private Timestamp updatedAt;
    private List<Booking> bookings;

    public Instructor() {
    }
}