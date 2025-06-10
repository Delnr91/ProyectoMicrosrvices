package com.dani.spring_boot_microservice_2_compra.repository;

import com.dani.spring_boot_microservice_2_compra.model.Compra;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio Spring Data JPA para la entidad {@link Compra}.
 * <p>
 * Proporciona una abstracción sobre la capa de acceso a datos para las compras.
 * Al extender {@link JpaRepository}, hereda automáticamente métodos CRUD estándar.
 * <p>
 * Define un método de consulta personalizado para buscar todas las compras
 * realizadas por un usuario específico.
 *
 * @see Compra La entidad gestionada por este repositorio.
 * @see JpaRepository La interfaz base de Spring Data JPA.
 * @author Daniel Núñez Rojas (danidev fullstack software)
 * @version 1.0
 * @since 2025-05-13
 */
@Repository
public interface CompraRepository extends JpaRepository<Compra, Long> {

    /**
     * Busca y devuelve todas las compras asociadas a un ID de usuario específico.
     * <p>
     * Spring Data JPA genera automáticamente la consulta JPQL necesaria a partir del
     * nombre de este método ({@code findByUserId}). La consulta será similar a:
     * "SELECT c FROM Compra c WHERE c.userId = :userId".
     *
     * @param userId El ID del usuario cuyas compras se desean encontrar.
     * @return Una {@link List} de objetos {@link Compra} pertenecientes al usuario.
     * La lista estará vacía si el usuario no ha realizado ninguna compra.
     */
    List<Compra> findAllByUserId(Long userId);
}