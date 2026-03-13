package com.ferrumlog.repository;

import com.ferrumlog.domain.entity.WorkoutSet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para la entidad WorkoutSet.
 * CLAVE: Permite consultar el historial de series de cada ejercicio.
 * Esto resuelve el problema principal: recordar qué peso levantaste la última vez.
 */
@Repository
public interface WorkoutSetRepository extends JpaRepository<WorkoutSet, Long> {

       long countByWorkoutLog_UserId(Long userId);

       @Query("SELECT ws FROM WorkoutSet ws " +
                 "JOIN FETCH ws.workoutLog wl " +
                 "JOIN FETCH ws.exercise e " +
                 "WHERE wl.user.id = :userId " +
                 "ORDER BY wl.workoutDate DESC, ws.id DESC")
       List<WorkoutSet> findRecentByUserId(@Param("userId") Long userId, Pageable pageable);

           @Query("SELECT ws FROM WorkoutSet ws " +
                  "JOIN FETCH ws.workoutLog wl " +
                  "JOIN FETCH ws.exercise e " +
                  "WHERE wl.user.id = :userId")
           List<WorkoutSet> findAllByUserIdWithExercise(@Param("userId") Long userId);

    /**
     * Obtiene todas las series de un entrenamiento específico.
     * @param workoutLogId el ID del registro de entrenamiento
     * @return lista de series del entrenamiento
     */
    List<WorkoutSet> findByWorkoutLogId(Long workoutLogId);

    @Query("SELECT ws FROM WorkoutSet ws " +
          "JOIN FETCH ws.exercise e " +
          "JOIN ws.workoutLog wl " +
          "WHERE wl.id = :workoutLogId " +
          "AND wl.user.id = :userId " +
          "ORDER BY ws.id ASC")
    List<WorkoutSet> findDetailedByWorkoutLogIdAndUserId(
           @Param("workoutLogId") Long workoutLogId,
           @Param("userId") Long userId);

    /**
     * Obtiene el historial de series de un ejercicio específico para un usuario.
     * IMPORTANTE: Te dice qué peso levantaste en ese ejercicio anteriormente.
     * 
     * @param userId el ID del usuario
     * @param exerciseId el ID del ejercicio
     * @return lista de series ordenadas por fecha (más reciente primero)
     */
    @Query("SELECT ws FROM WorkoutSet ws " +
           "JOIN ws.workoutLog wl " +
           "WHERE wl.user.id = :userId " +
           "AND ws.exercise.id = :exerciseId " +
           "ORDER BY wl.workoutDate DESC")
    List<WorkoutSet> findHistoryByUserAndExercise(@Param("userId") Long userId, 
                                                    @Param("exerciseId") Long exerciseId);

    /**
     * Obtiene la última serie registrada de un ejercicio para un usuario.
     * FUNCIONALIDAD ESTRELLA: "¿Qué peso levanté la semana pasada en sentadilla?"
     * 
     * @param userId el ID del usuario
     * @param exerciseId el ID del ejercicio
     * @return la última WorkoutSet registrada
     */
    @Query("SELECT ws FROM WorkoutSet ws " +
           "JOIN ws.workoutLog wl " +
           "WHERE wl.user.id = :userId " +
           "AND ws.exercise.id = :exerciseId " +
           "ORDER BY wl.workoutDate DESC " +
           "LIMIT 1")
    WorkoutSet findLastSetByUserAndExercise(@Param("userId") Long userId, 
                                             @Param("exerciseId") Long exerciseId);

    /**
     * MÉTODO CRÍTICO usando convención Spring Data JPA (sin @Query).
     * Busca el último WorkoutSet ordenando por fecha de WorkoutLog descendente.
     * 
     * @param userId el ID del usuario
     * @param exerciseId el ID del ejercicio
     * @return Optional con el último WorkoutSet si existe
     */
    Optional<WorkoutSet> findFirstByWorkoutLog_UserIdAndExerciseIdOrderByWorkoutLog_WorkoutDateDescIdDesc(
            Long userId, Long exerciseId);
}
