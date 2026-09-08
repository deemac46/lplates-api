package com.dmc.lplates.inbound.controllers;

import com.dmc.lplates.inbound.models.Booking;
import com.dmc.lplates.inbound.dtos.CompleteLessonRequest;
import com.dmc.lplates.inbound.dtos.CompleteLessonResponse;
import com.dmc.lplates.inbound.models.EdtProgress;
import com.dmc.lplates.inbound.models.Instructor;
import com.dmc.lplates.inbound.models.LessonType;
import com.dmc.lplates.inbound.models.Role;
import com.dmc.lplates.inbound.models.User;
import com.dmc.lplates.service.BookingServiceImpl;
import com.dmc.lplates.service.BusinessValidationService;
import com.dmc.lplates.service.EdtProgressService;
import com.dmc.lplates.service.ResourceAuthorizationService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/lessons")
public class BookingsController {

    BookingServiceImpl bookingService;
    EdtProgressService edtProgressService;
    ResourceAuthorizationService authorizationService;
    BusinessValidationService validationService;

    @Autowired
    public BookingsController(BookingServiceImpl bookingService,
                              EdtProgressService edtProgressService,
                              ResourceAuthorizationService authorizationService,
                              BusinessValidationService validationService) {
        this.bookingService = bookingService;
        this.edtProgressService = edtProgressService;
        this.authorizationService = authorizationService;
        this.validationService = validationService;
    }

    @GetMapping("/{lessonId}")
    public ResponseEntity<Booking> getLessonById(@PathVariable Long lessonId, Authentication authentication) {
        User currentUser = authorizationService.currentUser(authentication);
        return ResponseEntity.ok(authorizationService.requireLessonParticipant(lessonId, currentUser));
    }

    @PostMapping("/create")
    public ResponseEntity<?> createLesson(@RequestBody Booking lesson, Authentication authentication) {
        if (lesson.getLessonType() != null && !LessonType.isValid(lesson.getLessonType())) {
            return ResponseEntity.badRequest().body("Invalid lessonType. Must be one of: lesson, edt, test_car_hire");
        }
        User currentUser = (User) authentication.getPrincipal();
        lesson.setStudentId(currentUser.getId());
        validationService.prepareNewLesson(lesson);
        Booking created = bookingService.createBooking(lesson);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/")
    public ResponseEntity<List<Booking>> getAllLessons(Authentication authentication) {
        authorizationService.requireAdmin(authorizationService.currentUser(authentication));
        List<Booking> lessons = bookingService.getAllBookings();
        if (lessons != null && !lessons.isEmpty()) {
            return ResponseEntity.ok(lessons);
        } else {
            return ResponseEntity.noContent().build();
        }
    }

    @GetMapping("/pending")
    public ResponseEntity<List<Booking>> getPendingLessons(Authentication authentication) {
        authorizationService.requireAdmin(authorizationService.currentUser(authentication));
        List<Booking> lessons = bookingService.getAllPendingBookings();
        if (lessons != null && !lessons.isEmpty()) {
            return ResponseEntity.ok(lessons);
        } else {
            return ResponseEntity.noContent().build();
        }
    }

    @GetMapping("/instructor/{instructorId}")
    public ResponseEntity<List<Booking>> getLessonsByInstructor(@PathVariable Long instructorId,
                                                                 Authentication authentication) {
        authorizationService.requireOwnInstructor(instructorId, authorizationService.currentUser(authentication));
        List<Booking> lessons = bookingService.getLessonsByInstructorId(instructorId);
        if (lessons != null && !lessons.isEmpty()) {
            return ResponseEntity.ok(lessons);
        } else {
            return ResponseEntity.noContent().build();
        }
    }

    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<Booking>> getLessonsByStudent(@PathVariable Long studentId,
                                                              Authentication authentication) {
        authorizationService.requireStudent(studentId, authorizationService.currentUser(authentication));
        List<Booking> lessons = bookingService.getLessonsByStudentId(studentId);
        if (lessons != null && !lessons.isEmpty()) {
            return ResponseEntity.ok(lessons);
        } else {
            return ResponseEntity.noContent().build();
        }
    }

    @PostMapping("/confirm/{lessonId}")
    public ResponseEntity<Booking> confirmLesson(@PathVariable Long lessonId, Authentication authentication) {
        Booking existing = authorizationService.requireAssignedInstructor(
            lessonId, authorizationService.currentUser(authentication));
        validationService.validateConfirmation(existing);
        Booking lesson = bookingService.confirmBooking(lessonId);
        if (lesson != null) {
            return ResponseEntity.ok(lesson);
        } else {
            return ResponseEntity.status(500).build();
        }
    }

    @PostMapping("/{lessonId}/complete")
    public ResponseEntity<CompleteLessonResponse> completeLesson(@PathVariable Long lessonId,
                                                                 @RequestBody(required = false) CompleteLessonRequest request,
                                                                 Authentication authentication) {
        User currentUser = authorizationService.currentUser(authentication);
        Booking existing = authorizationService.requireAssignedInstructor(lessonId, currentUser);
        Integer edtModuleNumber = request != null ? request.getEdtModuleNumber() : null;
        String edtNote = request != null ? request.getEdtNote() : null;
        validationService.validateLessonCompletion(existing, edtModuleNumber);

        Long loggedByInstructorId = existing.getInstructorId();
        if (currentUser.getRole() != Role.ADMIN) {
            Instructor instructor = authorizationService.requireInstructorProfile(currentUser);
            loggedByInstructorId = instructor.getInstructorId();
        }

        Booking lesson = bookingService.completeBooking(existing, edtModuleNumber, edtNote, loggedByInstructorId);
        if (lesson == null) {
            return ResponseEntity.status(500).build();
        }
        EdtProgress progress = edtModuleNumber != null
                ? edtProgressService.getEdtProgressByStudentAndModule(lesson.getStudentId(), edtModuleNumber)
                : null;
        return ResponseEntity.ok(new CompleteLessonResponse(lesson, progress));
    }

    @PutMapping("/update/{lessonId}")
    public ResponseEntity<?> updateLesson(@PathVariable Long lessonId, @RequestBody Booking lesson,
                                          Authentication authentication) {
        if (lesson.getLessonType() != null && !LessonType.isValid(lesson.getLessonType())) {
            return ResponseEntity.badRequest().body("Invalid lessonType. Must be one of: lesson, edt, test_car_hire");
        }
        Booking existing = authorizationService.requireAssignedInstructor(
            lessonId, authorizationService.currentUser(authentication));
        lesson.setLessonId(lessonId);
        lesson.setStudentId(existing.getStudentId());
        lesson.setInstructorId(existing.getInstructorId());
        validationService.prepareLessonUpdate(existing, lesson);
        Booking updated = bookingService.updateBooking(lesson);
        if (updated != null) {
            return ResponseEntity.ok(updated);
        } else {
            return ResponseEntity.status(500).build();
        }
    }
}
