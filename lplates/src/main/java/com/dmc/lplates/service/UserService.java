package com.dmc.lplates.service;

import java.util.List;

import com.dmc.lplates.inbound.dtos.InstructorProfile;
import com.dmc.lplates.inbound.dtos.LearnerProfile;
import com.dmc.lplates.inbound.models.User;

public interface UserService {

    User createUser(User user);

    /**
     * Creates a user with an explicit ID (used for seeding). Returns the existing user
     * unchanged if one with this ID already exists.
     */
    User createUserWithId(long id, User user);

    User getUserById(long id);
    User getUserByUsername(String username);

    /**
     * Looks up a user by username first, falling back to email if no
     * username match is found. Used for login, which accepts either.
     */
    User getUserByUsernameOrEmail(String identifier);
    List<User> getAllUsers();
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);

    /**
     * Returns a LEARNER's profile: their bookings and EDT progress with summary.
     * Returns null if the user does not exist or is not a LEARNER.
     */
    LearnerProfile getLearnerProfile(long userId);

    /**
     * Returns an INSTRUCTOR's profile: their instructor entity with lessons and pricing.
     * Returns null if the user does not exist or is not an INSTRUCTOR.
     */
    InstructorProfile getInstructorProfile(long userId);
}
