package com.ferrumlog.service;

import com.ferrumlog.dto.ExerciseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Interfaz de servicio para la gestión de ejercicios del banco global.
 */
public interface ExerciseService {

    /**
     * Crea un nuevo ejercicio en el banco global.
     * 
     * @param exerciseDto datos del ejercicio
     * @return el ejercicio creado
     */
    ExerciseDto createExercise(ExerciseDto exerciseDto);

    /**
     * Actualiza un ejercicio existente.
     * 
     * @param id identificador del ejercicio
     * @param exerciseDto nuevos datos
     * @return el ejercicio actualizado
     * @throws RuntimeException si no existe
     */
    ExerciseDto updateExercise(Long id, ExerciseDto exerciseDto);

    /**
     * Obtiene un ejercicio por su ID.
     * 
     * @param id identificador del ejercicio
     * @return el ejercicio encontrado
     * @throws RuntimeException si no existe
     */
    ExerciseDto getExerciseById(Long id);

    /**
     * Obtiene todos los ejercicios del banco.
     * 
     * @return lista de ejercicios
     */
    List<ExerciseDto> getAllExercises();

    /**
     * Obtiene ejercicios paginados.
     * 
     * @param pageable parámetros de paginación
     * @return página de ejercicios
     */
    Page<ExerciseDto> getExercises(Pageable pageable);

    /**
     * Busca ejercicios por nombre o grupo muscular.
     * 
     * @param query término de búsqueda
     * @return lista de ejercicios que coinciden
     */
    List<ExerciseDto> searchExercises(String query);

    /**
     * Elimina un ejercicio del banco.
     * 
     * @param id identificador del ejercicio
     * @throws RuntimeException si está siendo usado en rutinas activas
     */
    void deleteExercise(Long id);
}
