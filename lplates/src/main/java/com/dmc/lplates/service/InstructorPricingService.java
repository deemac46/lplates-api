package com.dmc.lplates.service;

import java.util.List;

import com.dmc.lplates.inbound.models.InstructorPricing;

public interface InstructorPricingService {

    InstructorPricing createPricing(InstructorPricing pricing);
    InstructorPricing getPricingById(long pricingId);
    List<InstructorPricing> getPricingByInstructorId(long instructorId);
    void deletePricing(long pricingId);
}
