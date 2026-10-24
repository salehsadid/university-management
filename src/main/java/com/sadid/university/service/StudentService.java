package com.sadid.university.service;

import com.sadid.university.dto.StudentCreateRequest;
import com.sadid.university.dto.StudentResponse;
import com.sadid.university.dto.StudentUpdateRequest;
import com.sadid.university.entity.AppUser;
import com.sadid.university.entity.Role;
import com.sadid.university.entity.Student;
import com.sadid.university.exception.DuplicateResourceException;
import com.sadid.university.exception.ResourceNotFoundException;
import com.sadid.university.repository.AppUserRepository;
import com.sadid.university.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StudentService {

    private final StudentRepository studentRepository;
    private final AppUserRepository appUserRepository;

    @Transactional(readOnly = true)
    public List<StudentResponse> getAll() {
        return studentRepository.findAll()
                .stream()
                .map(StudentResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public StudentResponse getById(Long id) {
        return StudentResponse.from(findStudentOrThrow(id));
    }

    @Transactional
    public StudentResponse create(StudentCreateRequest request) {
        if (appUserRepository.existsByEmail(request.email())) {
            throw new DuplicateResourceException("Email already in use: " + request.email());
        }
        if (studentRepository.existsByStudentId(request.studentId())) {
            throw new DuplicateResourceException("Student ID already exists: " + request.studentId());
        }

        AppUser user = new AppUser();
        user.setEmail(request.email());
        user.setPassword(request.password()); // TODO Lesson 4: hash with BCrypt
        user.setRole(Role.STUDENT);

        Student student = new Student();
        student.setStudentId(request.studentId());
        student.setName(request.name());
        student.setDepartment(request.department());
        student.setSemester(request.semester());
        student.setPhone(request.phone());
        student.setUser(user);

        Student saved = studentRepository.save(student);
        return StudentResponse.from(saved);
    }

    @Transactional
    public StudentResponse update(Long id, StudentUpdateRequest request) {
        Student student = findStudentOrThrow(id);
        AppUser user = student.getUser();

        boolean emailChanged = !user.getEmail().equals(request.email());
        if (emailChanged && appUserRepository.existsByEmail(request.email())) {
            throw new DuplicateResourceException("Email already in use: " + request.email());
        }

        boolean studentIdChanged = !student.getStudentId().equals(request.studentId());
        if (studentIdChanged && studentRepository.existsByStudentId(request.studentId())) {
            throw new DuplicateResourceException("Student ID already exists: " + request.studentId());
        }

        student.setStudentId(request.studentId());
        student.setName(request.name());
        student.setDepartment(request.department());
        student.setSemester(request.semester());
        student.setPhone(request.phone());

        user.setEmail(request.email());
        if (request.password() != null) {
            user.setPassword(request.password()); // TODO Lesson 4: hash with BCrypt
        }

        return StudentResponse.from(student);
    }

    @Transactional
    public void delete(Long id) {
        Student student = findStudentOrThrow(id);
        studentRepository.delete(student);
    }

    private Student findStudentOrThrow(Long id) {
        return studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + id));
    }
}