package com.example.bankcards.repository;

import com.example.bankcards.entity.User;
import com.example.bankcards.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.List;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);

    // Дополнительные методы для управления пользователями
    List<User> findByRole(Role role);
    List<User> findByActiveTrue();
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
}
