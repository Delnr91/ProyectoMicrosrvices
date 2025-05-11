package com.dani.spring_boot_microservice_2_compra.service;

import com.dani.spring_boot_microservice_2_compra.model.Compra;
import java.util.List;

/**
 * Define el contrato para las operaciones de negocio relacionadas con las compras.
 * Abstrae la lógica de negocio de la capa de controlador y persistencia para las compras.
 */
public interface CompraService {

    /**
     * Registra una nueva compra en el sistema.
     * Asigna la fecha de compra antes de persistir.
     *
     * @param compra El objeto Compra con los detalles a guardar.
     * @return La compra guardada (con ID asignado y fecha de compra).
     */
    Compra saveCompra(Compra compra);

    /**
     * Recupera todas las compras asociadas a un ID de usuario específico.
     *
     * @param userId El ID del usuario cuyas compras se desean buscar.
     * @return Una lista de objetos Compra realizadas por el usuario;
     * puede estar vacía si el usuario no tiene compras.
     */
    List<Compra> findAllComprasOfUser(Long userId);
}