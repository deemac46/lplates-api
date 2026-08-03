package com.dmc.lplates.database.repository;

import java.util.List;

import com.dmc.lplates.inbound.models.User;

public interface UserRepository {

    User insertUser(User user);

    /**
     * Inserts a user with an explicit primary key (used for seeding mock data so that
     * user IDs line up with instructor/booking placeholder references). If a user with
     * this ID already exists, that existing user is returned unchanged.
     */
    User insertUserWithId(long id, User user);

    User findByUsername(String username);
    User findByEmail(String email);
    User findById(long id);
    List<User> getAllUsers();
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
}
