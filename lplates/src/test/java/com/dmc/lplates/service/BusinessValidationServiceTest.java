package com.dmc.lplates.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.dmc.lplates.inbound.models.Booking;
import com.dmc.lplates.inbound.models.Instructor;
import com.dmc.lplates.inbound.models.InstructorPricing;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class BusinessValidationServiceTest {

    private BookingServiceImpl bookingService;
    private InstructorsServiceImpl instructorsService;
    private InstructorPricingServiceImpl pricingService;
    private BusinessValidationService validationService;

    @BeforeEach
    void setUp() {
        bookingService = mock(BookingServiceImpl.class);
        instructorsService = mock(InstructorsServiceImpl.class);
        pricingService = mock(InstructorPricingServiceImpl.class);
        validationService = new BusinessValidationService(
                bookingService, instructorsService, pricingService, mock(FeedbackServiceImpl.class));
    }

    @Test
    void newLessonUsesServerPricingAndInitialState() {
        Booking lesson = validFutureLesson();
        lesson.setPrice(new BigDecimal("1.00"));
        Instructor instructor = approvedInstructor();
        InstructorPricing pricing = pricing(60, "55.00");
        when(instructorsService.getInstructorById(5L)).thenReturn(instructor);
        when(pricingService.getPricingByInstructorId(5L)).thenReturn(List.of(pricing));
        when(bookingService.getLessonsByInstructorId(5L)).thenReturn(List.of());

        validationService.prepareNewLesson(lesson);

        assertEquals(new BigDecimal("55.00"), lesson.getPrice());
        assertEquals("EUR", lesson.getCurrency());
        assertEquals("pending", lesson.getStatus());
        assertEquals("unpaid", lesson.getPaymentStatus());
    }

    @Test
    void overlappingLessonIsRejected() {
        Booking candidate = validFutureLesson();
        Booking existing = validFutureLesson();
        existing.setLessonId(9L);
        existing.setStatus("confirmed");
        existing.setScheduledTime(candidate.getScheduledTime().plusMinutes(30));
        when(instructorsService.getInstructorById(5L)).thenReturn(approvedInstructor());
        when(pricingService.getPricingByInstructorId(5L)).thenReturn(List.of(pricing(60, "55.00")));
        when(bookingService.getLessonsByInstructorId(5L)).thenReturn(List.of(existing));

        assertThrows(ConflictException.class, () -> validationService.prepareNewLesson(candidate));
    }

    private static Booking validFutureLesson() {
        Booking lesson = new Booking();
        lesson.setInstructorId(5L);
        lesson.setScheduledDate(LocalDate.now().plusDays(2));
        lesson.setScheduledTime(LocalTime.of(10, 0));
        lesson.setDurationMinutes(60);
        lesson.setLessonType("lesson");
        return lesson;
    }

    private static Instructor approvedInstructor() {
        Instructor instructor = new Instructor();
        instructor.setInstructorId(5L);
        instructor.setApprovalStatus("approved");
        return instructor;
    }

    private static InstructorPricing pricing(int duration, String price) {
        InstructorPricing pricing = new InstructorPricing();
        pricing.setDurationMinutes(duration);
        pricing.setPrice(new BigDecimal(price));
        return pricing;
    }
}