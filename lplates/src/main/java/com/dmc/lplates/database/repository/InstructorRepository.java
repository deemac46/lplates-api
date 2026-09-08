package com.dmc.lplates.database.repository;

import java.util.List;

import com.dmc.lplates.inbound.models.Instructor;

public interface InstructorRepository {

    List<Instructor> getAllInstructors();

    Instructor getInstructorById(Long instructorId);

    Long createInstructor(Instructor instructor);

    Instructor getInstructorWithLessons(Long instructorId);

    Instructor getInstructorByUserId(Long userId);

    List<Instructor> getPendingInstructors();

    Instructor updateApprovalStatus(Long instructorId, String approvalStatus);

    Instructor updateAvailability(Long instructorId, boolean available);

    Instructor updateProfilePicture(Long instructorId, String profilePicture);
}
