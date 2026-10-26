package com.sadid.university.controller;

import com.sadid.university.dto.ProfileUpdateRequest;
import com.sadid.university.dto.TeacherCreateRequest;
import com.sadid.university.dto.TeacherResponse;
import com.sadid.university.dto.TeacherUpdateRequest;
import com.sadid.university.security.SessionUtils;
import com.sadid.university.service.TeacherService;
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
@RequestMapping("/api/teachers")
@RequiredArgsConstructor
public class TeacherController {

    private final TeacherService teacherService;

    // ---- Own profile (TEACHER only, see SecurityConfig) ----

    @GetMapping("/me")
    public TeacherResponse getMe(Authentication authentication) {
        return teacherService.getByEmail(authentication.getName());
    }

    @PutMapping("/me")
    public TeacherResponse updateMe(Authentication authentication,
                                  @Valid @RequestBody ProfileUpdateRequest request,
                                  HttpServletRequest httpRequest) {
        TeacherResponse updated = teacherService.updateProfile(authentication.getName(), request);
        // New email or password: the user must log in again
        if (request.changesCredentials(authentication.getName())) {
            SessionUtils.endSession(httpRequest);
        }
        return updated;
    }

    @DeleteMapping("/me")
    public ResponseEntity<Void> deleteMe(Authentication authentication, HttpServletRequest httpRequest) {
        teacherService.deleteByEmail(authentication.getName());
        SessionUtils.endSession(httpRequest);
        return ResponseEntity.noContent().build();
    }

    // ---- Admin management ----

    @GetMapping
    public List<TeacherResponse> getAll() {
        return teacherService.getAll();
    }

    @GetMapping("/{id}")
    public TeacherResponse getById(@PathVariable Long id) {
        return teacherService.getById(id);
    }

    @PostMapping
    public ResponseEntity<TeacherResponse> create(@Valid @RequestBody TeacherCreateRequest request) {
        TeacherResponse created = teacherService.create(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.id())
                .toUri();
        return ResponseEntity.created(location).body(created);
    }

    @PutMapping("/{id}")
    public TeacherResponse update(@PathVariable Long id,
                                  @Valid @RequestBody TeacherUpdateRequest request) {
        return teacherService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        teacherService.delete(id);
        return ResponseEntity.noContent().build();
    }
}