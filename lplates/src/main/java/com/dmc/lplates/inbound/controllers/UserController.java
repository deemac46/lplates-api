package com.dmc.lplates.inbound.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
public class UserController {


    // Example endpoint to get user information
    @RequestMapping("/{userId}")
    public String getUserInfo() {
        return "User information";
    }

    // Example endpoint to create a new user
    @RequestMapping("/create")
    public String createUser() {
        return "User created successfully";
    }

    @RequestMapping("/")
    public String getAllUsers() {
        return "List of all users";
    }

    // Additional endpoints can be added here
}
