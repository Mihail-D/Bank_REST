package com.example.bankcards.service;

import com.example.bankcards.dto.AuthRequest;
import com.example.bankcards.dto.AuthResponse;
import com.example.bankcards.dto.RegisterRequest;
import com.example.bankcards.entity.Role;
import com.example.bankcards.entity.User;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AuthServiceTest {
    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtService jwtService;
    @Mock
    private AuthenticationManager authenticationManager;
    @InjectMocks
    private AuthService authService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        authService = new AuthService(userRepository, passwordEncoder, jwtService, authenticationManager);
    }

    @Test
    void register_success() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("user1");
        request.setPassword("pass");
        request.setEmail("user1@mail.com");
        when(userRepository.existsByUsername("user1")).thenReturn(false);
        when(userRepository.existsByEmail("user1@mail.com")).thenReturn(false);
        when(passwordEncoder.encode("pass")).thenReturn("encoded");
        User savedUser = new User();
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        assertDoesNotThrow(() -> authService.register(request));
    }

    @Test
    void register_usernameExists() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("user1");
        request.setEmail("user1@mail.com");
        when(userRepository.existsByUsername("user1")).thenReturn(true);
        assertThrows(IllegalArgumentException.class, () -> authService.register(request));
    }

    @Test
    void register_emailExists() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("user1");
        request.setEmail("user1@mail.com");
        when(userRepository.existsByUsername("user1")).thenReturn(false);
        when(userRepository.existsByEmail("user1@mail.com")).thenReturn(true);
        assertThrows(IllegalArgumentException.class, () -> authService.register(request));
    }

    @Test
    void authenticate_success() {
        AuthRequest request = new AuthRequest();
        request.setUsername("user1");
        request.setPassword("pass");
        User user = new User();
        user.setUsername("user1");
        user.setRole(Role.USER);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(mock(Authentication.class));
        when(userRepository.findByUsername("user1")).thenReturn(Optional.of(user));
        when(jwtService.generateToken("user1", Role.USER)).thenReturn("token");
        AuthResponse response = authService.authenticate(request);
        assertEquals("token", response.getToken());
    }

    @Test
    void authenticate_invalidCredentials() {
        AuthRequest request = new AuthRequest();
        request.setUsername("user1");
        request.setPassword("wrong");
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenThrow(new BadCredentialsException("Bad credentials"));
        assertThrows(IllegalArgumentException.class, () -> authService.authenticate(request));
    }
}
