package com.dmc.lplates.service;

import com.dmc.lplates.inbound.models.Instructor;

import java.util.List;

public interface InstructorsService {

    List<Instructor> getAllInstructors();

    List<Instructor> getAvailableInstructors();

    Instructor getInstructorById(Long instructorId);

    Instructor createInstructor(Instructor instructor);

    Instructor getInstructorWithLessons(Long instructorId);

    Instructor getInstructorByUserId(Long userId);

    List<Instructor> getPendingInstructors();

    Instructor updateApprovalStatus(Long instructorId, String approvalStatus);

    Instructor updateAvailability(Long instructorId, boolean available);

    Instructor updateProfilePicture(Long instructorId, String profilePicture);
}
