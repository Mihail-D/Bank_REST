package com.example.bankcards.integration;

import com.example.bankcards.entity.Role;
import com.example.bankcards.entity.User;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class UserManagementIntegrationTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Test
    void shouldCompleteUserManagementWorkflow() {
        // 1. Создание пользователя
        User user = new User("John Doe", "johndoe", "john@example.com", "password123", Role.USER);
        User savedUser = userService.save(user);

        assertThat(savedUser.getId()).isNotNull();
        assertThat(savedUser.getName()).isEqualTo("John Doe");
        assertThat(savedUser.getRole()).isEqualTo(Role.USER);
        assertThat(savedUser.isActive()).isTrue();

        // 2. Поиск пользователя
        Optional<User> foundByUsername = userService.findByUsername("johndoe");
        Optional<User> foundByEmail = userService.findByEmail("john@example.com");

        assertThat(foundByUsername).isPresent();
        assertThat(foundByEmail).isPresent();
        assertThat(foundByUsername.get().getId()).isEqualTo(savedUser.getId());

        // 3. Обновление роли пользователя
        userService.updateUserRole(savedUser.getId(), Role.ADMIN);
        User updatedUser = userService.findById(savedUser.getId()).orElseThrow();
        assertThat(updatedUser.getRole()).isEqualTo(Role.ADMIN);

        // 4. Деактивация пользователя
        userService.deactivateUser(savedUser.getId());
        User deactivatedUser = userService.findById(savedUser.getId()).orElseThrow();
        assertThat(deactivatedUser.isActive()).isFalse();

        // 5. Активация пользователя
        userService.activateUser(savedUser.getId());
        User activatedUser = userService.findById(savedUser.getId()).orElseThrow();
        assertThat(activatedUser.isActive()).isTrue();

        // 6. Поиск по роли и активности
        List<User> adminUsers = userService.findByRole(Role.ADMIN);
        List<User> activeUsers = userService.findActiveUsers();

        assertThat(adminUsers).hasSize(1);
        assertThat(activeUsers).hasSizeGreaterThanOrEqualTo(1);
        assertThat(adminUsers.get(0).getId()).isEqualTo(savedUser.getId());

        // 7. Проверка существования
        boolean usernameExists = userService.existsByUsername("johndoe");
        boolean emailExists = userService.existsByEmail("john@example.com");

        assertThat(usernameExists).isTrue();
        assertThat(emailExists).isTrue();

        // 8. Удаление пользователя
        userService.deleteById(savedUser.getId());
        Optional<User> deletedUser = userService.findById(savedUser.getId());
        assertThat(deletedUser).isEmpty();
    }

    @Test
    void shouldTestEnumRoleTypes() {
        // Создаем пользователей с разными ролями
        User adminUser = new User("Admin User", "admin", "admin@example.com", "password", Role.ADMIN);
        User regularUser = new User("Regular User", "user", "user@example.com", "password", Role.USER);

        userService.save(adminUser);
        userService.save(regularUser);

        // Проверяем поиск по ролям
        List<User> admins = userService.findByRole(Role.ADMIN);
        List<User> users = userService.findByRole(Role.USER);

        assertThat(admins).hasSizeGreaterThanOrEqualTo(1);
        assertThat(users).hasSizeGreaterThanOrEqualTo(1);

        // Проверяем, что роли сохраняются корректно
        User foundAdmin = userService.findByUsername("admin").orElseThrow();
        User foundUser = userService.findByUsername("user").orElseThrow();

        assertThat(foundAdmin.getRole()).isEqualTo(Role.ADMIN);
        assertThat(foundUser.getRole()).isEqualTo(Role.USER);
    }
}
