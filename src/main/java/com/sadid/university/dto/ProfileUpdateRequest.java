package com.sadid.university.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

// Used by students and teachers to update their own profile via PUT /me
public record ProfileUpdateRequest(

        @NotBlank(message = "Name is required")
        @Size(max = 100, message = "Name must be at most 100 characters")
        String name,

        @NotBlank(message = "Email is required")
        @Email(message = "Email must be valid")
        String email,

        @Size(min = 6, max = 64, message = "Password must be 6 to 64 characters")
        String password,

        @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Phone must be 10 to 15 digits")
        String phone
) {

    public boolean changesCredentials(String currentEmail) {
        return !email.equals(currentEmail) || password != null;
    }
}
