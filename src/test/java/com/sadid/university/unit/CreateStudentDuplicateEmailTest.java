package com.sadid.university.unit;

import com.sadid.university.dto.StudentCreateRequest;
import com.sadid.university.exception.DuplicateResourceException;
import com.sadid.university.repository.AppUserRepository;
import com.sadid.university.repository.StudentRepository;
import com.sadid.university.service.StudentService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// Unit test 2: creating a student with an email that is already used fails
@ExtendWith(MockitoExtension.class)
class CreateStudentDuplicateEmailTest {

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private AppUserRepository appUserRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private StudentService studentService;

    @Test
    void create_throwsWhenEmailAlreadyUsed() {
        StudentCreateRequest request = new StudentCreateRequest("S-001", "Rahim", "rahim@uni.edu",
                "secret123", "CSE", "3-2", "01712345678");

        when(appUserRepository.existsByEmail("rahim@uni.edu")).thenReturn(true);

        assertThatThrownBy(() -> studentService.create(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("Email already in use");

        verify(studentRepository, never()).save(any());
    }
}
