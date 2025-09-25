package com.example.bankcards.security;

import java.io.Serializable;

/**
 * Данные аутентифицированного пользователя, помещаются в Authentication.getDetails().
 */
public class UserContext implements Serializable {
    private final Long userId;
    private final String username;

    public UserContext(Long userId, String username) {
        this.userId = userId;
        this.username = username;
    }

    public Long getUserId() { return userId; }
    public String getUsername() { return username; }
}

