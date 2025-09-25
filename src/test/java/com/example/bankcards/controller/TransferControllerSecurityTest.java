package com.example.bankcards.controller;

import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.Transfer;
import com.example.bankcards.entity.User;
import com.example.bankcards.security.PermissionService;
import com.example.bankcards.service.TransferService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TransferController.class)
@Import(TransferControllerSecurityTest.Config.class)
class TransferControllerSecurityTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private TransferService transferService;
    @Autowired
    private PermissionService permissionService;

    private Transfer testTransfer;
    private User testUser;

    @TestConfiguration
    static class Config {
        @Bean
        public TransferService transferService() {
            return Mockito.mock(TransferService.class);
        }
        @Bean
        public PermissionService permissionService() {
            return Mockito.mock(PermissionService.class);
        }
    }

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        Card sourceCard = new Card();
        sourceCard.setUser(testUser);
        testTransfer = new Transfer();
        testTransfer.setId(100L);
        testTransfer.setSourceCard(sourceCard);
    }

    @Test
    @WithMockUser(username = "user1", roles = {"USER"})
    void getTransfer_shouldReturnForbidden_whenUserIsNotOwner() throws Exception {
        when(permissionService.isTransferOwner(100L, 1L)).thenReturn(false);
        mockMvc.perform(get("/api/transfers/100").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "user1", roles = {"USER"})
    void getTransfer_shouldReturnOk_whenUserIsOwner() throws Exception {
        when(permissionService.isTransferOwner(100L, 1L)).thenReturn(true);
        when(transferService.getTransferById(100L)).thenReturn(Optional.of(testTransfer));
        mockMvc.perform(get("/api/transfers/100").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void getTransfer_shouldReturnOk_whenAdmin() throws Exception {
        when(transferService.getTransferById(100L)).thenReturn(Optional.of(testTransfer));
        mockMvc.perform(get("/api/transfers/100").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void getTransfer_shouldReturnUnauthorized_whenNoAuth() throws Exception {
        mockMvc.perform(get("/api/transfers/100").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }
}
