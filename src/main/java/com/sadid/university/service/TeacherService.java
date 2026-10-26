package com.sadid.university.service;

import com.sadid.university.dto.ProfileUpdateRequest;
import com.sadid.university.dto.TeacherCreateRequest;
import com.sadid.university.dto.TeacherResponse;
import com.sadid.university.dto.TeacherUpdateRequest;
import com.sadid.university.entity.AppUser;
import com.sadid.university.entity.Role;
import com.sadid.university.entity.Teacher;
import com.sadid.university.exception.DuplicateResourceException;
import com.sadid.university.exception.ResourceNotFoundException;
import com.sadid.university.repository.AppUserRepository;
import com.sadid.university.repository.TeacherRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TeacherService {

    private final TeacherRepository teacherRepository;
    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public List<TeacherResponse> getAll() {
        return teacherRepository.findAll()
                .stream()
                .map(TeacherResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public TeacherResponse getById(Long id) {
        return TeacherResponse.from(findTeacherOrThrow(id));
    }

    @Transactional
    public TeacherResponse create(TeacherCreateRequest request) {
        if (appUserRepository.existsByEmail(request.email())) {
            throw new DuplicateResourceException("Email already in use: " + request.email());
        }

        AppUser user = new AppUser();
        user.setEmail(request.email());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setRole(Role.TEACHER);

        Teacher teacher = new Teacher();
        teacher.setName(request.name());
        teacher.setDepartment(request.department());
        teacher.setDesignation(request.designation());
        teacher.setPhone(request.phone());
        teacher.setUser(user);

        return TeacherResponse.from(teacherRepository.save(teacher));
    }

    @Transactional
    public TeacherResponse update(Long id, TeacherUpdateRequest request) {
        Teacher teacher = findTeacherOrThrow(id);
        AppUser user = teacher.getUser();

        boolean emailChanged = !user.getEmail().equals(request.email());
        if (emailChanged && appUserRepository.existsByEmail(request.email())) {
            throw new DuplicateResourceException("Email already in use: " + request.email());
        }

        teacher.setName(request.name());
        teacher.setDepartment(request.department());
        teacher.setDesignation(request.designation());
        teacher.setPhone(request.phone());

        user.setEmail(request.email());
        if (request.password() != null) {
            user.setPassword(passwordEncoder.encode(request.password()));
        }

        return TeacherResponse.from(teacher);
    }

    @Transactional
    public void delete(Long id) {
        teacherRepository.delete(findTeacherOrThrow(id));
    }

    // ---- Own profile (/me), identified by the logged-in user's email ----

    @Transactional(readOnly = true)
    public TeacherResponse getByEmail(String email) {
        return TeacherResponse.from(findTeacherByEmailOrThrow(email));
    }

    @Transactional
    public TeacherResponse updateProfile(String email, ProfileUpdateRequest request) {
        Teacher teacher = findTeacherByEmailOrThrow(email);
        AppUser user = teacher.getUser();

        boolean emailChanged = !user.getEmail().equals(request.email());
        if (emailChanged && appUserRepository.existsByEmail(request.email())) {
            throw new DuplicateResourceException("Email already in use: " + request.email());
        }

        teacher.setName(request.name());
        teacher.setPhone(request.phone());

        user.setEmail(request.email());
        if (request.password() != null) {
            user.setPassword(passwordEncoder.encode(request.password()));
        }

        return TeacherResponse.from(teacher);
    }

    @Transactional
    public void deleteByEmail(String email) {
        teacherRepository.delete(findTeacherByEmailOrThrow(email));
    }

    private Teacher findTeacherOrThrow(Long id) {
        return teacherRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher not found with id: " + id));
    }

    private Teacher findTeacherByEmailOrThrow(String email) {
        return teacherRepository.findByUserEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher not found with email: " + email));
    }
}