package com.example.bankcards.service;

import com.example.bankcards.entity.User;
import com.example.bankcards.entity.Role;
import java.util.Optional;
import java.util.List;

public interface UserService {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    Optional<User> findById(Long id);
    User save(User user);
    List<User> findAll();
    void deleteById(Long id);

    // Дополнительные методы для управления пользователями
    List<User> findByRole(Role role);
    List<User> findActiveUsers();
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    void activateUser(Long userId);
    void deactivateUser(Long userId);
    void updateUserRole(Long userId, Role role);
}
