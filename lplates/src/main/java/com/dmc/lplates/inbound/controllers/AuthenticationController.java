package com.dmc.lplates.inbound.controllers;

import java.sql.Timestamp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.dmc.lplates.config.JwtUtil;
import com.dmc.lplates.inbound.dtos.JwtResponse;
import com.dmc.lplates.inbound.dtos.LoginDto;
import com.dmc.lplates.inbound.dtos.RegisterDto;
import com.dmc.lplates.inbound.models.Role;
import com.dmc.lplates.inbound.models.User;
import com.dmc.lplates.service.UserService;
import com.dmc.lplates.service.ConflictException;
import com.dmc.lplates.service.UnauthorizedException;

@RestController
@RequestMapping("/auth")
public class AuthenticationController {

    private static final Logger log = LoggerFactory.getLogger(AuthenticationController.class);

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    public AuthenticationController(AuthenticationManager authenticationManager,
                                    JwtUtil jwtUtil,
                                    UserService userService,
                                    PasswordEncoder passwordEncoder) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * POST /auth/login
     * Authenticates credentials and returns a JWT. The `username` field accepts
     * either a username or an email address.
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginDto loginDto) {
        if (loginDto.getUsername() == null || loginDto.getUsername().isBlank()
                || loginDto.getPassword() == null || loginDto.getPassword().isBlank()) {
            throw new IllegalArgumentException("username and password are required");
        }
        log.info("Login attempt for identifier={}", loginDto.getUsername());
        try {
            authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginDto.getUsername(), loginDto.getPassword())
            );
        } catch (BadCredentialsException e) {
            log.warn("Login failed for identifier={}: invalid credentials", loginDto.getUsername());
            throw new UnauthorizedException("Invalid username or password");
        }

        User user = userService.getUserByUsernameOrEmail(loginDto.getUsername());
        String token = jwtUtil.generateToken(user);
        log.info("Login succeeded for username={} userId={} role={}", user.getUsername(), user.getId(), user.getRole());
        return ResponseEntity.ok(new JwtResponse(token, user.getId(), user.getUsername(), user.getRole().name()));
    }

    /**
     * POST /auth/register
     * Registers a new user and returns a JWT.
     * Role defaults to LEARNER if not specified.
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterDto registerDto) {
        if (registerDto.getUsername() == null || registerDto.getUsername().isBlank()
                || registerDto.getPassword() == null || registerDto.getPassword().isBlank()
                || registerDto.getEmail() == null || registerDto.getEmail().isBlank()) {
            throw new IllegalArgumentException("username, email, and password are required");
        }
        if (userService.existsByUsername(registerDto.getUsername())) {
            throw new ConflictException("Username already taken");
        }
        if (registerDto.getEmail() != null && userService.existsByEmail(registerDto.getEmail())) {
            throw new ConflictException("Email already in use");
        }

        Role role = Role.LEARNER;
        if (registerDto.getRole() != null) {
            try {
                role = Role.valueOf(registerDto.getRole().toUpperCase());
            } catch (IllegalArgumentException ignored) {
                throw new IllegalArgumentException("role must be LEARNER or INSTRUCTOR");
            }
        }
        if (role == Role.ADMIN) {
            throw new AccessDeniedException("Administrator accounts cannot be created through public registration");
        }

        User user = new User();
        user.setUsername(registerDto.getUsername());
        user.setFirstName(registerDto.getFirstName() != null ? registerDto.getFirstName() : "");
        user.setLastName(registerDto.getLastName() != null ? registerDto.getLastName() : "");
        user.setEmail(registerDto.getEmail() != null ? registerDto.getEmail() : "");
        user.setPassword(passwordEncoder.encode(registerDto.getPassword()));
        user.setRole(role);
        user.setActive(true);
        user.setCreatedAt(new Timestamp(System.currentTimeMillis()));

        User created = userService.createUser(user);
        String token = jwtUtil.generateToken(created);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new JwtResponse(token, created.getId(), created.getUsername(), created.getRole().name()));
    }
}
