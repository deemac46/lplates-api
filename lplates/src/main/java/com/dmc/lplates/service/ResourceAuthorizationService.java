package com.dmc.lplates.service;

import com.dmc.lplates.inbound.models.Booking;
import com.dmc.lplates.inbound.models.Feedback;
import com.dmc.lplates.inbound.models.Instructor;
import com.dmc.lplates.inbound.models.InstructorPricing;
import com.dmc.lplates.inbound.models.Role;
import com.dmc.lplates.inbound.models.User;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
public class ResourceAuthorizationService {

    private final BookingServiceImpl bookingService;
    private final InstructorsServiceImpl instructorsService;
    private final InstructorPricingServiceImpl pricingService;
    private final FeedbackServiceImpl feedbackService;

    public ResourceAuthorizationService(BookingServiceImpl bookingService,
                                        InstructorsServiceImpl instructorsService,
                                        InstructorPricingServiceImpl pricingService,
                                        FeedbackServiceImpl feedbackService) {
        this.bookingService = bookingService;
        this.instructorsService = instructorsService;
        this.pricingService = pricingService;
        this.feedbackService = feedbackService;
    }

    public User currentUser(Authentication authentication) {
        return (User) authentication.getPrincipal();
    }

    public void requireAdmin(User user) {
        if (user.getRole() != Role.ADMIN) {
            throw new AccessDeniedException("Administrator access is required");
        }
    }

    public Instructor requireOwnInstructor(long instructorId, User user) {
        Instructor instructor = instructorsService.getInstructorById(instructorId);
        if (instructor == null) {
            throw new ResourceNotFoundException("Instructor not found");
        }
        if (user.getRole() != Role.ADMIN && !user.getId().equals(instructor.getUserId())) {
            throw new AccessDeniedException("You may only manage your own instructor profile");
        }
        return instructor;
    }

    public Instructor requireInstructorProfile(User user) {
        Instructor instructor = instructorsService.getInstructorByUserId(user.getId());
        if (instructor == null) {
            throw new AccessDeniedException("An instructor profile is required");
        }
        return instructor;
    }

    public Booking requireLessonParticipant(long lessonId, User user) {
        Booking lesson = bookingService.getBookingDetailsById(lessonId);
        if (lesson == null) {
            throw new ResourceNotFoundException("Lesson not found");
        }
        if (user.getRole() == Role.ADMIN || user.getId().equals(lesson.getStudentId())) {
            return lesson;
        }
        Instructor instructor = instructorsService.getInstructorById(lesson.getInstructorId());
        if (instructor == null || !user.getId().equals(instructor.getUserId())) {
            throw new AccessDeniedException("You do not have access to this lesson");
        }
        return lesson;
    }

    public Booking requireAssignedInstructor(long lessonId, User user) {
        Booking lesson = requireLessonParticipant(lessonId, user);
        if (user.getRole() == Role.ADMIN) {
            return lesson;
        }
        Instructor instructor = instructorsService.getInstructorById(lesson.getInstructorId());
        if (instructor == null || !user.getId().equals(instructor.getUserId())) {
            throw new AccessDeniedException("Only the assigned instructor may perform this action");
        }
        return lesson;
    }

    public void requireStudent(long studentId, User user) {
        if (user.getRole() != Role.ADMIN && !user.getId().equals(studentId)) {
            throw new AccessDeniedException("You may only access your own learner records");
        }
    }

    public InstructorPricing requireOwnPricing(long pricingId, User user) {
        InstructorPricing pricing = pricingService.getPricingById(pricingId);
        if (pricing == null) {
            throw new ResourceNotFoundException("Pricing record not found");
        }
        requireOwnInstructor(pricing.getInstructorId(), user);
        return pricing;
    }

    public Feedback requireFeedbackAuthor(long feedbackId, User user) {
        Feedback feedback = feedbackService.getFeedbackById(feedbackId);
        if (feedback == null) {
            throw new ResourceNotFoundException("Feedback not found");
        }
        if (user.getRole() != Role.ADMIN && !user.getId().equals(feedback.getAuthorId())) {
            throw new AccessDeniedException("You may only manage your own feedback");
        }
        return feedback;
    }
}