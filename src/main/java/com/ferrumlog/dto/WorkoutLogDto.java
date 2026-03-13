package com.ferrumlog.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO para registrar una sesión completa de entrenamiento.
 * Incluye fecha y todas las series realizadas.
 */
public record WorkoutLogDto(
    Long id,
    
    @NotNull(message = "La fecha del entrenamiento es obligatoria")
    LocalDateTime workoutDate,
    
    @Valid
    List<WorkoutSetDto> sets  // Lista de series realizadas
) {}
