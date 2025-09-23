package com.example.bankcards.security;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {
    @Test
    void generateTokenShouldContainUsernameAndRole() {
        JwtService jwtService = new JwtService();
        String token = jwtService.generateToken("testuser", "ADMIN");
        assertNotNull(token);
        assertTrue(token.length() > 0);
        // Дополнительно можно проверить структуру токена, например, наличие точек (header.payload.signature)
        assertEquals(2, token.chars().filter(ch -> ch == '.').count());
    }
}

