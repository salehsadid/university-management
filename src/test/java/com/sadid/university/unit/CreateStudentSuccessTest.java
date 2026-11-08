package com.sadid.university.unit;

import com.sadid.university.dto.StudentCreateRequest;
import com.sadid.university.dto.StudentResponse;
import com.sadid.university.entity.Role;
import com.sadid.university.entity.Student;
import com.sadid.university.repository.AppUserRepository;
import com.sadid.university.repository.StudentRepository;
import com.sadid.university.service.StudentService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// Unit test 1: creating a student hashes the password and gives the STUDENT role
@ExtendWith(MockitoExtension.class)
class CreateStudentSuccessTest {

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private AppUserRepository appUserRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private StudentService studentService;

    @Test
    void create_savesStudentWithHashedPasswordAndStudentRole() {
        StudentCreateRequest request = new StudentCreateRequest("S-001", "Rahim", "rahim@uni.edu",
                "secret123", "CSE", "3-2", "01712345678");

        when(appUserRepository.existsByEmail("rahim@uni.edu")).thenReturn(false);
        when(studentRepository.existsByStudentId("S-001")).thenReturn(false);
        when(passwordEncoder.encode("secret123")).thenReturn("hashed-pw");
        when(studentRepository.save(any(Student.class))).thenAnswer(inv -> {
            Student s = inv.getArgument(0);
            s.setId(10L);
            return s;
        });

        StudentResponse response = studentService.create(request);

        ArgumentCaptor<Student> captor = ArgumentCaptor.forClass(Student.class);
        verify(studentRepository).save(captor.capture());
        Student saved = captor.getValue();

        assertThat(saved.getUser().getPassword()).isEqualTo("hashed-pw");
        assertThat(saved.getUser().getRole()).isEqualTo(Role.STUDENT);
        assertThat(response.id()).isEqualTo(10L);
        assertThat(response.email()).isEqualTo("rahim@uni.edu");
    }
}
