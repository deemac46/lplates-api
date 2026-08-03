package com.dmc.lplates.database.repository;

import java.util.List;

import com.dmc.lplates.inbound.models.Feedback;

public interface FeedbackRepository {

    Feedback insertFeedback(Feedback feedback);
    Feedback getFeedbackById(long feedbackId);
    Feedback getFeedbackByLessonId(long lessonId);
    List<Feedback> getFeedbackByInstructorId(long instructorId);
}
