package com.example.bankcards.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Запрос на аутентификацию пользователя")
@Data
public class AuthRequest {
    @Schema(description = "Имя пользователя", example = "jdoe")
    @NotBlank(message = "Username is required")
    private String username;

    @Schema(description = "Пароль пользователя", example = "P@ssw0rd")
    @NotBlank(message = "Password is required")
    private String password;
}
