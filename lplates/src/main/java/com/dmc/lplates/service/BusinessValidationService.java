package com.dmc.lplates.service;

import com.dmc.lplates.inbound.models.Booking;
import com.dmc.lplates.inbound.models.EdtProgress;
import com.dmc.lplates.inbound.models.Feedback;
import com.dmc.lplates.inbound.models.Instructor;
import com.dmc.lplates.inbound.models.InstructorPricing;
import com.dmc.lplates.inbound.models.LessonType;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Service;

@Service
public class BusinessValidationService {

    private static final Set<Integer> ALLOWED_DURATIONS = Set.of(30, 45, 60, 90, 120);
    private static final Map<String, Set<String>> ALLOWED_STATUS_TRANSITIONS = Map.of(
            "pending", Set.of("pending", "confirmed", "declined", "cancelled"),
            "confirmed", Set.of("confirmed", "completed", "cancelled"),
            "completed", Set.of("completed"),
            "declined", Set.of("declined"),
            "cancelled", Set.of("cancelled")
    );

    private final BookingServiceImpl bookingService;
    private final InstructorsServiceImpl instructorsService;
    private final InstructorPricingServiceImpl pricingService;
    private final FeedbackServiceImpl feedbackService;

    public BusinessValidationService(BookingServiceImpl bookingService,
                                     InstructorsServiceImpl instructorsService,
                                     InstructorPricingServiceImpl pricingService,
                                     FeedbackServiceImpl feedbackService) {
        this.bookingService = bookingService;
        this.instructorsService = instructorsService;
        this.pricingService = pricingService;
        this.feedbackService = feedbackService;
    }

    public void prepareNewLesson(Booking lesson) {
        validateLessonFields(lesson, true);
        Instructor instructor = instructorsService.getInstructorById(lesson.getInstructorId());
        if (instructor == null) {
            throw new ResourceNotFoundException("Instructor not found");
        }
        if (!"approved".equalsIgnoreCase(instructor.getApprovalStatus())) {
            throw new ConflictException("Instructor is not approved for bookings");
        }

        lesson.setPrice(resolvePrice(lesson, instructor));
        lesson.setCurrency("EUR");
        lesson.setStatus("pending");
        lesson.setPaymentStatus("unpaid");
        lesson.setEdtCompleted(false);
        rejectBookingConflict(lesson, null);
    }

    public void validateNewInstructor(long userId) {
        if (instructorsService.getInstructorByUserId(userId) != null) {
            throw new ConflictException("An instructor profile already exists for this user");
        }
    }

    public void prepareLessonUpdate(Booking existing, Booking proposed) {
        validateLessonFields(proposed, false);
        validateStatusTransition(existing.getStatus(), proposed.getStatus());
        proposed.setPrice(existing.getPrice());
        proposed.setCurrency(existing.getCurrency());
        proposed.setPaymentStatus(existing.getPaymentStatus());
        rejectBookingConflict(proposed, existing.getLessonId());
    }

    public void validateConfirmation(Booking lesson) {
        if (!"pending".equalsIgnoreCase(lesson.getStatus())) {
            throw new ConflictException("Only pending lessons can be confirmed");
        }
    }

    public void validateLessonCompletion(Booking lesson, Integer edtModuleNumber) {
        validateStatusTransition(lesson.getStatus(), "completed");
        if (edtModuleNumber == null) {
            return;
        }
        EdtProgress progress = new EdtProgress();
        progress.setModuleNumber(edtModuleNumber);
        validateEdtModule(progress, lesson);
    }

    public void validatePricing(InstructorPricing pricing) {
        if (pricing.getInstructorId() == null) {
            throw new IllegalArgumentException("instructorId is required");
        }
        if (pricing.getDurationMinutes() == null || !ALLOWED_DURATIONS.contains(pricing.getDurationMinutes())) {
            throw new IllegalArgumentException("durationMinutes must be one of: 30, 45, 60, 90, 120");
        }
        if (pricing.getPrice() == null || pricing.getPrice().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("price must be zero or greater");
        }
        boolean duplicate = pricingService.getPricingByInstructorId(pricing.getInstructorId()).stream()
                .anyMatch(existing -> existing.getDurationMinutes().equals(pricing.getDurationMinutes()));
        if (duplicate) {
            throw new ConflictException("A pricing tier already exists for this duration");
        }
    }

    public void validateFeedback(Feedback feedback, Booking lesson) {
        if (feedback.getRating() == null || feedback.getRating() < 1 || feedback.getRating() > 5) {
            throw new IllegalArgumentException("rating must be between 1 and 5");
        }
        if (!lesson.getStudentId().equals(feedback.getAuthorId())) {
            throw new IllegalArgumentException("Only the lesson learner may submit feedback");
        }
        if (!"completed".equalsIgnoreCase(lesson.getStatus())) {
            throw new ConflictException("Feedback may only be submitted for a completed lesson");
        }
        if (feedbackService.getFeedbackByLessonId(lesson.getLessonId()) != null) {
            throw new ConflictException("Feedback already exists for this lesson");
        }
    }

    public void validateEdtProgress(EdtProgress progress, Booking lesson) {
        validateEdtModule(progress, lesson);
        if (!"completed".equalsIgnoreCase(lesson.getStatus())) {
            throw new ConflictException("The EDT lesson must be completed first");
        }
    }

    private void validateEdtModule(EdtProgress progress, Booking lesson) {
        if (progress.getModuleNumber() == null || progress.getModuleNumber() < 1 || progress.getModuleNumber() > 12) {
            throw new IllegalArgumentException("moduleNumber must be between 1 and 12");
        }
        if (!"edt".equalsIgnoreCase(lesson.getLessonType())) {
            throw new ConflictException("EDT progress requires an EDT lesson");
        }
        if (lesson.getEdtModule() != null && !lesson.getEdtModule().isBlank()) {
            String expectedModule = String.format("edt_%02d", progress.getModuleNumber());
            if (!expectedModule.equalsIgnoreCase(lesson.getEdtModule())) {
                throw new ConflictException("moduleNumber does not match the lesson EDT module");
            }
        }
    }

    private void validateLessonFields(Booking lesson, boolean requireFuture) {
        if (lesson.getInstructorId() == null) {
            throw new IllegalArgumentException("instructorId is required");
        }
        if (lesson.getScheduledDate() == null || lesson.getScheduledTime() == null) {
            throw new IllegalArgumentException("scheduledDate and scheduledTime are required");
        }
        if (requireFuture && !LocalDateTime.of(lesson.getScheduledDate(), lesson.getScheduledTime())
                .isAfter(LocalDateTime.now())) {
            throw new IllegalArgumentException("Lesson must be scheduled in the future");
        }
        if (!ALLOWED_DURATIONS.contains(lesson.getDurationMinutes())) {
            throw new IllegalArgumentException("durationMinutes must be one of: 30, 45, 60, 90, 120");
        }
        if (!LessonType.isValid(lesson.getLessonType())) {
            throw new IllegalArgumentException("lessonType must be one of: lesson, edt, test_car_hire");
        }
    }

    private BigDecimal resolvePrice(Booking lesson, Instructor instructor) {
        if ("test_car_hire".equalsIgnoreCase(lesson.getLessonType())) {
            if (!Boolean.TRUE.equals(instructor.getOffersTestCarHire()) || instructor.getTestCarHirePrice() == null) {
                throw new ConflictException("Instructor does not offer test car hire");
            }
            return instructor.getTestCarHirePrice();
        }
        return pricingService.getPricingByInstructorId(lesson.getInstructorId()).stream()
                .filter(pricing -> pricing.getDurationMinutes().equals(lesson.getDurationMinutes()))
                .map(InstructorPricing::getPrice)
                .findFirst()
                .orElseThrow(() -> new ConflictException("No instructor pricing exists for this duration"));
    }

    private void rejectBookingConflict(Booking candidate, Long ignoredLessonId) {
        LocalDateTime candidateStart = LocalDateTime.of(candidate.getScheduledDate(), candidate.getScheduledTime());
        LocalDateTime candidateEnd = candidateStart.plusMinutes(candidate.getDurationMinutes());
        List<Booking> lessons = bookingService.getLessonsByInstructorId(candidate.getInstructorId());
        boolean conflict = lessons.stream()
                .filter(existing -> ignoredLessonId == null || !ignoredLessonId.equals(existing.getLessonId()))
                .filter(existing -> Set.of("pending", "confirmed").contains(existing.getStatus().toLowerCase()))
                .filter(existing -> existing.getScheduledDate() != null && existing.getScheduledTime() != null)
                .anyMatch(existing -> {
                    LocalDateTime existingStart = LocalDateTime.of(existing.getScheduledDate(), existing.getScheduledTime());
                    LocalDateTime existingEnd = existingStart.plusMinutes(existing.getDurationMinutes());
                    return candidateStart.isBefore(existingEnd) && existingStart.isBefore(candidateEnd);
                });
        if (conflict) {
            throw new ConflictException("Instructor already has an overlapping lesson");
        }
    }

    private void validateStatusTransition(String currentStatus, String nextStatus) {
        if (currentStatus == null || nextStatus == null) {
            throw new IllegalArgumentException("status is required");
        }
        Set<String> allowed = ALLOWED_STATUS_TRANSITIONS.get(currentStatus.toLowerCase());
        if (allowed == null || !allowed.contains(nextStatus.toLowerCase())) {
            throw new ConflictException("Invalid lesson status transition from " + currentStatus + " to " + nextStatus);
        }
    }
}