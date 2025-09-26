package com.example.health_app.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.example.health_app.constant.AppMessage;
import com.example.health_app.entity.Role;
import com.example.health_app.entity.User;
import com.example.health_app.repository.UserRepository;

/**
 * Mockの単体テスト用
 * ・ユーザー登録（成功・失敗）
 * ・パスワード変更
 */
@ExtendWith(MockitoExtension.class)
class UserServiceTest {
	@Mock UserRepository userRepository;
    @Mock PasswordEncoder passwordEncoder;

    @InjectMocks UserService userService;

    User newUser;

    @BeforeEach
    void setup() {
        newUser = new User();
        newUser.setUsername("testuser");
        newUser.setEmail("test@example.com");
        newUser.setPassword("plainpass");
    }

    @Test
    void registerUser_success() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("plainpass")).thenReturn("hashedpass");

        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(1L);
            return u;
        });

        User saved = userService.registerUser(newUser);

        assertNotNull(saved.getId());
        assertEquals("hashedpass", saved.getPassword());
        assertTrue(saved.getRoles().contains(Role.ROLE_USER));
    }

    @Test
    void registerUser_duplicateEmail_throws() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(new User()));

        IllegalArgumentException ex =
                assertThrows(IllegalArgumentException.class, () -> userService.registerUser(newUser));
        assertEquals(AppMessage.USERNAME_ALREADY_USED, ex.getMessage());
    }

    @Test
    void changePassword_success() {
        User user = new User();
        user.setId(99L);
        user.setPassword("oldpass");

        when(userRepository.findById(99L)).thenReturn(Optional.of(user));
        when(passwordEncoder.encode("newpass")).thenReturn("hashedNew");

        userService.changePassword(99L, "newpass");

        assertEquals("hashedNew", user.getPassword());
        verify(userRepository).save(user);
    }

    @Test
    void changePassword_userNotFound_throws() {
        when(userRepository.findById(123L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> userService.changePassword(123L, "x"));
    }
}