package com.example.bankcards.security;

import com.example.bankcards.entity.Role;
import com.example.bankcards.entity.User;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.nio.charset.StandardCharsets;
import java.security.Key;

@Service
public class JwtService {

    // Читаем из переменных окружения с дефолтами для локальной разработки и тестов
    private final String jwtSecret = System.getenv().getOrDefault("JWT_SECRET", "yourSecretKeyyourSecretKeyyourSecretKey");
    private final long jwtExpirationMs = Long.parseLong(System.getenv().getOrDefault("JWT_EXPIRATION_MS", "86400000"));

    public String generateToken(String username, Role role) { // legacy вариант без userId (оставлен для совместимости тестов/старых вызовов)
        Key key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        return Jwts.builder()
                .setSubject(username)
                .claim("role", role.name())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
                .signWith(key)
                .compact();
    }

    // Новый предпочтительный метод: добавляем userId как отдельный claim
    public String generateToken(User user) {
        Key key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        return Jwts.builder()
                .setSubject(user.getUsername())
                .claim("role", user.getRole().name())
                .claim("userId", user.getId())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
                .signWith(key)
                .compact();
    }

    public String getSecret() { // для фильтра (можно позже вынести в отдельный бин)
        return jwtSecret;
    }
}
