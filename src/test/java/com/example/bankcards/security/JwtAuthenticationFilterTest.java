package com.example.bankcards.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class JwtAuthenticationFilterTest {
    @Test
    void doFilterInternalWithNoToken() throws ServletException, IOException {
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter();
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = Mockito.mock(FilterChain.class);
        SecurityContextHolder.clearContext();
        filter.doFilterInternal(request, response, chain);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }
}

