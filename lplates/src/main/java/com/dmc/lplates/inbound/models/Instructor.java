package com.dmc.lplates.inbound.models;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Instructor {

    private Long instructorId;
    private Long userId;
    private String firstName;
    private String lastName;
    private String email;
    private String approvalStatus;
    private String gender;
    private String phoneNumber;
    private String adiNumber;
    private String transmission;
    private Integer yearsExperience;
    private Double rating;
    private String carMake;
    private String carModel;
    private List<String> locations;
    private String county;
    private String areasCovered;
    private Double latitude;
    private Double longitude;
    private String profilePicture;
    private String description;
    private Integer reviewsCount;
    private Boolean agreeTerms;
    private Boolean available;
    private Boolean offersTestCarHire;
    private BigDecimal testCarHirePrice;
    private Boolean hasAdaptedVehicle;
    private String adaptedVehicleTypes;
    private String disabilityExperience;
    private Boolean disabilityTraining;
    private Timestamp createdAt;
    private Timestamp updatedAt;
    private List<Booking> lessons;

    public Instructor() {
    }
}