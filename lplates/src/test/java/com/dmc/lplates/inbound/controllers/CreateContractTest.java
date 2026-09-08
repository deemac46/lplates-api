package com.dmc.lplates.inbound.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import com.dmc.lplates.inbound.dtos.AvailabilityDto;
import com.dmc.lplates.inbound.dtos.CompleteLessonRequest;
import com.dmc.lplates.inbound.dtos.CompleteLessonResponse;
import com.dmc.lplates.inbound.models.Booking;
import com.dmc.lplates.inbound.models.EdtProgress;
import com.dmc.lplates.inbound.models.Instructor;
import com.dmc.lplates.inbound.models.Role;
import com.dmc.lplates.inbound.models.User;
import com.dmc.lplates.service.BookingServiceImpl;
import com.dmc.lplates.service.BusinessValidationService;
import com.dmc.lplates.service.EdtProgressService;
import com.dmc.lplates.service.InstructorsServiceImpl;
import com.dmc.lplates.service.ResourceAuthorizationService;

class CreateContractTest {

    @Test
    void createInstructorReturnsResourceAndUsesAuthenticatedUserId() {
        InstructorsServiceImpl instructorsService = mock(InstructorsServiceImpl.class);
        ResourceAuthorizationService authorizationService = mock(ResourceAuthorizationService.class);
        BusinessValidationService validationService = mock(BusinessValidationService.class);
        InstructorsController controller = new InstructorsController(
                instructorsService, authorizationService, validationService);
        User user = user(42L, Role.INSTRUCTOR);
        Authentication authentication = authentication(user);
        Instructor request = new Instructor();
        request.setUserId(999L);
        Instructor created = new Instructor();
        created.setInstructorId(7L);
        created.setUserId(42L);
        when(instructorsService.createInstructor(any(Instructor.class))).thenReturn(created);

        ResponseEntity<Instructor> response = controller.createInstructor(request, authentication);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertSame(created, response.getBody());
        ArgumentCaptor<Instructor> captor = ArgumentCaptor.forClass(Instructor.class);
        verify(instructorsService).createInstructor(captor.capture());
        assertEquals(42L, captor.getValue().getUserId());
        verify(validationService).validateNewInstructor(42L);
    }

    @Test
    void createLessonReturnsResourceAndUsesAuthenticatedStudentId() {
        BookingServiceImpl bookingService = mock(BookingServiceImpl.class);
        ResourceAuthorizationService authorizationService = mock(ResourceAuthorizationService.class);
        BusinessValidationService validationService = mock(BusinessValidationService.class);
        BookingsController controller = new BookingsController(
            bookingService, mock(EdtProgressService.class), authorizationService, validationService);
        User user = user(21L, Role.LEARNER);
        Authentication authentication = authentication(user);
        Booking request = new Booking();
        request.setStudentId(999L);
        Booking created = new Booking();
        created.setLessonId(11L);
        created.setStudentId(21L);
        when(bookingService.createBooking(any(Booking.class))).thenReturn(created);

        ResponseEntity<?> response = controller.createLesson(request, authentication);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertSame(created, response.getBody());
        ArgumentCaptor<Booking> captor = ArgumentCaptor.forClass(Booking.class);
        verify(bookingService).createBooking(captor.capture());
        assertEquals(21L, captor.getValue().getStudentId());
        verify(validationService).prepareNewLesson(request);
    }

    @Test
    void completeLessonAllowsNullableEdtModuleAndNote() {
        BookingServiceImpl bookingService = mock(BookingServiceImpl.class);
        EdtProgressService edtProgressService = mock(EdtProgressService.class);
        ResourceAuthorizationService authorizationService = mock(ResourceAuthorizationService.class);
        BusinessValidationService validationService = mock(BusinessValidationService.class);
        BookingsController controller = new BookingsController(
                bookingService, edtProgressService, authorizationService, validationService);
        User admin = user(1L, Role.ADMIN);
        Authentication authentication = authentication(admin);
        Booking existing = confirmedLesson();
        Booking completed = confirmedLesson();
        completed.setStatus("completed");
        when(authorizationService.currentUser(authentication)).thenReturn(admin);
        when(authorizationService.requireAssignedInstructor(11L, admin)).thenReturn(existing);
        when(bookingService.completeBooking(existing, null, null, 5L)).thenReturn(completed);

        ResponseEntity<CompleteLessonResponse> response = controller.completeLesson(11L, null, authentication);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        CompleteLessonResponse body = response.getBody();
        assertNotNull(body);
        assertSame(completed, body.getLesson());
        assertNull(body.getEdtProgress());
        verify(validationService).validateLessonCompletion(existing, null);
        verify(bookingService).completeBooking(existing, null, null, 5L);
    }

    @Test
    void completeLessonLogsEdtProgressWithServerDerivedInstructor() {
        BookingServiceImpl bookingService = mock(BookingServiceImpl.class);
        EdtProgressService edtProgressService = mock(EdtProgressService.class);
        ResourceAuthorizationService authorizationService = mock(ResourceAuthorizationService.class);
        BusinessValidationService validationService = mock(BusinessValidationService.class);
        BookingsController controller = new BookingsController(
                bookingService, edtProgressService, authorizationService, validationService);
        User instructorUser = user(50L, Role.INSTRUCTOR);
        Authentication authentication = authentication(instructorUser);
        Booking existing = confirmedLesson();
        existing.setLessonType("edt");
        existing.setEdtModule("edt_03");
        Booking completed = confirmedLesson();
        completed.setStatus("completed");
        EdtProgress progress = new EdtProgress();
        progress.setModuleNumber(3);
        Instructor instructor = new Instructor();
        instructor.setInstructorId(5L);
        CompleteLessonRequest request = new CompleteLessonRequest();
        request.setEdtModuleNumber(3);
        request.setEdtNote("Covered changing direction.");
        when(authorizationService.currentUser(authentication)).thenReturn(instructorUser);
        when(authorizationService.requireAssignedInstructor(11L, instructorUser)).thenReturn(existing);
        when(authorizationService.requireInstructorProfile(instructorUser)).thenReturn(instructor);
        when(bookingService.completeBooking(existing, 3, "Covered changing direction.", 5L)).thenReturn(completed);
        when(edtProgressService.getEdtProgressByStudentAndModule(21L, 3)).thenReturn(progress);

        ResponseEntity<CompleteLessonResponse> response = controller.completeLesson(11L, request, authentication);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        CompleteLessonResponse body = response.getBody();
        assertNotNull(body);
        assertSame(completed, body.getLesson());
        assertSame(progress, body.getEdtProgress());
        verify(validationService).validateLessonCompletion(existing, 3);
        verify(bookingService).completeBooking(existing, 3, "Covered changing direction.", 5L);
    }

    @Test
    void updateAvailabilityAllowsOwningInstructorToToggle() {
        InstructorsServiceImpl instructorsService = mock(InstructorsServiceImpl.class);
        ResourceAuthorizationService authorizationService = mock(ResourceAuthorizationService.class);
        InstructorsController controller = new InstructorsController(
                instructorsService, authorizationService, mock(BusinessValidationService.class));
        User user = user(42L, Role.INSTRUCTOR);
        Authentication authentication = authentication(user);
        Instructor existing = new Instructor();
        existing.setInstructorId(7L);
        existing.setUserId(42L);
        Instructor updated = new Instructor();
        updated.setInstructorId(7L);
        updated.setAvailable(false);
        AvailabilityDto request = new AvailabilityDto();
        request.setAvailable(false);
        when(authorizationService.currentUser(authentication)).thenReturn(user);
        when(authorizationService.requireOwnInstructor(7L, user)).thenReturn(existing);
        when(instructorsService.updateAvailability(7L, false)).thenReturn(updated);

        ResponseEntity<?> response = controller.updateAvailability(7L, request, authentication);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(updated, response.getBody());
        verify(instructorsService).updateAvailability(7L, false);
    }

    @Test
    void updateAvailabilityRejectsMissingValue() {
        InstructorsServiceImpl instructorsService = mock(InstructorsServiceImpl.class);
        ResourceAuthorizationService authorizationService = mock(ResourceAuthorizationService.class);
        InstructorsController controller = new InstructorsController(
                instructorsService, authorizationService, mock(BusinessValidationService.class));

        ResponseEntity<?> response = controller.updateAvailability(7L, new AvailabilityDto(), authentication(user(42L, Role.INSTRUCTOR)));

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    private static Booking confirmedLesson() {
        Booking lesson = new Booking();
        lesson.setLessonId(11L);
        lesson.setStudentId(21L);
        lesson.setInstructorId(5L);
        lesson.setStatus("confirmed");
        lesson.setLessonType("lesson");
        return lesson;
    }

    private static User user(long id, Role role) {
        User user = new User();
        user.setId(id);
        user.setRole(role);
        return user;
    }

    private static Authentication authentication(User user) {
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(user);
        return authentication;
    }
}