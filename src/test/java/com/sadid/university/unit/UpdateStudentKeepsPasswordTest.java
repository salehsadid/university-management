package com.sadid.university.unit;

import com.sadid.university.dto.StudentResponse;
import com.sadid.university.dto.StudentUpdateRequest;
import com.sadid.university.entity.AppUser;
import com.sadid.university.entity.Role;
import com.sadid.university.entity.Student;
import com.sadid.university.repository.AppUserRepository;
import com.sadid.university.repository.StudentRepository;
import com.sadid.university.service.StudentService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// Unit test 5: updating a student without a new password keeps the old password
@ExtendWith(MockitoExtension.class)
class UpdateStudentKeepsPasswordTest {

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private AppUserRepository appUserRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private StudentService studentService;

    @Test
    void update_keepsOldPasswordWhenPasswordIsNull() {
        AppUser user = new AppUser();
        user.setEmail("old@uni.edu");
        user.setPassword("old-hash");
        user.setRole(Role.STUDENT);

        Student student = new Student();
        student.setId(1L);
        student.setStudentId("S-001");
        student.setName("Rahim");
        student.setDepartment("CSE");
        student.setSemester("3-2");
        student.setUser(user);

        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
        when(appUserRepository.existsByEmail("new@uni.edu")).thenReturn(false);

        StudentUpdateRequest request = new StudentUpdateRequest("S-001", "Rahim Uddin", "new@uni.edu",
                null, "EEE", "4-1", null);

        StudentResponse response = studentService.update(1L, request);

        assertThat(response.name()).isEqualTo("Rahim Uddin");
        assertThat(response.email()).isEqualTo("new@uni.edu");
        assertThat(response.department()).isEqualTo("EEE");
        assertThat(user.getPassword()).isEqualTo("old-hash");
        verify(passwordEncoder, never()).encode(any());
    }
}
