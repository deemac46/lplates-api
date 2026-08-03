package com.dmc.lplates.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dmc.lplates.database.repository.InstructorPricingRepository;
import com.dmc.lplates.inbound.models.InstructorPricing;

@Service
public class InstructorPricingServiceImpl implements InstructorPricingService {

    private final InstructorPricingRepository pricingRepository;

    public InstructorPricingServiceImpl(@Autowired InstructorPricingRepository pricingRepository) {
        this.pricingRepository = pricingRepository;
    }

    @Override
    public InstructorPricing createPricing(InstructorPricing pricing) {
        return pricingRepository.insertPricing(pricing);
    }

    @Override
    public InstructorPricing getPricingById(long pricingId) {
        return pricingRepository.getPricingById(pricingId);
    }

    @Override
    public List<InstructorPricing> getPricingByInstructorId(long instructorId) {
        return pricingRepository.getPricingByInstructorId(instructorId);
    }

    @Override
    public void deletePricing(long pricingId) {
        pricingRepository.deletePricing(pricingId);
    }
}
