package com.dmc.lplates.database.repository;

import com.dmc.lplates.inbound.models.Instructor;

import java.util.List;

public interface InstructorRepository {

    List<Instructor> getAllInstructors();

    Instructor getInstructorById(Long instructorId);

    String createInstructor(Instructor instructor);

    Instructor getInstructorWithLessons(Long instructorId);

    Instructor getInstructorByUserId(Long userId);

    List<Instructor> getPendingInstructors();

    Instructor updateApprovalStatus(Long instructorId, String approvalStatus);

    Instructor updateProfilePicture(Long instructorId, String profilePicture);
}
