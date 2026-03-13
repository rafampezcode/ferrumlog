package com.ferrumlog.repository;

import com.ferrumlog.domain.entity.Routine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio para la entidad Routine.
 * Permite buscar rutinas por usuario.
 */
@Repository
public interface RoutineRepository extends JpaRepository<Routine, Long> {

    long countByUserId(Long userId);

    List<Routine> findTop5ByUserIdOrderByCreatedAtDesc(Long userId);

    boolean existsByIdAndUserId(Long id, Long userId);

    /**
     * Obtiene todas las rutinas de un usuario específico.
     * @param userId el ID del usuario
     * @return lista de rutinas del usuario
     */
    List<Routine> findByUserId(Long userId);

    /**
     * Busca rutinas por nombre (búsqueda parcial, case-insensitive).
     * @param name el nombre de la rutina
     * @return lista de rutinas que coinciden con el nombre
     */
    List<Routine> findByNameContainingIgnoreCase(String name);
}
