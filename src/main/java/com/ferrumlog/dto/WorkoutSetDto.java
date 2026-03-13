package com.ferrumlog.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * DTO para registrar una serie individual de un ejercicio.
 * NÚCLEO del sistema: registrar peso + repeticiones + RPE.
 */
public record WorkoutSetDto(
    Long id,
    
    @NotNull(message = "El ejercicio es obligatorio")
    Long exerciseId,
    
    String exerciseName,  // Para mostrar en la UI
    
    @NotNull(message = "El peso es obligatorio")
    @Min(value = 0, message = "El peso no puede ser negativo")
    Double weight,
    
    @NotNull(message = "Las repeticiones son obligatorias")
    @Min(value = 1, message = "Debe haber al menos 1 repetición")
    Integer reps,
    
    @Min(value = 0, message = "El RPE no puede ser negativo")
    Double rpe  // Rate of Perceived Exertion (0-10)
) {}
