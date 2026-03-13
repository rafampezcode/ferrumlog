package com.ferrumlog.repository;

import com.ferrumlog.domain.entity.RoutineExercise;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio para la entidad RoutineExercise.
 * Gestiona la relación entre rutinas y ejercicios.
 */
@Repository
public interface RoutineExerciseRepository extends JpaRepository<RoutineExercise, Long> {

    /**
     * Obtiene todos los ejercicios de una rutina específica, ordenados.
     * @param routineId el ID de la rutina
     * @return lista de ejercicios de la rutina en orden
     */
    List<RoutineExercise> findByRoutineIdOrderByOrderIndexAsc(Long routineId);

        @Query("SELECT re FROM RoutineExercise re " +
            "JOIN FETCH re.exercise e " +
            "WHERE re.routine.id = :routineId " +
            "ORDER BY re.orderIndex ASC")
        List<RoutineExercise> findDetailedByRoutineId(@Param("routineId") Long routineId);

    /**
     * Elimina todos los ejercicios de una rutina.
     * @param routineId el ID de la rutina
     */
    void deleteByRoutineId(Long routineId);
}
