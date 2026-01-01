package com.dmc.lplates.service;

import com.dmc.lplates.inbound.models.Instructor;

import java.util.List;

public interface InstructorsService {


    List<Instructor> getAllInstructors();

    Instructor getInstructorById(Long instructorId);

    String createInstructor(Instructor instructor);

    Instructor getInstructorWithBookings(Long instructorId);
}
