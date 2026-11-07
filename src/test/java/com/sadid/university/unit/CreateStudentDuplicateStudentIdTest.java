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

// Unit test 3: creating a student with a student ID that already exists fails
@ExtendWith(MockitoExtension.class)
class CreateStudentDuplicateStudentIdTest {

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private AppUserRepository appUserRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private StudentService studentService;

    @Test
    void create_throwsWhenStudentIdAlreadyExists() {
        StudentCreateRequest request = new StudentCreateRequest("S-001", "Rahim", "rahim@uni.edu",
                "secret123", "CSE", "3-2", "01712345678");

        when(appUserRepository.existsByEmail("rahim@uni.edu")).thenReturn(false);
        when(studentRepository.existsByStudentId("S-001")).thenReturn(true);

        assertThatThrownBy(() -> studentService.create(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("Student ID already exists");

        verify(studentRepository, never()).save(any());
    }
}
