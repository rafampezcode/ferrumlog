package com.ferrumlog.dto;

import java.time.LocalDateTime;

/**
 * Resumen de una sesion de entrenamiento.
 */
public record WorkoutSummaryDto(
        Long id,
        LocalDateTime workoutDate,
        long totalSets,
        long totalExercises
) {
}