package com.example.inventory_factory_management.security;

import com.example.inventory_factory_management.entity.User;
import com.example.inventory_factory_management.repository.UserRepository;
import com.example.inventory_factory_management.service.TokenBlacklistService;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;


@Component
public class JwtFilter extends OncePerRequestFilter {

    JwtAuth jwtAuth;
    UserRepository userRepository;
    TokenBlacklistService tokenBlacklistService;

    @Autowired
    public JwtFilter(JwtAuth jwtAuth, UserRepository userRepository, TokenBlacklistService tokenBlacklistService) {
        this.jwtAuth = jwtAuth;
        this.userRepository = userRepository;
        this.tokenBlacklistService = tokenBlacklistService;
    }

    @Override
    public void doFilterInternal(HttpServletRequest request,
                                 HttpServletResponse response,
                                 FilterChain filterChain) throws ServletException, IOException {

        String requestPath = request.getServletPath();

        // Skip JWT validation for public endpoints
        if (requestPath.equals("/auth/signup") ||
                requestPath.equals("/auth/login") ||
                requestPath.equals("/auth/logout") ||
                requestPath.equals("/index/")) {
            filterChain.doFilter(request, response);
            return;
        }

        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            sendErrorResponse(response, "Missing or invalid Authorization header. Format: Bearer <token>");
            return;
        }

        try {
            String token = header.substring(7).trim();
            // Check if token is blacklisted
            if (tokenBlacklistService.isTokenBlacklisted(token)) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("{\"error\": \"Token has been invalidated, login again\"}");
                return;
            }

            String userEmail = jwtAuth.getUserEmailFromToken(token);

            if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                if (userRepository.findByEmail(userEmail).isEmpty()) {
                    sendErrorResponse(response, "User not found");
                    return;
                } else {
                    Optional<User> u = userRepository.findByEmail(userEmail);
                    SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + u.get().getRole().name());
                    UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken =
                            new UsernamePasswordAuthenticationToken(
                                    userEmail,
                                    null,
                                    Collections.singletonList(authority));
                    SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
                }
            }

            filterChain.doFilter(request, response);

        }
        catch (MalformedJwtException e) {
            sendErrorResponse(response, "Invalid JWT token format");
        } catch (ExpiredJwtException e) {
            sendErrorResponse(response, "JWT token has expired");
        }
        catch (RuntimeException e) {
            sendErrorResponse(response, e.getMessage());
        }
//        catch (Exception e) {
//            sendErrorResponse(response, "Authentication failed");
//        }
    }

    private void sendErrorResponse(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String timestamp = LocalDateTime.now().toString();
        String jsonResponse = String.format(
                "{\"timestamp\":\"%s\",\"message\":\"%s\",\"status\":401}",
                timestamp, message
        );

        response.getWriter().write(jsonResponse);
        response.getWriter().flush();
    }
}