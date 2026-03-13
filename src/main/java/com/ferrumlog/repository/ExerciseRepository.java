package com.ferrumlog.repository;

import com.ferrumlog.domain.entity.Exercise;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para la entidad Exercise.
 * Gestiona el banco de ejercicios disponibles (sentadilla, press banca, etc.).
 */
@Repository
public interface ExerciseRepository extends JpaRepository<Exercise, Long> {

    /**
     * Busca un ejercicio por su nombre exacto.
     * @param name el nombre del ejercicio
     * @return un Optional con el ejercicio si existe
     */
    Optional<Exercise> findByName(String name);

    /**
     * Obtiene todos los ejercicios de un grupo muscular específico.
     * @param muscleGroup el grupo muscular (ej: "Pecho", "Piernas")
     * @return lista de ejercicios del grupo muscular
     */
    List<Exercise> findByMuscleGroup(String muscleGroup);

    /**
     * Busca ejercicios por nombre (búsqueda parcial, case-insensitive).
     * @param name el nombre del ejercicio
     * @return lista de ejercicios que coinciden
     */
    List<Exercise> findByNameContainingIgnoreCase(String name);

    /**
     * Busca ejercicios por nombre o grupo muscular (búsqueda parcial, case-insensitive).
     * Útil para búsquedas globales.
     * @param name término de búsqueda para nombre
     * @param muscleGroup término de búsqueda para grupo muscular
     * @return lista de ejercicios que coinciden en nombre o grupo muscular
     */
    List<Exercise> findByNameContainingIgnoreCaseOrMuscleGroupContainingIgnoreCase(
            String name, String muscleGroup);

        @Query("SELECT DISTINCT e FROM Exercise e " +
            "JOIN e.routineExercises re " +
            "WHERE re.routine.user.id = :userId")
        List<Exercise> findDistinctByRoutineUserId(@Param("userId") Long userId);
}
