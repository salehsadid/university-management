package com.sadid.university.unit;

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

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// Unit test 6: deleting an existing student removes it through the repository
@ExtendWith(MockitoExtension.class)
class DeleteStudentTest {

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private AppUserRepository appUserRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private StudentService studentService;

    @Test
    void delete_removesExistingStudent() {
        AppUser user = new AppUser();
        user.setEmail("rahim@uni.edu");
        user.setRole(Role.STUDENT);

        Student student = new Student();
        student.setId(1L);
        student.setStudentId("S-001");
        student.setUser(user);

        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));

        studentService.delete(1L);

        verify(studentRepository).delete(student);
    }
}
