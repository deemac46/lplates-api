package com.dmc.lplates.inbound.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.dmc.lplates.inbound.models.InstructorPricing;
import com.dmc.lplates.service.InstructorPricingServiceImpl;

@RestController
@RequestMapping("/pricing")
public class InstructorPricingController {

    private final InstructorPricingServiceImpl pricingService;

    @Autowired
    public InstructorPricingController(InstructorPricingServiceImpl pricingService) {
        this.pricingService = pricingService;
    }

    @PostMapping("/create")
    public ResponseEntity<InstructorPricing> createPricing(@RequestBody InstructorPricing pricing) {
        InstructorPricing result = pricingService.createPricing(pricing);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{pricingId}")
    public ResponseEntity<InstructorPricing> getPricingById(@PathVariable Long pricingId) {
        InstructorPricing pricing = pricingService.getPricingById(pricingId);
        if (pricing != null) {
            return ResponseEntity.ok(pricing);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/instructor/{instructorId}")
    public ResponseEntity<List<InstructorPricing>> getPricingByInstructor(@PathVariable Long instructorId) {
        List<InstructorPricing> pricingList = pricingService.getPricingByInstructorId(instructorId);
        if (pricingList != null && !pricingList.isEmpty()) {
            return ResponseEntity.ok(pricingList);
        } else {
            return ResponseEntity.noContent().build();
        }
    }

    @DeleteMapping("/{pricingId}")
    public ResponseEntity<Void> deletePricing(@PathVariable Long pricingId) {
        pricingService.deletePricing(pricingId);
        return ResponseEntity.noContent().build();
    }
}
