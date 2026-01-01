package com.dmc.lplates.inbound.controllers;

import com.dmc.lplates.inbound.models.Booking;
import com.dmc.lplates.inbound.models.Instructor;
import com.dmc.lplates.service.InstructorsServiceImpl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/instructors")
public class InstructorsController {

    InstructorsServiceImpl instructorsService;

    public InstructorsController(InstructorsServiceImpl instructorsService) {
        this.instructorsService = instructorsService;
    }

    @GetMapping("/{instructorId}")
    public ResponseEntity<Instructor> getInstructorInfo(@PathVariable Long instructorId) {
        Instructor instructor = instructorsService.getInstructorById(instructorId);
        if (instructor != null) {
            return ResponseEntity.ok(instructor);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/")
    public ResponseEntity<List<Instructor>> getInstructors() {
        List<Instructor> instructors = instructorsService.getAllInstructors();
        if (instructors != null) {
            return ResponseEntity.ok(instructors);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/create")
    public ResponseEntity<String> createInstructor(@RequestBody Instructor instructor) {
        String result = instructorsService.createInstructor(instructor);
        if (result != null) {
            return ResponseEntity.ok("Instructor created successfully with ID: " + result);
        } else {
            return ResponseEntity.status(500).body("Failed to create Instructor");
        }
    }

    @GetMapping("/{instructorId}/bookings")
    public ResponseEntity<Instructor> getBookingsForInstructor(@PathVariable Long instructorId) {
        Instructor instructor = instructorsService.getInstructorWithBookings(instructorId);
        if (instructor != null) {

            return ResponseEntity.ok(instructor);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

}
