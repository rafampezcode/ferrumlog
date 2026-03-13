package com.ferrumlog.service;

import com.ferrumlog.dto.RoutineDto;

import java.util.List;

/**
 * Interfaz de servicio para la gestión de rutinas de entrenamiento.
 */
public interface RoutineService {

    /**
     * Crea una nueva rutina para el usuario autenticado.
     * 
     * @param routineDto datos de la rutina
     * @param userId ID del usuario propietario
     * @return la rutina creada
     */
    RoutineDto createRoutine(RoutineDto routineDto, Long userId);

    /**
     * Actualiza una rutina existente.
     * Solo el propietario puede actualizar su rutina.
     * 
     * @param id identificador de la rutina
     * @param routineDto nuevos datos
     * @param userId ID del usuario que intenta actualizar
     * @return la rutina actualizada
     * @throws SecurityException si el usuario no es el propietario
     * @throws RuntimeException si la rutina no existe
     */
    RoutineDto updateRoutine(Long id, RoutineDto routineDto, Long userId);

    /**
     * Obtiene una rutina por su ID.
     * Solo el propietario puede ver su rutina.
     * 
     * @param id identificador de la rutina
     * @param userId ID del usuario que intenta acceder
     * @return la rutina encontrada
     * @throws SecurityException si el usuario no es el propietario
     * @throws RuntimeException si no existe
     */
    RoutineDto getRoutineById(Long id, Long userId);

    /**
     * Obtiene todas las rutinas del usuario autenticado.
     * 
     * @param userId ID del usuario
     * @return lista de rutinas del usuario
     */
    List<RoutineDto> getUserRoutines(Long userId);

    /**
     * Elimina una rutina.
     * Solo el propietario puede eliminar su rutina.
     * 
     * @param id identificador de la rutina
     * @param userId ID del usuario que intenta eliminar
     * @throws SecurityException si el usuario no es el propietario
     * @throws RuntimeException si no existe
     */
    void deleteRoutine(Long id, Long userId);
}
