package com.sadid.university.config;

import com.sadid.university.dto.AdminCreateRequest;
import com.sadid.university.repository.AdminRepository;
import com.sadid.university.repository.AppUserRepository;
import com.sadid.university.service.AdminService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

// Creates the first admin on startup so someone can log in and create other users.
@Slf4j
@Component
@RequiredArgsConstructor
public class AdminSeeder implements CommandLineRunner {

    private final AdminRepository adminRepository;
    private final AppUserRepository appUserRepository;
    private final AdminService adminService;

    @Value("${app.admin.name:Super Admin}")
    private String name;

    @Value("${app.admin.email:admin@uni.edu}")
    private String email;

    @Value("${app.admin.password:admin123}")
    private String password;

    @Override
    public void run(String... args) {
        if (adminRepository.count() > 0 || appUserRepository.existsByEmail(email)) {
            log.info(">>> Admin already exists, skipping seeding");
            return;
        }

        adminService.create(new AdminCreateRequest(name, email, password));
        log.info(">>> Initial admin created: {}", email);
    }
}
