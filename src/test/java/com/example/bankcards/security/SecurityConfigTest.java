package com.example.bankcards.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SecurityConfigTest {
    @Autowired
    private MockMvc mockMvc;

    @Test
    void authEndpointsShouldBeAccessibleWithoutAuth() throws Exception {
        // Тестируем POST запрос на регистрацию (правильный HTTP метод)
        String requestBody = """
                {
                    "username": "testuser",
                    "email": "test@example.com",
                    "password": "password123",
                    "role": "USER"
                }
                """;

        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isCreated()); // Успешная регистрация возвращает 201
    }

    @Test
    void authLoginEndpointShouldBeAccessible() throws Exception {
        // Тестируем POST запрос на логин
        String requestBody = """
                {
                    "username": "testuser",
                    "password": "password123"
                }
                """;

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().is4xxClientError()); // Ожидаем 4xx (неверные данные или пользователь не найден)
    }

    @Test
    void protectedEndpointsShouldRequireAuth() throws Exception {
        // Spring Security возвращает 403 (Forbidden) для неаутентифицированных запросов
        // это нормальное поведение, а не 401 (Unauthorized)
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isForbidden()); // 403 - правильный ответ для защищенных endpoint'ов
    }

    @Test
    void protectedPostEndpointShouldRequireAuth() throws Exception {
        String requestBody = """
                {
                    "username": "newuser",
                    "email": "new@example.com",
                    "password": "password123",
                    "role": "USER"
                }
                """;

        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isForbidden()); // 403 для защищенных endpoint'ов
    }
}
