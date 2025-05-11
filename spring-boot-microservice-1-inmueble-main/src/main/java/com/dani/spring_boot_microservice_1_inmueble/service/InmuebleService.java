package com.dani.spring_boot_microservice_1_inmueble.service;

import com.dani.spring_boot_microservice_1_inmueble.model.Inmueble;
import java.util.List;

/**
 * Define el contrato para las operaciones de negocio relacionadas con los inmuebles.
 * Abstrae la lógica de negocio de la capa de controlador y persistencia.
 */
public interface InmuebleService {

    /**
     * Guarda un nuevo inmueble o actualiza uno existente.
     * Asigna la fecha de creación antes de persistir.
     *
     * @param inmueble El objeto Inmueble a guardar.
     * @return El inmueble guardado (potencialmente con ID asignado o campos actualizados).
     */
    Inmueble saveInmueble(Inmueble inmueble);

    /**
     * Elimina un inmueble basado en su identificador único.
     *
     * @param inmuebleId El ID del inmueble a eliminar.
     */
    void deleteInmueble(Long inmuebleId);

    /**
     * Recupera una lista de todos los inmuebles registrados en el sistema.
     *
     * @return Una lista de objetos Inmueble; puede estar vacía si no hay inmuebles.
     */
    List<Inmueble> findAllInmuebles();
}