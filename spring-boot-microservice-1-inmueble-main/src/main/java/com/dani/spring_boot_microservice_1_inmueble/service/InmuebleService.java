package com.dani.spring_boot_microservice_1_inmueble.service;

import com.dani.spring_boot_microservice_1_inmueble.model.EstadoInmueble;
import com.dani.spring_boot_microservice_1_inmueble.model.Inmueble;

import java.util.List;
import java.util.Optional;

/**
 * Interfaz que define el contrato para las operaciones de negocio
 * relacionadas con los inmuebles del sistema.
 * <p>
 * Abstrae la lógica de negocio, incluyendo la lógica de permisos para la creación,
 * actualización y eliminación de inmuebles.
 *
 * @author Daniel Núñez Rojas (danidev fullstack software)
 * @version 1.0
 * @since 2025-05-13
 */
public interface InmuebleService {

    /**
     * Guarda un nuevo inmueble o actualiza uno existente.
     * <p>
     * Si el ID del inmueble es nulo, se considera una creación. Se le asignará
     * la fecha de creación actual, el estado por defecto, y el ID del usuario que realiza la petición.
     * <p>
     * Si el ID del inmueble no es nulo, se considera una actualización. Se verificará
     * que el usuario que realiza la petición sea el propietario del inmueble o un administrador.
     *
     * @param inmueble El objeto {@link Inmueble} a guardar.
     * @param requestorUserId El ID del usuario que realiza la petición.
     * @param requestorRoles La lista de roles del usuario que realiza la petición.
     * @return El objeto {@link Inmueble} guardado o actualizado.
     * @throws SecurityException si el usuario no tiene permisos para actualizar el inmueble.
     * @throws IllegalArgumentException si se intenta actualizar un inmueble que no existe.
     */
    Inmueble saveInmueble(Inmueble inmueble, Long requestorUserId, List<String> requestorRoles);

    /**
     * Elimina un inmueble por su ID.
     * <p>
     * Antes de eliminar, se verifica que el usuario que realiza la petición sea el propietario
     * del inmueble o un administrador.
     *
     * @param inmuebleId El ID del inmueble a eliminar.
     * @param requestorUserId El ID del usuario que realiza la petición.
     * @param requestorRoles La lista de roles del usuario que realiza la petición.
     * @throws SecurityException si el usuario no tiene permisos para eliminar el inmueble.
     */
    void deleteInmueble(Long inmuebleId, Long requestorUserId, List<String> requestorRoles);

    /**
     * Devuelve una lista de todos los inmuebles registrados en el sistema.
     *
     * @return Una {@link List} de todos los objetos {@link Inmueble}.
     */
    List<Inmueble> findAllInmuebles();

    /**
     * Actualiza el estado de un inmueble específico.
     * Este método es típicamente llamado por otros servicios (ej. `compra-service`)
     * para cambiar el estado de un inmueble a "VENDIDO" después de una compra.
     *
     * @param inmuebleId El ID del inmueble a actualizar.
     * @param estado El nuevo {@link EstadoInmueble} a asignar.
     */
    void updateInmuebleEstado(Long inmuebleId, EstadoInmueble estado);

    /**
     * Busca un inmueble por su ID.
     *
     * @param inmuebleId El ID del inmueble a buscar.
     * @return Un {@link Optional} que contiene el {@link Inmueble} si se encuentra,
     * o un {@link Optional} vacío si no.
     */
    Optional<Inmueble> findById(Long inmuebleId);

    /**
     * Devuelve todos los inmuebles pertenecientes a un usuario específico.
     *
     * @param userId El ID del usuario cuyos inmuebles se desean recuperar.
     * @return Una {@link List} de objetos {@link Inmueble} del usuario.
     */
    List<Inmueble> findAllByUserId(Long userId);
}