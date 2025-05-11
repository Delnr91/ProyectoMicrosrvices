package com.dani.spring_boot_microservice_1_inmueble.repository;

import com.dani.spring_boot_microservice_1_inmueble.model.Inmueble;
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
}