package com.ecommerce.userservice.service;

import com.ecommerce.userservice.dto.AuthRequest;
import com.ecommerce.userservice.dto.AuthResponse;
import com.ecommerce.userservice.dto.RegisterRequest;
import com.ecommerce.userservice.entity.User;
import com.ecommerce.userservice.repository.UserRepository;
import com.ecommerce.userservice.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    
    @Mock
    private PasswordEncoder passwordEncoder;

    private UserService userService;

    private RegisterRequest request;

    @BeforeEach
    void setUp() {
        request = new RegisterRequest("Test User", "test@example.com", "password123");
        JwtUtil jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secret", "QUJDREVGR0hJSktMTU5PUFFSU1RVVldYWVo2Nzg5MDEyMzQ1Njc4OTA=");
        ReflectionTestUtils.setField(jwtUtil, "expirationTime", 86400000L);
        userService = new UserService(userRepository, passwordEncoder, jwtUtil);
    }

    @Test
    void testRegister_ExistingEmailThrowsException() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(new User()));
        
        assertThrows(IllegalArgumentException.class, () -> {
            userService.register(request);
        });
        
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testLogin_ReturnsTokenAndUser() {
        User user = new User("test@example.com", "encoded-password", "Test User", "USER");
        user.setId(1L);
        AuthRequest loginRequest = new AuthRequest("test@example.com", "password123");

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password123", "encoded-password")).thenReturn(true);

        AuthResponse response = userService.login(loginRequest);

        assertEquals("Bearer", response.type());
        assertEquals(86400000L, response.expiresIn());
        assertEquals("test@example.com", response.user().email());
        assertFalse(response.token().isBlank());
    }

    @Test
    void testLogin_InvalidPasswordThrowsException() {
        User user = new User("test@example.com", "encoded-password", "Test User", "USER");
        AuthRequest loginRequest = new AuthRequest("test@example.com", "wrong-password");

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong-password", "encoded-password")).thenReturn(false);

        assertThrows(BadCredentialsException.class, () -> userService.login(loginRequest));
    }
}
