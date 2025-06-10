package com.dani.spring_boot_microservice_1_inmueble.repository;

import com.dani.spring_boot_microservice_1_inmueble.model.EstadoInmueble;
import com.dani.spring_boot_microservice_1_inmueble.model.Inmueble;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio Spring Data JPA para la entidad {@link Inmueble}.
 * <p>
 * Proporciona una abstracción sobre la capa de acceso a datos para la entidad Inmueble.
 * Al extender {@link JpaRepository}, hereda automáticamente métodos CRUD estándar
 * (como {@code save()}, {@code findById()}, {@code findAll()}, etc.).
 * <p>
 * Además, define métodos de consulta personalizados para operaciones específicas
 * como buscar todos los inmuebles de un usuario o actualizar el estado de un inmueble.
 *
 * @see Inmueble La entidad gestionada por este repositorio.
 * @see JpaRepository La interfaz base de Spring Data JPA.
 * @author Daniel Núñez Rojas (danidev fullstack software)
 * @version 1.0
 * @since 2025-05-13 (Fecha de creación o última modificación significativa)
 */
@Repository
public interface InmuebleRepository extends JpaRepository<Inmueble, Long> {

    /**
     * Busca y devuelve todos los inmuebles asociados a un ID de usuario específico.
     * <p>
     * Spring Data JPA genera automáticamente la consulta JPQL necesaria a partir del
     * nombre de este método ({@code findByUserId}). La consulta será similar a:
     * "SELECT i FROM Inmueble i WHERE i.userId = :userId".
     *
     * @param userId El ID del usuario cuyos inmuebles se desean encontrar.
     * @return Una {@link List} de objetos {@link Inmueble} pertenecientes al usuario.
     * La lista estará vacía si el usuario no tiene ningún inmueble.
     */
    List<Inmueble> findAllByUserId(Long userId);

    /**
     * Actualiza el estado de un inmueble específico, identificado por su ID.
     * <p>
     * Esta es una operación de modificación directa en la base de datos, definida
     * mediante una consulta JPQL con la anotación {@link Query}.
     * La anotación {@link Modifying} es crucial para indicar a Spring Data JPA que
     * esta consulta modifica datos.
     * <p>
     * El método de servicio que llama a esta operación debe ser transaccional
     * (anotado con {@link org.springframework.transaction.annotation.Transactional}).
     *
     * @param inmuebleId El ID del inmueble cuyo estado se va a actualizar.
     * @param estado El nuevo {@link EstadoInmueble} que se asignará al inmueble.
     * @see Modifying
     * @see Query
     * @see Param
     */
    @Modifying
    @Query("update Inmueble i set i.estado = :estado where i.id = :inmuebleId")
    void updateInmuebleEstado(@Param("inmuebleId") Long inmuebleId, @Param("estado") EstadoInmueble estado);
}