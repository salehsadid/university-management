package com.sadid.university.service;

import com.sadid.university.dto.AdminCreateRequest;
import com.sadid.university.dto.AdminResponse;
import com.sadid.university.dto.AdminUpdateRequest;
import com.sadid.university.entity.Admin;
import com.sadid.university.entity.AppUser;
import com.sadid.university.entity.Role;
import com.sadid.university.exception.DuplicateResourceException;
import com.sadid.university.exception.ResourceNotFoundException;
import com.sadid.university.repository.AdminRepository;
import com.sadid.university.repository.AppUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final AdminRepository adminRepository;
    private final AppUserRepository appUserRepository;

    @Transactional(readOnly = true)
    public List<AdminResponse> getAll() {
        return adminRepository.findAll()
                .stream()
                .map(AdminResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public AdminResponse getById(Long id) {
        return AdminResponse.from(findAdminOrThrow(id));
    }

    @Transactional
    public AdminResponse create(AdminCreateRequest request) {
        if (appUserRepository.existsByEmail(request.email())) {
            throw new DuplicateResourceException("Email already in use: " + request.email());
        }

        AppUser user = new AppUser();
        user.setEmail(request.email());
        user.setPassword(request.password()); // TODO Lesson 4: hash with BCrypt
        user.setRole(Role.ADMIN);

        Admin admin = new Admin();
        admin.setName(request.name());
        admin.setUser(user);

        return AdminResponse.from(adminRepository.save(admin));
    }

    @Transactional
    public AdminResponse update(Long id, AdminUpdateRequest request) {
        Admin admin = findAdminOrThrow(id);
        AppUser user = admin.getUser();

        boolean emailChanged = !user.getEmail().equals(request.email());
        if (emailChanged && appUserRepository.existsByEmail(request.email())) {
            throw new DuplicateResourceException("Email already in use: " + request.email());
        }

        admin.setName(request.name());

        user.setEmail(request.email());
        if (request.password() != null) {
            user.setPassword(request.password()); // TODO Lesson 4: hash with BCrypt
        }

        return AdminResponse.from(admin);
    }

    @Transactional
    public void delete(Long id) {
        adminRepository.delete(findAdminOrThrow(id));
    }

    private Admin findAdminOrThrow(Long id) {
        return adminRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Admin not found with id: " + id));
    }
}