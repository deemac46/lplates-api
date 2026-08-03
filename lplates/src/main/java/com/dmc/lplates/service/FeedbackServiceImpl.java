package com.dmc.lplates.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dmc.lplates.database.repository.FeedbackRepository;
import com.dmc.lplates.inbound.models.Feedback;

@Service
public class FeedbackServiceImpl implements FeedbackService {

    private final FeedbackRepository feedbackRepository;

    public FeedbackServiceImpl(@Autowired FeedbackRepository feedbackRepository) {
        this.feedbackRepository = feedbackRepository;
    }

    @Override
    public Feedback createFeedback(Feedback feedback) {
        return feedbackRepository.insertFeedback(feedback);
    }

    @Override
    public Feedback getFeedbackById(long feedbackId) {
        return feedbackRepository.getFeedbackById(feedbackId);
    }

    @Override
    public Feedback getFeedbackByLessonId(long lessonId) {
        return feedbackRepository.getFeedbackByLessonId(lessonId);
    }

    @Override
    public List<Feedback> getFeedbackByInstructorId(long instructorId) {
        return feedbackRepository.getFeedbackByInstructorId(instructorId);
    }
}
