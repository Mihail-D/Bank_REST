package com.example.bankcards.security;

import com.example.bankcards.entity.Role;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.nio.charset.StandardCharsets;
import java.security.Key;

@Service
public class JwtService {

    private final String jwtSecret = "yourSecretKeyyourSecretKeyyourSecretKey";
    private final long jwtExpirationMs = 86400000;

    public String generateToken(String username, Role role) {
        Key key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        return Jwts.builder()
                .setSubject(username)
                .claim("role", role.name())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
                .signWith(key)
                .compact();
    }
}
