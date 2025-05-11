package com.dani.spring_boot_microservice_2_compra.repository;

import com.dani.spring_boot_microservice_2_compra.model.Compra;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository; // Importar
import java.util.List;

/**
 * Repositorio Spring Data JPA para la entidad Compra.
 * Proporciona métodos CRUD básicos y métodos de consulta derivados del nombre.
 */
@Repository // Clarifica el rol del bean, aunque JpaRepository sea detectado.
public interface CompraRepository extends JpaRepository<Compra, Long> {

    /**
     * Busca y devuelve todas las compras realizadas por un usuario específico.
     * Método derivado automáticamente por Spring Data JPA a partir del nombre del método.
     * Equivalente a "SELECT c FROM Compra c WHERE c.userId = :userId".
     *
     * @param userId El ID del usuario cuyas compras se buscan.
     * @return Una lista de compras pertenecientes al usuario; vacía si no se encuentran.
     */
    List<Compra> findAllByUserId(Long userId);

}