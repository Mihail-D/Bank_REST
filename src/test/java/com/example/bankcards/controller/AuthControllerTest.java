package com.example.bankcards.controller;

import com.example.bankcards.dto.AuthRequest;
import com.example.bankcards.dto.RegisterRequest;
import com.example.bankcards.dto.UserMapper;
import com.example.bankcards.entity.User;
import com.example.bankcards.entity.Role;
import com.example.bankcards.service.UserService;
import com.example.bankcards.security.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@Import(AuthControllerTest.TestSecurityConfig.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private ObjectMapper objectMapper;

    @TestConfiguration
    @EnableWebSecurity
    static class TestSecurityConfig {

        @Bean
        @Primary
        public UserService userService() {
            return mock(UserService.class);
        }

        @Bean
        @Primary
        public PasswordEncoder passwordEncoder() {
            return mock(PasswordEncoder.class);
        }

        @Bean
        @Primary
        public JwtService jwtService() {
            return mock(JwtService.class);
        }

        @Bean
        @Primary
        public UserMapper userMapper() {
            return mock(UserMapper.class);
        }

        @Bean
        public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
            http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                    .requestMatchers("/auth/**").permitAll()
                    .anyRequest().authenticated()
                );
            return http.build();
        }
    }

    @Test
    void registerSuccess() throws Exception {
        // Arrange
        RegisterRequest request = new RegisterRequest();
        request.setName("New User");
        request.setUsername("newuser");
        request.setEmail("new@mail.com");
        request.setPassword("password123");
        request.setRole(Role.USER);

        when(userService.findByUsername("newuser")).thenReturn(Optional.empty());
        when(userService.findByEmail("new@mail.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("password123")).thenReturn("encoded");

        User user = new User();
        user.setName("New User");
        user.setUsername("newuser");
        user.setEmail("new@mail.com");
        user.setPassword("encoded");
        user.setRole(Role.USER);

        when(userService.save(any())).thenReturn(user);

        // Act & Assert
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    void registerUserAlreadyExists() throws Exception {
        // Arrange
        RegisterRequest request = new RegisterRequest();
        request.setName("Existing User");
        request.setUsername("existinguser");
        request.setEmail("existing@mail.com");
        request.setPassword("password123");
        request.setRole(Role.USER);

        when(userService.findByUsername("existinguser")).thenReturn(Optional.of(new User()));

        // Act & Assert
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    void registerEmailAlreadyExists() throws Exception {
        // Arrange
        RegisterRequest request = new RegisterRequest();
        request.setName("New User");
        request.setUsername("newuser");
        request.setEmail("existing@mail.com");
        request.setPassword("password123");
        request.setRole(Role.USER);

        when(userService.findByUsername("newuser")).thenReturn(Optional.empty());
        when(userService.findByEmail("existing@mail.com")).thenReturn(Optional.of(new User()));

        // Act & Assert
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    void loginSuccess() throws Exception {
        // Arrange
        AuthRequest request = new AuthRequest();
        request.setUsername("testuser");
        request.setPassword("password123");

        User user = new User();
        user.setUsername("testuser");
        user.setPassword("encodedPassword");
        user.setRole(Role.USER);

        when(userService.findByUsername("testuser")).thenReturn(java.util.Optional.of(user));
        when(passwordEncoder.matches("password123", "encodedPassword")).thenReturn(true);
        // Мокаем оба варианта на случай использования legacy или нового метода
        when(jwtService.generateToken("testuser", Role.USER)).thenReturn("jwt-token");
        when(jwtService.generateToken(any(User.class))).thenReturn("jwt-token");

        // Act & Assert
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token"));
    }

    @Test
    void loginUserNotFound() throws Exception {
        // Arrange
        AuthRequest request = new AuthRequest();
        request.setUsername("nonexistent");
        request.setPassword("password123");

        when(userService.findByUsername("nonexistent")).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void loginWrongPassword() throws Exception {
        // Arrange
        AuthRequest request = new AuthRequest();
        request.setUsername("testuser");
        request.setPassword("wrongpassword");

        User user = new User();
        user.setUsername("testuser");
        user.setPassword("encodedPassword");
        user.setRole(Role.USER);

        when(userService.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrongpassword", "encodedPassword")).thenReturn(false);

        // Act & Assert
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }
}
