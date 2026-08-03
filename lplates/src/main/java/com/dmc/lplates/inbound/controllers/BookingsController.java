package com.dmc.lplates.inbound.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.dmc.lplates.inbound.models.Booking;
import com.dmc.lplates.inbound.models.LessonType;
import com.dmc.lplates.service.BookingServiceImpl;

@RestController
@RequestMapping("/lessons")
public class BookingsController {

    BookingServiceImpl bookingService;

    @Autowired
    public BookingsController(BookingServiceImpl bookingService) {
        this.bookingService = bookingService;
    }

    @GetMapping("/{lessonId}")
    public ResponseEntity<Booking> getLessonById(@PathVariable Long lessonId) {
        Booking lesson = bookingService.getBookingDetailsById(lessonId);
        if (lesson != null) {
            return ResponseEntity.ok(lesson);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/create")
    public ResponseEntity<String> createLesson(@RequestBody Booking lesson) {
        if (lesson.getLessonType() != null && !LessonType.isValid(lesson.getLessonType())) {
            return ResponseEntity.badRequest().body("Invalid lessonType. Must be one of: lesson, edt, test_car_hire");
        }
        String result = bookingService.createBooking(lesson);
        if (result != null) {
            return ResponseEntity.ok("Lesson created successfully with ID: " + result);
        } else {
            return ResponseEntity.status(500).body("Failed to create lesson");
        }
    }

    @GetMapping("/")
    public ResponseEntity<List<Booking>> getAllLessons() {
        List<Booking> lessons = bookingService.getAllBookings();
        if (lessons != null && !lessons.isEmpty()) {
            return ResponseEntity.ok(lessons);
        } else {
            return ResponseEntity.noContent().build();
        }
    }

    @GetMapping("/pending")
    public ResponseEntity<List<Booking>> getPendingLessons() {
        List<Booking> lessons = bookingService.getAllPendingBookings();
        if (lessons != null && !lessons.isEmpty()) {
            return ResponseEntity.ok(lessons);
        } else {
            return ResponseEntity.noContent().build();
        }
    }

    @GetMapping("/instructor/{instructorId}")
    public ResponseEntity<List<Booking>> getLessonsByInstructor(@PathVariable Long instructorId) {
        List<Booking> lessons = bookingService.getLessonsByInstructorId(instructorId);
        if (lessons != null && !lessons.isEmpty()) {
            return ResponseEntity.ok(lessons);
        } else {
            return ResponseEntity.noContent().build();
        }
    }

    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<Booking>> getLessonsByStudent(@PathVariable Long studentId) {
        List<Booking> lessons = bookingService.getLessonsByStudentId(studentId);
        if (lessons != null && !lessons.isEmpty()) {
            return ResponseEntity.ok(lessons);
        } else {
            return ResponseEntity.noContent().build();
        }
    }

    @PostMapping("/confirm/{lessonId}")
    public ResponseEntity<Booking> confirmLesson(@PathVariable Long lessonId) {
        Booking lesson = bookingService.confirmBooking(lessonId);
        if (lesson != null) {
            return ResponseEntity.ok(lesson);
        } else {
            return ResponseEntity.status(500).build();
        }
    }

    @PutMapping("/update/{lessonId}")
    public ResponseEntity<?> updateLesson(@PathVariable Long lessonId, @RequestBody Booking lesson) {
        if (lesson.getLessonType() != null && !LessonType.isValid(lesson.getLessonType())) {
            return ResponseEntity.badRequest().body("Invalid lessonType. Must be one of: lesson, edt, test_car_hire");
        }
        lesson.setLessonId(lessonId);
        Booking updated = bookingService.updateBooking(lesson);
        if (updated != null) {
            return ResponseEntity.ok(updated);
        } else {
            return ResponseEntity.status(500).build();
        }
    }
}
