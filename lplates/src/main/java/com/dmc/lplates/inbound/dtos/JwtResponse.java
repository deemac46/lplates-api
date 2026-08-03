package com.dmc.lplates.inbound.dtos;

import lombok.Getter;

@Getter
public class JwtResponse {

    private final String token;
    private final String type = "Bearer";
    private final Long userId;
    private final String username;
    private final String role;

    public JwtResponse(String token, Long userId, String username, String role) {
        this.token = token;
        this.userId = userId;
        this.username = username;
        this.role = role;
    }
}
