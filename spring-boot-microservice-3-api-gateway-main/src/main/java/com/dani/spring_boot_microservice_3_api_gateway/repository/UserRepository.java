package com.dani.spring_boot_microservice_3_api_gateway.repository;

import com.dani.spring_boot_microservice_3_api_gateway.model.Role;
import com.dani.spring_boot_microservice_3_api_gateway.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;

/**
 * Repositorio Spring Data JPA para la entidad {@link User}.
 * <p>
 * Esta interfaz extiende {@link JpaRepository}, lo que proporciona automáticamente
 * métodos CRUD estándar (como {@code save()}, {@code findById()}, {@code findAll()}, {@code deleteById()}, etc.)
 * para la entidad {@link User} sin necesidad de implementación explícita.
 * <p>
 * Adicionalmente, define métodos de consulta personalizados para operaciones específicas
 * relacionadas con los usuarios, como la búsqueda por nombre de usuario o la actualización de roles.
 * La anotación {@link Repository} es opcional aquí ya que las interfaces que extienden
 * {@code JpaRepository} son detectadas automáticamente por Spring, pero se incluye para
 * mayor claridad semántica.
 *
 * @see User Entidad gestionada por este repositorio.
 * @see JpaRepository Interfaz base de Spring Data JPA.
 * @author Daniel Núñez Rojas (danidev fullstack software)
 * @version 1.1
 * @since 2025-05-04 (Fecha de creación o última modificación significativa)
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Busca un usuario en la base de datos por su nombre de usuario (username).
     * <p>
     * Spring Data JPA deriva automáticamente la consulta JPQL necesaria a partir del nombre
     * de este método ({@code findByUsername}). La consulta generada será similar a:
     * "SELECT u FROM User u WHERE u.username = :username".
     *
     * @param username El nombre de usuario (único) del usuario a buscar. No debe ser nulo.
     * @return Un {@link Optional} que contiene el objeto {@link User} si se encuentra un usuario
     * con el nombre de usuario proporcionado. Devuelve un {@link Optional#empty()}
     * si no se encuentra ningún usuario.
     */
    Optional<User> findByUsername(String username); //

    /**
     * Actualiza el rol de un usuario específico, identificado por su nombre de usuario.
     * <p>
     * Esta operación es una modificación directa en la base de datos y se define mediante
     * una consulta JPQL personalizada con la anotación {@link Query}.
     * La anotación {@link Modifying} es crucial aquí, ya que indica a Spring Data JPA
     * que esta consulta modifica datos (no es solo una consulta de lectura).
     * <p>
     * Es importante que el método de servicio que llama a {@code updateUserRole}
     * esté anotado con {@link org.springframework.transaction.annotation.Transactional}
     * para asegurar que la operación de actualización se ejecute dentro de una transacción.
     *
     * @param username El nombre de usuario del usuario cuyo rol se va a actualizar. No debe ser nulo.
     * @param role El nuevo {@link Role} que se asignará al usuario. No debe ser nulo.
     * @see Modifying
     * @see Query
     * @see Param
     */
    @Modifying
    @Query("update User u set u.role = :role where u.username = :username")
    void updateUserRole(@Param("username") String username, @Param("role") Role role); //

    /**
     * Cuenta el número de usuarios que tienen un rol específico.
     * <p>
     * Spring Data JPA deriva automáticamente la consulta necesaria (similar a
     * "SELECT count(u) FROM User u WHERE u.role = :role") a partir del nombre del método.
     * Este método es útil, por ejemplo, para implementar lógicas de negocio que limitan
     * la cantidad de usuarios con un determinado rol (como administradores).
     *
     * @param role El {@link Role} por el cual se contarán los usuarios. No debe ser nulo.
     * @return El número total de usuarios que tienen el rol especificado.
     */
    long countByRole(Role role); //
}