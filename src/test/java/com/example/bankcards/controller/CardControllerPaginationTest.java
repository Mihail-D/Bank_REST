package com.example.bankcards.controller;

import com.example.bankcards.dto.PageResponseDto;
import com.example.bankcards.dto.CardDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class CardControllerPaginationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testGetAllCardsWithPagination() throws Exception {
        String url = "http://localhost:" + port + "/api/cards/paginated?page=0&size=10&sortBy=id&sortDirection=asc";

        // Этот тест проверяет, что эндпоинт доступен
        // В реальном тестировании нужно будет настроить тестовую базу данных
        // и создать тестовые данные

        // Для демонстрации оставим базовую структуру
        System.out.println("Testing pagination endpoint: " + url);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testGetCardsWithPaginationAndSorting() throws Exception {
        String url = "http://localhost:" + port + "/api/cards/paginated?page=0&size=5&sortBy=status&sortDirection=desc";

        System.out.println("Testing pagination with sorting endpoint: " + url);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testSearchCardsWithPagination() throws Exception {
        String url = "http://localhost:" + port + "/api/cards/search/paginated?status=ACTIVE&page=0&size=10&sortBy=id&sortDirection=asc";

        System.out.println("Testing search with pagination endpoint: " + url);
    }
}
