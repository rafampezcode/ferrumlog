package com.ferrumlog.service;

import com.ferrumlog.dto.UserRegistrationDto;
import com.ferrumlog.dto.UserResponseDto;

/**
 * Interfaz de servicio para la gestión de usuarios.
 */
public interface UserService {

    /**
     * Registra un nuevo usuario en el sistema.
     * La contraseña se cifra con BCrypt antes de guardarla.
     * 
     * @param registrationDto datos del nuevo usuario
     * @return el usuario registrado
     * @throws IllegalArgumentException si el username o email ya existen
     */
    UserResponseDto registerUser(UserRegistrationDto registrationDto);

    /**
     * Busca un usuario por su username.
     * 
     * @param username nombre de usuario
     * @return el usuario encontrado
     * @throws RuntimeException si no existe
     */
    UserResponseDto findByUsername(String username);

    /**
     * Verifica si un username ya está en uso.
     * 
     * @param username nombre de usuario a verificar
     * @return true si existe, false en caso contrario
     */
    boolean existsByUsername(String username);

    /**
     * Verifica si un email ya está en uso.
     * 
     * @param email correo electrónico a verificar
     * @return true si existe, false en caso contrario
     */
    boolean existsByEmail(String email);
}
