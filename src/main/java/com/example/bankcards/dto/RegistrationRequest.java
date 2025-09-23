package com.example.bankcards.dto;

import com.example.bankcards.entity.Role;
import lombok.Data;

@Data
public class RegistrationRequest {
    private String name;
    private String username;
    private String password;
    private String email;
    private Role role;
}
