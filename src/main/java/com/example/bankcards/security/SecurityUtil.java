package com.example.bankcards.security;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class SecurityUtil {

    public Authentication getAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    public String getCurrentUsername() {
        Authentication auth = getAuthentication();
        return auth == null ? null : String.valueOf(auth.getPrincipal());
    }

    public Long getCurrentUserId() {
        Authentication auth = getAuthentication();
        if (auth == null) return null;
        Object details = auth.getDetails();
        if (details instanceof UserContext ctx && ctx.getUserId() != null) {
            return ctx.getUserId();
        }
        // Fallback: извлечь цифры из username (на случай legacy токена без userId claim)
        String username = getCurrentUsername();
        if (username == null) return null;
        String digits = username.replaceAll("[^0-9]", "");
        if (digits.isEmpty()) return null;
        try { return Long.valueOf(digits); } catch (NumberFormatException e) { return null; }
    }

    public boolean isAdmin() {
        Authentication auth = getAuthentication();
        if (auth == null) return false;
        for (GrantedAuthority ga : auth.getAuthorities()) {
            if ("ROLE_ADMIN".equals(ga.getAuthority())) return true;
        }
        return false;
    }

    public void assertAuthenticated() {
        if (getAuthentication() == null) {
            throw new AccessDeniedException("Аутентификация требуется");
        }
    }

    public void assertOwner(Long ownerId) {
        if (isAdmin()) return; // администратор без ограничений
        Long current = getCurrentUserId();
        if (current == null || ownerId == null || !current.equals(ownerId)) {
            throw new AccessDeniedException("Доступ запрещён: не владелец ресурса");
        }
    }
}

