package com.dani.spring_boot_microservice_1_inmueble.repository;

import com.dani.spring_boot_microservice_1_inmueble.model.Inmueble;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository; // Opcional, pero buena práctica

/**
 * Repositorio Spring Data JPA para la entidad Inmueble.
 * Proporciona métodos CRUD básicos (save, findById, findAll, deleteById, etc.)
 * y la capacidad de definir métodos de consulta personalizados.
 */
@Repository // Anotación opcional, JpaRepository ya es detectado, pero clarifica el rol.
public interface InmuebleRepository extends JpaRepository<Inmueble, Long> {
    // Aquí se podrían añadir métodos de consulta personalizados si fueran necesarios.
    // Ejemplo: List<Inmueble> findByPriceLessThan(Double price);

    /**
     * Busca todos los inmuebles asociados a un usuario específico.
     *
     * @param userId El ID del usuario.
     * @return Una lista de inmuebles asociados al usuario.
     */
    // Este método se generará automáticamente por Spring Data JPA.
    // No es necesario implementar nada aquí, Spring Data JPA lo hace por nosotros.
    // Se puede personalizar la consulta si es necesario.

    List<Inmueble> findAllByUserId(Long userId);
}