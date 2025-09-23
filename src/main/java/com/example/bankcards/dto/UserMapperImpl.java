package com.example.bankcards.dto;

import com.example.bankcards.entity.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapperImpl implements UserMapper {

    @Override
    public UserDto toDto(User user) {
        if (user == null) {
            return null;
        }

        return UserDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole())
                // password намеренно не устанавливаем, так как он не должен возвращаться в DTO
                .build();
    }

    @Override
    public User toEntity(UserDto userDto) {
        if (userDto == null) {
            return null;
        }

        return User.builder()
                .id(userDto.getId())
                .username(userDto.getUsername())
                .email(userDto.getEmail())
                .role(userDto.getRole())
                .password(userDto.getPassword())
                .build();
    }

    @Override
    public User toEntity(CreateUserDto createUserDto) {
        if (createUserDto == null) {
            return null;
        }

        return User.builder()
                .username(createUserDto.getUsername())
                .email(createUserDto.getEmail())
                .role(createUserDto.getRole())
                .password(createUserDto.getPassword())
                .build();
    }
}
