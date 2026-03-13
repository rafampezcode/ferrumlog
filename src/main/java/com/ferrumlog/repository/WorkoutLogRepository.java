package com.ferrumlog.repository;

import com.ferrumlog.dto.WorkoutSummaryDto;
import com.ferrumlog.domain.entity.WorkoutLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repositorio para la entidad WorkoutLog.
 * Permite registrar y consultar las sesiones de entrenamiento.
 */
@Repository
public interface WorkoutLogRepository extends JpaRepository<WorkoutLog, Long> {

    long countByUserIdAndWorkoutDateBetween(Long userId, LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Obtiene todos los entrenamientos de un usuario.
     * @param userId el ID del usuario
     * @return lista de entrenamientos ordenados por fecha descendente
     */
    List<WorkoutLog> findByUserIdOrderByWorkoutDateDesc(Long userId);

    /**
     * Obtiene entrenamientos de un usuario entre dos fechas.
     * @param userId el ID del usuario
     * @param startDate fecha inicial
     * @param endDate fecha final
     * @return lista de entrenamientos en el rango de fechas
     */
    List<WorkoutLog> findByUserIdAndWorkoutDateBetween(Long userId, LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Encuentra el último entrenamiento registrado por el usuario.
     * @param userId el ID del usuario
     * @return el último WorkoutLog del usuario
     */
    WorkoutLog findFirstByUserIdOrderByWorkoutDateDesc(Long userId);

    boolean existsByIdAndUserId(Long id, Long userId);

    @Query("""
            SELECT new com.ferrumlog.dto.WorkoutSummaryDto(
                wl.id,
                wl.workoutDate,
                COUNT(ws.id),
                COUNT(DISTINCT ws.exercise.id)
            )
            FROM WorkoutLog wl
            LEFT JOIN wl.sets ws
            WHERE wl.user.id = :userId
            GROUP BY wl.id, wl.workoutDate
            ORDER BY wl.workoutDate DESC
            """)
    List<WorkoutSummaryDto> findWorkoutSummariesByUserIdOrderByWorkoutDateDesc(@Param("userId") Long userId);

    @Query("""
            SELECT new com.ferrumlog.dto.WorkoutSummaryDto(
                wl.id,
                wl.workoutDate,
                COUNT(ws.id),
                COUNT(DISTINCT ws.exercise.id)
            )
            FROM WorkoutLog wl
            LEFT JOIN wl.sets ws
            WHERE wl.user.id = :userId
            GROUP BY wl.id, wl.workoutDate
            ORDER BY wl.workoutDate ASC
            """)
    List<WorkoutSummaryDto> findWorkoutSummariesByUserIdOrderByWorkoutDateAsc(@Param("userId") Long userId);
}
