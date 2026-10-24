package com.sadid.university.dto;

import com.sadid.university.entity.Student;

public record StudentResponse(
        Long id,
        String studentId,
        String name,
        String email,
        String department,
        String semester,
        String phone
) {

    public static StudentResponse from(Student student) {
        return new StudentResponse(
                student.getId(),
                student.getStudentId(),
                student.getName(),
                student.getUser().getEmail(),
                student.getDepartment(),
                student.getSemester(),
                student.getPhone()
        );
    }
}