package com.sadid.university.controller;

import com.sadid.university.dto.ProfileUpdateRequest;
import com.sadid.university.dto.StudentCreateRequest;
import com.sadid.university.dto.StudentResponse;
import com.sadid.university.dto.StudentUpdateRequest;
import com.sadid.university.security.SessionUtils;
import com.sadid.university.service.StudentService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/students")
@RequiredArgsConstructor
public class StudentController {

    private final StudentService studentService;

    // ---- Own profile (STUDENT only, see SecurityConfig) ----

    @GetMapping("/me")
    public StudentResponse getMe(Authentication authentication) {
        return studentService.getByEmail(authentication.getName());
    }

    @PutMapping("/me")
    public StudentResponse updateMe(Authentication authentication,
                                  @Valid @RequestBody ProfileUpdateRequest request,
                                  HttpServletRequest httpRequest) {
        StudentResponse updated = studentService.updateProfile(authentication.getName(), request);
        // New email or password: the user must log in again
        if (request.changesCredentials(authentication.getName())) {
            SessionUtils.endSession(httpRequest);
        }
        return updated;
    }

    @DeleteMapping("/me")
    public ResponseEntity<Void> deleteMe(Authentication authentication, HttpServletRequest httpRequest) {
        studentService.deleteByEmail(authentication.getName());
        SessionUtils.endSession(httpRequest);
        return ResponseEntity.noContent().build();
    }

    // ---- Admin management ----

    @GetMapping
    public List<StudentResponse> getAll() {
        return studentService.getAll();
    }

    @GetMapping("/{id}")
    public StudentResponse getById(@PathVariable Long id) {
        return studentService.getById(id);
    }

    @PostMapping
    public ResponseEntity<StudentResponse> create(@Valid @RequestBody StudentCreateRequest request) {
        StudentResponse created = studentService.create(request);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.id())
                .toUri();

        return ResponseEntity.created(location).body(created);
    }

    @PutMapping("/{id}")
    public StudentResponse update(@PathVariable Long id,
                                  @Valid @RequestBody StudentUpdateRequest request) {
        return studentService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        studentService.delete(id);
        return ResponseEntity.noContent().build();
    }
}