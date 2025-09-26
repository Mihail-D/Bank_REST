package com.example.bankcards.dto;

import lombok.Data;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Запрос на регистрацию пользователя")
@Data
public class RegisterRequest {
    @Schema(description = "Полное имя", example = "John Doe", maxLength = 100)
    @NotBlank(message = "Name is required")
    @Size(max = 100, message = "Name must be at most 100 characters")
    private String name;

    @Schema(description = "Уникальный логин", example = "jdoe", minLength = 3, maxLength = 50)
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    private String username;

    @Schema(description = "Пароль", example = "P@ssw0rd", minLength = 4, maxLength = 100)
    @NotBlank(message = "Password is required")
    @Size(min = 4, max = 100, message = "Password must be between 4 and 100 characters")
    private String password;

    @Schema(description = "Email", example = "jdoe@example.com")
    @NotBlank(message = "Email is required")
    @Email(message = "Email must be a valid email address")
    private String email;

    @Schema(description = "Роль пользователя", example = "USER")
    private com.example.bankcards.entity.Role role;
}
