package com.dmc.lplates.service;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.dmc.lplates.inbound.models.Booking;
import com.dmc.lplates.inbound.models.Instructor;
import com.dmc.lplates.inbound.models.Role;
import com.dmc.lplates.inbound.models.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;

class ResourceAuthorizationServiceTest {

    private BookingServiceImpl bookingService;
    private InstructorsServiceImpl instructorsService;
    private ResourceAuthorizationService authorizationService;

    @BeforeEach
    void setUp() {
        bookingService = mock(BookingServiceImpl.class);
        instructorsService = mock(InstructorsServiceImpl.class);
        authorizationService = new ResourceAuthorizationService(
                bookingService,
                instructorsService,
                mock(InstructorPricingServiceImpl.class),
                mock(FeedbackServiceImpl.class));
    }

    @Test
    void learnerCannotReadAnotherLearnersLesson() {
        Booking lesson = lesson(1L, 10L, 50L);
        when(bookingService.getBookingDetailsById(1L)).thenReturn(lesson);
        when(instructorsService.getInstructorById(50L)).thenReturn(instructor(50L, 20L));

        assertThrows(AccessDeniedException.class,
                () -> authorizationService.requireLessonParticipant(1L, user(30L, Role.LEARNER)));
    }

    @Test
    void assignedInstructorCanReadLesson() {
        Booking lesson = lesson(1L, 10L, 50L);
        when(bookingService.getBookingDetailsById(1L)).thenReturn(lesson);
        when(instructorsService.getInstructorById(50L)).thenReturn(instructor(50L, 20L));

        Booking authorized = authorizationService.requireLessonParticipant(1L, user(20L, Role.INSTRUCTOR));

        assertSame(lesson, authorized);
    }

    @Test
    void learnerCannotConfirmOwnLesson() {
        Booking lesson = lesson(1L, 10L, 50L);
        when(bookingService.getBookingDetailsById(1L)).thenReturn(lesson);

        assertThrows(AccessDeniedException.class,
                () -> authorizationService.requireAssignedInstructor(1L, user(10L, Role.LEARNER)));
    }

    private static Booking lesson(long lessonId, long studentId, long instructorId) {
        Booking lesson = new Booking();
        lesson.setLessonId(lessonId);
        lesson.setStudentId(studentId);
        lesson.setInstructorId(instructorId);
        return lesson;
    }

    private static Instructor instructor(long instructorId, long userId) {
        Instructor instructor = new Instructor();
        instructor.setInstructorId(instructorId);
        instructor.setUserId(userId);
        return instructor;
    }

    private static User user(long id, Role role) {
        User user = new User();
        user.setId(id);
        user.setRole(role);
        return user;
    }
}