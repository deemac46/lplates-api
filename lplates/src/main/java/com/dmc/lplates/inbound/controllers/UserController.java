package com.dmc.lplates.inbound.controllers;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.dmc.lplates.inbound.dtos.InstructorProfile;
import com.dmc.lplates.inbound.dtos.LearnerProfile;
import com.dmc.lplates.inbound.models.Role;
import com.dmc.lplates.inbound.models.User;
import com.dmc.lplates.service.UserService;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /** GET /users/me — basic info of the current authenticated user */
    @GetMapping("/me")
    public ResponseEntity<User> getCurrentUser(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        return ResponseEntity.ok(user);
    }

    /**
     * GET /users/me/profile
     * Returns a role-specific profile for the current user:
     * - LEARNER:     LearnerProfile (bookings + EDT progress + summary)
     * - INSTRUCTOR:  InstructorProfile (instructor entity with lessons + pricing)
     * - ADMIN:       plain User
     */
    @GetMapping("/me/profile")
    public ResponseEntity<?> getCurrentUserProfile(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        return buildProfileResponse(user.getId(), user.getRole());
    }

    /**
     * GET /users/{id}/profile — role-specific profile for any user. ADMIN only.
     */
    @GetMapping("/{id}/profile")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getUserProfile(@PathVariable long id) {
        User user = userService.getUserById(id);
        if (user == null) return ResponseEntity.notFound().build();
        return buildProfileResponse(id, user.getRole());
    }

    /** GET /users/{id} — basic user info. ADMIN only. */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<User> getUserById(@PathVariable long id) {
        User user = userService.getUserById(id);
        if (user == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(user);
    }

    /** GET /users/ — all users. ADMIN only. */
    @GetMapping("/")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    // -------------------------------------------------------------------------

    private ResponseEntity<?> buildProfileResponse(long userId, Role role) {
        return switch (role) {
            case LEARNER -> {
                LearnerProfile profile = userService.getLearnerProfile(userId);
                yield profile != null ? ResponseEntity.ok(profile) : ResponseEntity.notFound().build();
            }
            case INSTRUCTOR -> {
                InstructorProfile profile = userService.getInstructorProfile(userId);
                yield profile != null ? ResponseEntity.ok(profile) : ResponseEntity.notFound().build();
            }
            case ADMIN -> ResponseEntity.ok(userService.getUserById(userId));
        };
    }
}
