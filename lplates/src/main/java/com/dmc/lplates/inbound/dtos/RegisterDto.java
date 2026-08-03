package com.dmc.lplates.inbound.dtos;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterDto {

    private String username;
    private String firstName;
    private String lastName;
    private String email;
    private String password;
    /**
     * Role name: LEARNER, INSTRUCTOR, or ADMIN.
     * Defaults to LEARNER if omitted.
     */
    private String role;
}
