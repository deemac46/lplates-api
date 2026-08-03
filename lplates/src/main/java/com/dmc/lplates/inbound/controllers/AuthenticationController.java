package com.dmc.lplates.inbound.controllers;

import java.sql.Timestamp;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
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
        log.info("Login attempt for identifier={}", loginDto.getUsername());
        try {
            authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginDto.getUsername(), loginDto.getPassword())
            );
        } catch (BadCredentialsException e) {
            log.warn("Login failed for identifier={}: invalid credentials", loginDto.getUsername());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Invalid username or password"));
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
        if (userService.existsByUsername(registerDto.getUsername())) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", "Username already taken"));
        }
        if (registerDto.getEmail() != null && userService.existsByEmail(registerDto.getEmail())) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", "Email already in use"));
        }

        Role role = Role.LEARNER;
        if (registerDto.getRole() != null) {
            try {
                role = Role.valueOf(registerDto.getRole().toUpperCase());
            } catch (IllegalArgumentException ignored) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Invalid role. Must be LEARNER, INSTRUCTOR, or ADMIN"));
            }
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
