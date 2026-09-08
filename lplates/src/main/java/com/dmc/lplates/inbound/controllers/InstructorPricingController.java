package com.dmc.lplates.inbound.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.dmc.lplates.inbound.models.InstructorPricing;
import com.dmc.lplates.inbound.models.Role;
import com.dmc.lplates.inbound.models.User;
import com.dmc.lplates.service.InstructorPricingServiceImpl;
import com.dmc.lplates.service.BusinessValidationService;
import com.dmc.lplates.service.ResourceAuthorizationService;

@RestController
@RequestMapping("/pricing")
public class InstructorPricingController {

    private final InstructorPricingServiceImpl pricingService;
    private final ResourceAuthorizationService authorizationService;
    private final BusinessValidationService validationService;

    @Autowired
    public InstructorPricingController(InstructorPricingServiceImpl pricingService,
                                       ResourceAuthorizationService authorizationService,
                                       BusinessValidationService validationService) {
        this.pricingService = pricingService;
        this.authorizationService = authorizationService;
        this.validationService = validationService;
    }

    @PostMapping("/create")
    public ResponseEntity<InstructorPricing> createPricing(@RequestBody InstructorPricing pricing,
                                                            Authentication authentication) {
        User currentUser = authorizationService.currentUser(authentication);
        if (currentUser.getRole() != Role.ADMIN) {
            pricing.setInstructorId(authorizationService.requireInstructorProfile(currentUser).getInstructorId());
        } else {
            authorizationService.requireOwnInstructor(pricing.getInstructorId(), currentUser);
        }
        validationService.validatePricing(pricing);
        InstructorPricing result = pricingService.createPricing(pricing);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{pricingId}")
    public ResponseEntity<InstructorPricing> getPricingById(@PathVariable Long pricingId,
                                                             Authentication authentication) {
        User currentUser = authorizationService.currentUser(authentication);
        return ResponseEntity.ok(authorizationService.requireOwnPricing(pricingId, currentUser));
    }

    @GetMapping("/instructor/{instructorId}")
    public ResponseEntity<List<InstructorPricing>> getPricingByInstructor(@PathVariable Long instructorId,
                                                                          Authentication authentication) {
        authorizationService.requireOwnInstructor(instructorId, authorizationService.currentUser(authentication));
        List<InstructorPricing> pricingList = pricingService.getPricingByInstructorId(instructorId);
        if (pricingList != null && !pricingList.isEmpty()) {
            return ResponseEntity.ok(pricingList);
        } else {
            return ResponseEntity.noContent().build();
        }
    }

    @DeleteMapping("/{pricingId}")
    public ResponseEntity<Void> deletePricing(@PathVariable Long pricingId, Authentication authentication) {
        authorizationService.requireOwnPricing(pricingId, authorizationService.currentUser(authentication));
        pricingService.deletePricing(pricingId);
        return ResponseEntity.noContent().build();
    }
}
