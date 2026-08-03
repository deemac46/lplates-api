package com.dmc.lplates.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final UserDetailsServiceImpl userDetailsService;
    private final JwtAuthFilter jwtAuthFilter;
    private final PasswordEncoder passwordEncoder;

    public SecurityConfig(UserDetailsServiceImpl userDetailsService,
                          JwtAuthFilter jwtAuthFilter,
                          PasswordEncoder passwordEncoder) {
        this.userDetailsService = userDetailsService;
        this.jwtAuthFilter = jwtAuthFilter;
        this.passwordEncoder = passwordEncoder;
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // Public endpoints
                .requestMatchers("/auth/**").permitAll()
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                // Instructors - anyone authenticated can read; only INSTRUCTOR/ADMIN can create
                .requestMatchers(HttpMethod.GET, "/instructors/pending").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PATCH, "/instructors/*/approval").hasRole("ADMIN")
                .requestMatchers(HttpMethod.POST, "/instructors/*/profile-picture").hasAnyRole("ADMIN", "INSTRUCTOR")
                .requestMatchers(HttpMethod.GET, "/instructors/**").authenticated()
                .requestMatchers(HttpMethod.POST, "/instructors/create").hasAnyRole("ADMIN", "INSTRUCTOR")
                // Lessons - LEARNER and ADMIN can create; all authenticated can read
                .requestMatchers(HttpMethod.POST, "/lessons/create").hasAnyRole("ADMIN", "LEARNER")
                .requestMatchers("/lessons/**").authenticated()
                // EDT - LEARNER and ADMIN
                .requestMatchers("/edt/**").hasAnyRole("ADMIN", "LEARNER")
                // Pricing - INSTRUCTOR and ADMIN
                .requestMatchers("/pricing/**").hasAnyRole("ADMIN", "INSTRUCTOR")
                // Users
                .requestMatchers("/users/**").authenticated()
                .anyRequest().authenticated()
            )
            .authenticationProvider(authenticationProvider())
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
