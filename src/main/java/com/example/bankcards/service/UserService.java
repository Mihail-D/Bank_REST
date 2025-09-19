package com.example.bankcards.service;

import com.example.bankcards.entity.User;
import java.util.Optional;
import java.util.List;

public interface UserService {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    Optional<User> findById(Long id);
    User save(User user);
    List<User> findAll();
    void deleteById(Long id);
}
