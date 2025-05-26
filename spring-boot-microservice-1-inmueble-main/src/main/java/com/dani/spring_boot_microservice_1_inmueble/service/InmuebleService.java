package com.dani.spring_boot_microservice_1_inmueble.service;

import com.dani.spring_boot_microservice_1_inmueble.model.EstadoInmueble;
import com.dani.spring_boot_microservice_1_inmueble.model.Inmueble;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

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
    Inmueble saveInmueble(Inmueble inmueble, Long requestorUserId, String requestorRoles);

    /**
     * Elimina un inmueble basado en su identificador único.
     *
     * @param inmuebleId El ID del inmueble a eliminar.
     */
    void deleteInmueble(Long inmuebleId, Long requestorUserId, String requestorRoles);

    /**
     * Recupera una lista de todos los inmuebles registrados en el sistema.
     *
     * @return Una lista de objetos Inmueble; puede estar vacía si no hay inmuebles.
     */
    List<Inmueble> findAllInmuebles();

    /**
     * Busca un inmueble por su ID.
     *
     * @param inmuebleId El ID del inmueble a buscar.
     * @return Un Optional que contiene el inmueble si se encuentra, o vacío si no.
     */
    Optional<Inmueble> findById(Long inmuebleId);

    /**
     * Busca inmuebles por el ID del usuario que los creó o posee.
     *
     * @param userId El ID del usuario a buscar.
     * @return Una lista de objetos Inmueble asociados al usuario; puede estar vacía si no hay coincidencias.
     */
    List<Inmueble> findAllByUserId(Long userId);

    // NUEVO MÉTODO PARA ACTUALIZAR ESTADO
    Inmueble updateEstado(Long inmuebleId, EstadoInmueble nuevoEstado);
}