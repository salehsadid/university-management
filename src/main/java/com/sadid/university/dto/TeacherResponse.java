package com.sadid.university.dto;

import com.sadid.university.entity.Teacher;

public record TeacherResponse(
        Long id,
        String name,
        String email,
        String department,
        String designation,
        String phone
) {

    public static TeacherResponse from(Teacher teacher) {
        return new TeacherResponse(
                teacher.getId(),
                teacher.getName(),
                teacher.getUser().getEmail(),
                teacher.getDepartment(),
                teacher.getDesignation(),
                teacher.getPhone()
        );
    }
}