package com.sadid.university.dto;

public record CsrfResponse(
        String headerName,
        String token
) {
}
