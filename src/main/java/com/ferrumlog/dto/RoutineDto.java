package com.ferrumlog.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;

/**
 * DTO para crear/actualizar rutinas.
 */
public record RoutineDto(
    Long id,
    
    @NotBlank(message = "El nombre de la rutina es obligatorio")
    @Size(max = 100, message = "El nombre no puede superar 100 caracteres")
    String name,
    
    @Size(max = 1000, message = "La descripción no puede superar 1000 caracteres")
    String description,

    List<Long> exerciseIds,

    List<Integer> exerciseSets
) {}
