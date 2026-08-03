package com.dmc.lplates.inbound.models;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class User implements UserDetails {

    private Long id;
    private String username;
    private String firstName;
    private String lastName;
    private String email;
    private String password;
    private Role role;
    private Boolean active;
    private Timestamp createdAt;

    // -----------------------------------------------------------------
    // Relationship fields - only populated when the profile is requested
    // -----------------------------------------------------------------

    /** LEARNER: lessons this user has booked as a student */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<Booking> bookings;

    /** LEARNER: EDT module completion records for this student */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<EdtProgress> edtProgress;

    /** INSTRUCTOR: full instructor profile (includes lessons, pricing, feedback) */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Instructor instructorProfile;

    @Override
    @JsonIgnore
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    @JsonIgnore
    public String getPassword() {
        return password;
    }

    @Override
    @JsonIgnore
    public boolean isAccountNonExpired() { return true; }

    @Override
    @JsonIgnore
    public boolean isAccountNonLocked() { return true; }

    @Override
    @JsonIgnore
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    @JsonIgnore
    public boolean isEnabled() { return Boolean.TRUE.equals(active); }
}
