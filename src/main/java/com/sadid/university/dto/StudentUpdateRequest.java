package com.sadid.university.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record StudentUpdateRequest(

        @NotBlank(message = "Student ID is required")
        @Size(max = 20, message = "Student ID must be at most 20 characters")
        String studentId,

        @NotBlank(message = "Name is required")
        @Size(max = 100, message = "Name must be at most 100 characters")
        String name,

        @NotBlank(message = "Email is required")
        @Email(message = "Email must be valid")
        String email,

        @Size(min = 6, max = 64, message = "Password must be 6 to 64 characters")
        String password,

        @NotBlank(message = "Department is required")
        String department,

        @NotBlank(message = "Semester is required")
        String semester,

        @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Phone must be 10 to 15 digits")
        String phone
) {
}