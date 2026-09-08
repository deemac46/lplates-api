package com.dmc.lplates.inbound.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.dmc.lplates.inbound.models.Feedback;
import com.dmc.lplates.inbound.models.Booking;
import com.dmc.lplates.inbound.models.User;
import com.dmc.lplates.service.BusinessValidationService;
import com.dmc.lplates.service.FeedbackServiceImpl;
import com.dmc.lplates.service.ResourceAuthorizationService;

@RestController
@RequestMapping("/feedback")
public class FeedbackController {

    private final FeedbackServiceImpl feedbackService;
    private final ResourceAuthorizationService authorizationService;
    private final BusinessValidationService validationService;

    @Autowired
    public FeedbackController(FeedbackServiceImpl feedbackService,
                              ResourceAuthorizationService authorizationService,
                              BusinessValidationService validationService) {
        this.feedbackService = feedbackService;
        this.authorizationService = authorizationService;
        this.validationService = validationService;
    }

    @PostMapping("/create")
    public ResponseEntity<Feedback> createFeedback(@RequestBody Feedback feedback, Authentication authentication) {
        User currentUser = authorizationService.currentUser(authentication);
        Booking lesson = authorizationService.requireLessonParticipant(feedback.getLessonId(), currentUser);
        feedback.setAuthorId(currentUser.getId());
        validationService.validateFeedback(feedback, lesson);
        Feedback result = feedbackService.createFeedback(feedback);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{feedbackId}")
    public ResponseEntity<Feedback> getFeedbackById(@PathVariable Long feedbackId,
                                                     Authentication authentication) {
        User currentUser = authorizationService.currentUser(authentication);
        return ResponseEntity.ok(authorizationService.requireFeedbackAuthor(feedbackId, currentUser));
    }

    @GetMapping("/lesson/{lessonId}")
    public ResponseEntity<Feedback> getFeedbackByLesson(@PathVariable Long lessonId,
                                                         Authentication authentication) {
        authorizationService.requireLessonParticipant(lessonId, authorizationService.currentUser(authentication));
        Feedback feedback = feedbackService.getFeedbackByLessonId(lessonId);
        if (feedback != null) {
            return ResponseEntity.ok(feedback);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/instructor/{instructorId}")
    public ResponseEntity<List<Feedback>> getFeedbackByInstructor(@PathVariable Long instructorId) {
        List<Feedback> feedbackList = feedbackService.getFeedbackByInstructorId(instructorId);
        if (feedbackList != null && !feedbackList.isEmpty()) {
            return ResponseEntity.ok(feedbackList);
        } else {
            return ResponseEntity.noContent().build();
        }
    }
}
