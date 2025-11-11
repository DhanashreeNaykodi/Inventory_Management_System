package com.example.inventory_factory_management.service;

import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TokenBlacklistService {

    // In production, use Redis or database instead of in-memory storage
    private final Set<String> blacklistedTokens = ConcurrentHashMap.newKeySet();

    public void blacklistToken(String token) {
        if (token != null && !token.trim().isEmpty()) {
            blacklistedTokens.add(token);
        }
    }

    public boolean isTokenBlacklisted(String token) {
        return blacklistedTokens.contains(token);
    }

    // Optional: Clean up expired tokens (you can run this periodically)
    public void cleanExpiredTokens() {
        // Implementation depends on how you track token expiration
        // For JWT, you might want to check expiration before cleaning
//        blacklistedTokens.clear();
    }
}