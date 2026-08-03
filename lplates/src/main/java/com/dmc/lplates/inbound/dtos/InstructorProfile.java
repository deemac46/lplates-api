package com.dmc.lplates.inbound.dtos;

import com.dmc.lplates.inbound.models.Instructor;
import com.dmc.lplates.inbound.models.User;

import lombok.Getter;

@Getter
public class InstructorProfile {

    private final User user;
    private final Instructor instructor;

    public InstructorProfile(User user, Instructor instructor) {
        this.user = user;
        this.instructor = instructor;
    }
}
