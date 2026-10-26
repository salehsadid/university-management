package com.sadid.university.repository;

import com.sadid.university.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StudentRepository extends JpaRepository<Student, Long> {

    boolean existsByStudentId(String studentId);

    Optional<Student> findByUserEmail(String email);
}