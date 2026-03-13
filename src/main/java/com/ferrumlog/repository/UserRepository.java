package com.ferrumlog.repository;

import com.ferrumlog.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repositorio para la entidad User.
 * Spring Data JPA genera automáticamente las implementaciones de los métodos CRUD.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Busca un usuario por su nombre de usuario.
     * @param username el nombre de usuario
     * @return un Optional con el usuario si existe
     */
    Optional<User> findByUsername(String username);

    /**
     * Busca un usuario por su email.
     * @param email el correo electrónico
     * @return un Optional con el usuario si existe
     */
    Optional<User> findByEmail(String email);

    /**
     * Verifica si existe un usuario con el username dado.
     * @param username el nombre de usuario
     * @return true si existe, false en caso contrario
     */
    boolean existsByUsername(String username);

    /**
     * Verifica si existe un usuario con el email dado.
     * @param email el correo electrónico
     * @return true si existe, false en caso contrario
     */
    boolean existsByEmail(String email);
}
