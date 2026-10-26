package com.sadid.university.dto;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

public record MeResponse(
        String email,
        String role
) {

    private static final String ROLE_PREFIX = "ROLE_";

    public static MeResponse from(Authentication authentication) {
        // Other authorities (e.g. FACTOR_PASSWORD) may be present, so pick the ROLE_ one
        String role = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(authority -> authority.startsWith(ROLE_PREFIX))
                .map(authority -> authority.substring(ROLE_PREFIX.length()))
                .findFirst()
                .orElse(null);

        return new MeResponse(authentication.getName(), role);
    }
}
