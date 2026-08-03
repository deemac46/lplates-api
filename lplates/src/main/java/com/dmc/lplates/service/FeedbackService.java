package com.dmc.lplates.service;

import java.util.List;

import com.dmc.lplates.inbound.models.Feedback;

public interface FeedbackService {

    Feedback createFeedback(Feedback feedback);
    Feedback getFeedbackById(long feedbackId);
    Feedback getFeedbackByLessonId(long lessonId);
    List<Feedback> getFeedbackByInstructorId(long instructorId);
}
