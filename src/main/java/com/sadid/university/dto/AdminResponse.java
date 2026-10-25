package com.sadid.university.dto;

import com.sadid.university.entity.Admin;

public record AdminResponse(
        Long id,
        String name,
        String email
) {

    public static AdminResponse from(Admin admin) {
        return new AdminResponse(
                admin.getId(),
                admin.getName(),
                admin.getUser().getEmail()
        );
    }
}