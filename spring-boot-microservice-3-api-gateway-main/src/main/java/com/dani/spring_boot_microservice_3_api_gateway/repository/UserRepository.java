package com.dani.spring_boot_microservice_3_api_gateway.repository;

import com.dani.spring_boot_microservice_3_api_gateway.model.Role;
import com.dani.spring_boot_microservice_3_api_gateway.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository; // Importar
import java.util.List;

import java.util.Optional;

/**
 * Repositorio Spring Data JPA para la entidad User.
 * Proporciona métodos CRUD y consultas personalizadas para usuarios.
 */
@Repository // Opcional, pero clarifica
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Busca un usuario por su nombre de usuario (username).
     * Método derivado por convención de nombres de Spring Data JPA.
     *
     * @param username El nombre de usuario a buscar.
     * @return Un Optional que contiene el User si se encuentra, o vacío si no.
     */
    Optional<User> findByUsername(String username);

    /**
     * Actualiza el rol de un usuario específico identificado por su username.
     * Utiliza una consulta JPQL personalizada con anotación @Query y @Modifying.
     * NOTA: Requiere @Transactional en el método del servicio que lo llama.
     *
     * @param username El nombre de usuario del usuario a modificar.
     * @param role El nuevo rol a asignar.
     */
    @Modifying // Indica que esta consulta modifica datos
    @Query("update User u set u.role = :role where u.username = :username")
    void updateUserRole(@Param("username") String username, @Param("role") Role role);

    // NUEVO MÉTODO para contar usuarios por rol
    long countByRole(Role role);
}