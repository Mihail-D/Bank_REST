package com.example.bankcards.controller;

import com.example.bankcards.entity.History;
import com.example.bankcards.entity.User;
import com.example.bankcards.security.PermissionService;
import com.example.bankcards.service.HistoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class HistoryControllerSecurityTest {
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private HistoryService historyService;
    @MockBean
    private PermissionService permissionService;

    private History testHistory;
    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testHistory = new History();
        testHistory.setId(200L);
        testHistory.setUser(testUser);
    }

    @Test
    @WithMockUser(username = "user1", roles = {"USER"})
    void getHistory_shouldReturnForbidden_whenUserIsNotOwner() throws Exception {
        when(permissionService.isHistoryOwner(200L, "user1")).thenReturn(false);
        when(historyService.getHistoryById(200L)).thenReturn(Optional.of(testHistory));
        mockMvc.perform(get("/api/history/200").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
        Mockito.verify(permissionService).isHistoryOwner(200L, "user1");
    }

    @Test
    @WithMockUser(username = "user1", roles = {"USER"})
    void getHistory_shouldReturnOk_whenUserIsOwner() throws Exception {
        when(permissionService.isHistoryOwner(200L, "user1")).thenReturn(true);
        when(historyService.getHistoryById(200L)).thenReturn(Optional.of(testHistory));
        mockMvc.perform(get("/api/history/200").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void getHistory_shouldReturnOk_whenAdmin() throws Exception {
        when(historyService.getHistoryById(200L)).thenReturn(Optional.of(testHistory));
        mockMvc.perform(get("/api/history/200").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void getHistory_shouldReturnUnauthorized_whenNoAuth() throws Exception {
        mockMvc.perform(get("/api/history/200").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }
}
