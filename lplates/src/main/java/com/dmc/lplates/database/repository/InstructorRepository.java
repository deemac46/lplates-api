package com.dmc.lplates.database.repository;

import com.dmc.lplates.inbound.models.Instructor;

import java.util.List;

public interface InstructorRepository {

    List<Instructor> getAllInstructors();

    Instructor getInstructorById(Long instructorId);

    String createInstructor(Instructor instructor);

    Instructor getInstructorWithBookings(Long instructorId);
}
