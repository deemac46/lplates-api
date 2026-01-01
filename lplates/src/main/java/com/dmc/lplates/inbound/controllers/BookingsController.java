package com.dmc.lplates.inbound.controllers;


import com.dmc.lplates.inbound.models.Booking;
import com.dmc.lplates.inbound.models.Instructor;
import com.dmc.lplates.service.BookingService;
import com.dmc.lplates.service.BookingServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/bookings")
public class BookingsController {


    BookingServiceImpl bookingService;

    @Autowired
    public BookingsController(BookingServiceImpl bookingService) {
        this.bookingService = bookingService;
    }

    // Example endpoint to get booking information
    @GetMapping("/{bookingId}")
    public ResponseEntity<Booking> getBookingInfo(@PathVariable Long bookingId) {
        Booking booking = bookingService.getBookingDetailsById(bookingId);
        if (booking != null) {
            return ResponseEntity.ok(booking);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // Example endpoint to create a new booking
    @PostMapping("/create")
    public ResponseEntity<String> createBooking(@RequestBody Booking booking, @RequestParam Long instructorId) {

        String result = bookingService.createBooking(booking, instructorId);
        if (result != null) {
            return ResponseEntity.ok("Booking created successfully with ID: " + result);
        } else {
            return ResponseEntity.status(500).body("Failed to create booking");
        }
    }

    @GetMapping("/")
    public ResponseEntity<List<Booking>> getAllBookings() {
        List<Booking> bookings = bookingService.getAllBookings();
        if (bookings != null && !bookings.isEmpty()) {
            return ResponseEntity.ok(bookings);
        } else {
            return ResponseEntity.noContent().build();
        }
    }

    @GetMapping("/pending")
    public ResponseEntity<List<Booking>> getAllPendingBookings() {
        List<Booking> bookings = bookingService.getAllPendingBookings();
        if (bookings != null && !bookings.isEmpty()) {
            return ResponseEntity.ok(bookings);
        } else {
            return ResponseEntity.noContent().build();
        }
    }

    @PostMapping("/confirm/{bookingId}")
    public ResponseEntity<Booking> confirmBooking(@PathVariable Long bookingId) {
        Booking booking = bookingService.confirmBooking(bookingId);
        if (booking != null) {
            return ResponseEntity.ok(booking);
        } else {
            return ResponseEntity.status(500).build();
        }
    }

    // Additional endpoints can be added here
}
