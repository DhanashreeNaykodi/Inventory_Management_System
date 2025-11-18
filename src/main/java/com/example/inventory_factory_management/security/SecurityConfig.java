package com.example.inventory_factory_management.security;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {


    @Autowired
    private JwtFilter jwtFilter;

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth

                        // Public endpoints
                        .requestMatchers("/auth/login", "/auth/signup").permitAll()

                        // User endpoints - accessible to all authenticated users
                        .requestMatchers("/user/profile").authenticated()
                        .requestMatchers("/user/{userId}/profile").authenticated()

                        // FIXED: Use proper pattern for logout
                        .requestMatchers("/user/*/logout").authenticated()  // Changed from /**/ to /*
                        .requestMatchers("/user/logout").authenticated()     // Add this for your updated logout endpoint

                        // Specific role-based endpoints
                        .requestMatchers("/manager/**").hasAnyRole("OWNER", "MANAGER")
                        .requestMatchers("/owner/**").hasAnyRole("OWNER","CENTRAL_OFFICER", "MANAGER")
                        .requestMatchers("/product/categories/**").hasRole("OWNER")
                        .requestMatchers("/factories/**").hasRole("OWNER")
                        .requestMatchers("/tools/**").hasAnyRole("OWNER", "MANAGER", "CHIEF_SUPERVISOR", "WORKER")

                        .requestMatchers("/distributor/**").hasAnyRole("DISTRIBUTOR", "CHIEF_OFFICER")
                        // Fallback - any other request needs authentication
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

}