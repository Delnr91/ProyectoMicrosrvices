package com.dani.spring_boot_microservice_2_compra.service;

import com.dani.spring_boot_microservice_2_compra.model.Compra;

import java.util.List;

/**
 * Interfaz que define el contrato para las operaciones de negocio
 * relacionadas con las compras en el sistema.
 *
 * @author Daniel Núñez Rojas (danidev fullstack software)
 * @version 1.0
 * @since 2025-05-13
 */
public interface CompraService {

    /**
     * Guarda una nueva transacción de compra en la base de datos.
     * <p>
     * Además de guardar el registro de la compra, esta operación es responsable de
     * coordinar con otros servicios si es necesario (por ejemplo, para actualizar
     * el estado del inmueble comprado en el {@code inmueble-service}).
     *
     * @param compra El objeto {@link Compra} a guardar. Se espera que el ID sea nulo.
     * @return El objeto {@link Compra} guardado, ahora con el ID y la fecha de compra asignados.
     */
    Compra saveCompra(Compra compra);

    /**
     * Devuelve una lista de todas las compras realizadas por un usuario específico.
     *
     * @param userId El ID del usuario cuyas compras se desean recuperar.
     * @return Una {@link List} de objetos {@link Compra} pertenecientes al usuario,
     * o una lista vacía si no ha realizado compras.
     */
    List<Compra> findAllByUserId(Long userId);
}