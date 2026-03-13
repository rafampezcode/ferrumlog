package com.ferrumlog.dto;

/**
 * DTO para la respuesta del usuario (SIN password).
 * Usado para mostrar información del usuario en el frontend de forma segura.
 */
public record UserResponseDto(
    Long id,
    String username,
    String email
) {}
