package com.dmc.lplates.database.repository;

import java.util.List;

import com.dmc.lplates.inbound.models.InstructorPricing;

public interface InstructorPricingRepository {

    InstructorPricing insertPricing(InstructorPricing pricing);
    InstructorPricing getPricingById(long pricingId);
    List<InstructorPricing> getPricingByInstructorId(long instructorId);
    void deletePricing(long pricingId);
}
