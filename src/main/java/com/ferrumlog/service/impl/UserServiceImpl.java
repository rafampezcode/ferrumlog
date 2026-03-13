package com.ferrumlog.service.impl;

import com.ferrumlog.domain.entity.User;
import com.ferrumlog.dto.UserRegistrationDto;
import com.ferrumlog.dto.UserResponseDto;
import com.ferrumlog.repository.UserRepository;
import com.ferrumlog.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementación del servicio de usuarios.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public UserResponseDto registerUser(UserRegistrationDto registrationDto) {
        log.info("Registrando nuevo usuario: {}", registrationDto.username());

        // Validar que username no existe
        if (userRepository.existsByUsername(registrationDto.username())) {
            throw new IllegalArgumentException(
                    "El nombre de usuario ya está en uso: " + registrationDto.username());
        }

        // Validar que email no existe
        if (userRepository.existsByEmail(registrationDto.email())) {
            throw new IllegalArgumentException(
                    "El email ya está en uso: " + registrationDto.email());
        }

        // Crear usuario con contraseña cifrada
        User user = User.builder()
                .username(registrationDto.username())
                .email(registrationDto.email())
                .password(passwordEncoder.encode(registrationDto.password()))
                .build();

        User savedUser = userRepository.save(user);
        log.info("Usuario registrado exitosamente: {} (ID: {})", 
                savedUser.getUsername(), savedUser.getId());

        return mapToDto(savedUser);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponseDto findByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado: " + username));
        return mapToDto(user);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    private UserResponseDto mapToDto(User user) {
        return new UserResponseDto(
                user.getId(),
                user.getUsername(),
                user.getEmail()
        );
    }
}
