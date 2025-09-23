package com.example.bankcards.service.impl;

import com.example.bankcards.entity.User;
import com.example.bankcards.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.Optional;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceImplTest {

    private UserRepository userRepository;
    private UserServiceImpl userService;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        userService = new UserServiceImpl(userRepository);
    }

    @Test
    void testFindByUsername_found() {
        User user = new User();
        user.setUsername("testuser");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));

        Optional<User> result = userService.findByUsername("testuser");
        assertTrue(result.isPresent());
        assertEquals("testuser", result.get().getUsername());
        verify(userRepository, times(1)).findByUsername("testuser");
    }

    @Test
    void testFindByUsername_notFound() {
        when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        Optional<User> result = userService.findByUsername("unknown");
        assertFalse(result.isPresent());
        verify(userRepository, times(1)).findByUsername("unknown");
    }

    @Test
    void testFindByEmail_found() {
        User user = new User();
        user.setEmail("test@mail.com");
        when(userRepository.findByEmail("test@mail.com")).thenReturn(Optional.of(user));

        Optional<User> result = userService.findByEmail("test@mail.com");
        assertTrue(result.isPresent());
        assertEquals("test@mail.com", result.get().getEmail());
        verify(userRepository, times(1)).findByEmail("test@mail.com");
    }

    @Test
    void testFindByEmail_notFound() {
        when(userRepository.findByEmail("unknown@mail.com")).thenReturn(Optional.empty());

        Optional<User> result = userService.findByEmail("unknown@mail.com");
        assertFalse(result.isPresent());
        verify(userRepository, times(1)).findByEmail("unknown@mail.com");
    }

    @Test
    void testSave() {
        User user = new User();
        when(userRepository.save(user)).thenReturn(user);

        User saved = userService.save(user);
        assertNotNull(saved);
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void testFindAll_nonEmpty() {
        User user1 = new User();
        User user2 = new User();
        when(userRepository.findAll()).thenReturn(List.of(user1, user2));

        List<User> users = userService.findAll();
        assertEquals(2, users.size());
        verify(userRepository, times(1)).findAll();
    }

    @Test
    void testFindAll_empty() {
        when(userRepository.findAll()).thenReturn(List.of());

        List<User> users = userService.findAll();
        assertTrue(users.isEmpty());
        verify(userRepository, times(1)).findAll();
    }

    @Test
    void testDeleteById() {
        userService.deleteById(1L);
        verify(userRepository, times(1)).deleteById(1L);
    }

    @Test
    void testFindById_found() {
        User user = new User();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        Optional<User> result = userService.findById(1L);
        assertTrue(result.isPresent());
        verify(userRepository, times(1)).findById(1L);
    }

    @Test
    void testFindById_notFound() {
        when(userRepository.findById(2L)).thenReturn(Optional.empty());

        Optional<User> result = userService.findById(2L);
        assertFalse(result.isPresent());
        verify(userRepository, times(1)).findById(2L);
    }
}
