package com.example.bankcards.controller;

import com.example.bankcards.dto.AuthRequest;
import com.example.bankcards.dto.RegisterRequest;
import com.example.bankcards.service.UserService;
import com.example.bankcards.security.JwtService;
import com.example.bankcards.entity.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.http.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.BadCredentialsException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Auth", description = "Аутентификация и регистрация")
@RestController
@RequestMapping("/auth")
public class AuthController {
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Autowired
    public AuthController(UserService userService, PasswordEncoder passwordEncoder, JwtService jwtService, com.example.bankcards.dto.UserMapper userMapper) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        // userMapper оставлен в сигнатуре конструктора для совместимости конфигураций теста, но не используется здесь напрямую
    }

    @Operation(summary = "Регистрация пользователя", description = "Создаёт нового пользователя", security = {})
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Создано",
                    content = @Content(mediaType = "text/plain")),
            @ApiResponse(ref = "BadRequest"),
            @ApiResponse(ref = "Conflict"),
            @ApiResponse(ref = "InternalServerError")
    })
    @PostMapping("/register")
    public ResponseEntity<String> register(@Valid @RequestBody RegisterRequest request) {
        userService.findByUsername(request.getUsername()).ifPresent(u -> {
            throw new DataIntegrityViolationException("Username already exists");
        });
        userService.findByEmail(request.getEmail()).ifPresent(u -> {
            throw new DataIntegrityViolationException("Email already exists");
        });
        User user = new User();
        user.setName(request.getName());
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(request.getRole() != null ? request.getRole() : com.example.bankcards.entity.Role.USER);
        userService.save(user);
        return ResponseEntity.status(HttpStatus.CREATED).body("User registered successfully");
    }

    @Operation(summary = "Логин", description = "Возвращает JWT токен при корректных учётных данных", security = {})
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Успешная аутентификация",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = com.example.bankcards.dto.AuthResponse.class))),
            @ApiResponse(ref = "BadRequest"),
            @ApiResponse(ref = "Unauthorized"),
            @ApiResponse(ref = "InternalServerError")
    })
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody AuthRequest request) {
        return userService.findByUsername(request.getUsername())
                .map(user -> {
                    if (passwordEncoder.matches(request.getPassword(), user.getPassword())) {
                        String token = jwtService.generateToken(user);
                        return ResponseEntity.ok(java.util.Collections.singletonMap("token", token));
                    } else {
                        throw new BadCredentialsException("Invalid username or password");
                    }
                })
                .orElseThrow(() -> new BadCredentialsException("Invalid username or password"));
    }
}
