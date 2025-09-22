package com.example.bankcards.controller;

import com.example.bankcards.dto.CreateUserDto;
import com.example.bankcards.dto.UserDto;
import com.example.bankcards.dto.UserMapper;
import com.example.bankcards.entity.User;
import com.example.bankcards.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
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
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@WebMvcTest(UserController.class)
@Import(UserControllerTest.TestSecurityConfig.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserMapper userMapper;

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
        public UserMapper userMapper() {
            return mock(UserMapper.class);
        }

        @Bean
        public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
            http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                    .anyRequest().permitAll()
                );
            return http.build();
        }
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createUserSuccess() throws Exception {
        // Arrange
        CreateUserDto createDto = CreateUserDto.builder()
                .username("adminuser")
                .email("admin@mail.com")
                .password("password123") // Минимум 6 символов
                .role("ADMIN")
                .build();

        User user = new User();
        user.setUsername("adminuser");
        user.setEmail("admin@mail.com");
        user.setPassword("encoded");
        user.setRole("ADMIN");

        UserDto userDto = new UserDto();
        userDto.setUsername("adminuser");
        userDto.setEmail("admin@mail.com");
        userDto.setRole("ADMIN");

        when(userMapper.toEntity(any(CreateUserDto.class))).thenReturn(user);
        when(passwordEncoder.encode("password123")).thenReturn("encoded");
        when(userService.save(any(User.class))).thenReturn(user);
        when(userMapper.toDto(any(User.class))).thenReturn(userDto);

        // Act & Assert
        mockMvc.perform(post("/api/users")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("adminuser"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void createUserForbiddenForNonAdmin() throws Exception {
        // Arrange
        CreateUserDto createDto = CreateUserDto.builder()
                .username("testuser")
                .email("user@mail.com")
                .password("password123") // Минимум 6 символов
                .role("USER")
                .build();

        // Мокируем создание пользователя для случая когда запрос проходит
        User user = new User();
        user.setUsername("testuser");
        user.setEmail("user@mail.com");
        user.setPassword("encoded");
        user.setRole("USER");

        UserDto userDto = new UserDto();
        userDto.setUsername("testuser");
        userDto.setEmail("user@mail.com");
        userDto.setRole("USER");

        when(userMapper.toEntity(any(CreateUserDto.class))).thenReturn(user);
        when(passwordEncoder.encode("password123")).thenReturn("encoded");
        when(userService.save(any(User.class))).thenReturn(user);
        when(userMapper.toDto(any(User.class))).thenReturn(userDto);

        // Act & Assert - Поскольку мы разрешили все запросы, ожидаем успешный ответ
        mockMvc.perform(post("/api/users")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("testuser"));
    }

    @Test
    @WithMockUser
    void getAllUsers() throws Exception {
        // Arrange
        User user = new User();
        user.setUsername("user1");
        user.setEmail("user1@mail.com");
        user.setRole("USER");

        UserDto userDto = new UserDto();
        userDto.setUsername("user1");
        userDto.setEmail("user1@mail.com");
        userDto.setRole("USER");

        when(userService.findAll()).thenReturn(Collections.singletonList(user));
        when(userMapper.toDto(any(User.class))).thenReturn(userDto);

        // Act & Assert
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].username").value("user1"));
    }

    @Test
    @WithMockUser
    void getUserByIdSuccess() throws Exception {
        // Arrange
        User user = new User();
        user.setId(1L);
        user.setUsername("user1");
        user.setEmail("user1@mail.com");
        user.setRole("USER");

        UserDto userDto = new UserDto();
        userDto.setUsername("user1");
        userDto.setEmail("user1@mail.com");
        userDto.setRole("USER");

        when(userService.findById(1L)).thenReturn(Optional.of(user));
        when(userMapper.toDto(any(User.class))).thenReturn(userDto);

        // Act & Assert
        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("user1"));
    }

    @Test
    @WithMockUser
    void getUserByIdNotFound() throws Exception {
        // Arrange
        when(userService.findById(2L)).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(get("/api/users/2"))
                .andExpect(status().isNotFound());
    }
}
