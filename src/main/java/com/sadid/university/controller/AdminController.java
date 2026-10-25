package com.sadid.university.controller;

import com.sadid.university.dto.AdminCreateRequest;
import com.sadid.university.dto.AdminResponse;
import com.sadid.university.dto.AdminUpdateRequest;
import com.sadid.university.service.AdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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
@RequestMapping("/api/admins")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @GetMapping
    public List<AdminResponse> getAll() {
        return adminService.getAll();
    }

    @GetMapping("/{id}")
    public AdminResponse getById(@PathVariable Long id) {
        return adminService.getById(id);
    }

    @PostMapping
    public ResponseEntity<AdminResponse> create(@Valid @RequestBody AdminCreateRequest request) {
        AdminResponse created = adminService.create(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.id())
                .toUri();
        return ResponseEntity.created(location).body(created);
    }

    @PutMapping("/{id}")
    public AdminResponse update(@PathVariable Long id,
                                @Valid @RequestBody AdminUpdateRequest request) {
        return adminService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        adminService.delete(id);
        return ResponseEntity.noContent().build();
    }
}