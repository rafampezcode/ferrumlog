package com.ferrumlog.service;

import com.ferrumlog.dto.WorkoutSetDto;

import java.util.List;
import java.util.Optional;

/**
 * Interfaz de servicio para la gestión de entrenamientos y registros.
 */
public interface WorkoutService {

    /**
     * MÉTODO CRÍTICO: Recupera el último WorkoutSet registrado para un ejercicio específico
     * realizado por el usuario autenticado.
     * 
     * Útil para mostrar al usuario su último peso/reps/RPE al comenzar un nuevo entrenamiento,
     * aplicando el principio de sobrecarga progresiva.
     * 
     * @param userId ID del usuario autenticado
     * @param exerciseId ID del ejercicio del que queremos el último registro
     * @return Optional con el último WorkoutSetDto si existe, vacío si nunca lo ha hecho
     */
    Optional<WorkoutSetDto> getLastWorkoutSet(Long userId, Long exerciseId);

    /**
     * Registra una nueva serie de entrenamiento.
     * 
     * @param workoutSetDto datos de la serie
     * @param userId ID del usuario que registra
     * @return la serie registrada
     */
    WorkoutSetDto recordWorkoutSet(WorkoutSetDto workoutSetDto, Long userId);

    /**
     * Registra una sesion completa como una unidad (un WorkoutLog con multiples series).
     *
     * @param workoutSets series completadas en la sesion
     * @param userId ID del usuario autenticado
     * @return ID del WorkoutLog creado
     */
    Long recordWorkoutSession(List<WorkoutSetDto> workoutSets, Long userId);
}
