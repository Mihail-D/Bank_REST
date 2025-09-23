package com.example.bankcards.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.nio.charset.StandardCharsets;
import java.security.Key;

@Service
public class JwtService {

    private final String jwtSecret = "yourSecretKeyyourSecretKeyyourSecretKey"; // минимум 32 символа
    private final long jwtExpirationMs = 86400000;

    public String generateToken(String username, String role) {
        Key key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        return Jwts.builder()
                .setSubject(username)
                .claim("role", role)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
                .signWith(key)
                .compact();
    }

}
