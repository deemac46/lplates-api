package com.dmc.lplates.inbound.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.dmc.lplates.inbound.models.Feedback;
import com.dmc.lplates.service.FeedbackServiceImpl;

@RestController
@RequestMapping("/feedback")
public class FeedbackController {

    private final FeedbackServiceImpl feedbackService;

    @Autowired
    public FeedbackController(FeedbackServiceImpl feedbackService) {
        this.feedbackService = feedbackService;
    }

    @PostMapping("/create")
    public ResponseEntity<Feedback> createFeedback(@RequestBody Feedback feedback) {
        Feedback result = feedbackService.createFeedback(feedback);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{feedbackId}")
    public ResponseEntity<Feedback> getFeedbackById(@PathVariable Long feedbackId) {
        Feedback feedback = feedbackService.getFeedbackById(feedbackId);
        if (feedback != null) {
            return ResponseEntity.ok(feedback);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/lesson/{lessonId}")
    public ResponseEntity<Feedback> getFeedbackByLesson(@PathVariable Long lessonId) {
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
