package com.ferrumlog.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO para ejercicios del banco de ejercicios.
 */
public record ExerciseDto(
    Long id,
    
    @NotBlank(message = "El nombre del ejercicio es obligatorio")
    @Size(max = 120, message = "El nombre no puede superar 120 caracteres")
    String name,
    
    @NotBlank(message = "El grupo muscular es obligatorio")
    @Size(max = 80, message = "El grupo muscular no puede superar 80 caracteres")
    String muscleGroup,
    
    @Size(max = 1000, message = "La descripción no puede superar 1000 caracteres")
    String description
) {}
