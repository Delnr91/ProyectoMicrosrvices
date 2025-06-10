package com.dani.spring_boot_microservice_1_inmueble.service;

import com.dani.spring_boot_microservice_1_inmueble.model.EstadoInmueble;
import com.dani.spring_boot_microservice_1_inmueble.model.Inmueble;
import com.dani.spring_boot_microservice_1_inmueble.repository.InmuebleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Implementación concreta de la interfaz {@link InmuebleService}.
 * <p>
 * Contiene la lógica de negocio para la gestión de inmuebles, incluyendo
 * la lógica de permisos para la creación, actualización y eliminación,
 * la cual se basa en el ID y los roles del usuario que realiza la petición.
 *
 * @author Daniel Núñez Rojas (danidev fullstack software)
 * @version 1.1
 * @since 2025-06-09 (Actualizado con JavaDoc completo)
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class InmuebleServiceImpl implements InmuebleService {

    private final InmuebleRepository inmuebleRepository;

    /**
     * {@inheritDoc}
     * <p>
     * Esta implementación distingue entre creación y actualización basándose
     * en si el ID del inmueble es nulo.
     * Para actualizaciones, verifica que el rol sea "ROLE_ADMIN" o que el ID del
     * solicitante coincida con el {@code userId} del inmueble existente.
     * Para creaciones, asigna valores por defecto como la fecha de creación, el ID del
     * usuario solicitante y el estado "DISPONIBLE".
     */
    @Override
    @Transactional
    public Inmueble saveInmueble(Inmueble inmueble, Long requestorUserId, List<String> requestorRoles) {
        log.info("Procesando guardado/actualización de inmueble por usuario ID: {}. Roles: {}", requestorUserId, requestorRoles);

        if (inmueble.getId() != null) { // Es una actualización
            return inmuebleRepository.findById(inmueble.getId())
                    .map(existingInmueble -> {
                        // Lógica de permisos para actualización
                        if (requestorRoles.contains("ROLE_ADMIN") || existingInmueble.getUserId().equals(requestorUserId)) {
                            log.info("Permiso concedido. Actualizando inmueble ID: {}", existingInmueble.getId());
                            existingInmueble.setName(inmueble.getName());
                            existingInmueble.setAddress(inmueble.getAddress());
                            existingInmueble.setPicture(inmueble.getPicture());
                            existingInmueble.setPrice(inmueble.getPrice());
                            return inmuebleRepository.save(existingInmueble);
                        } else {
                            log.warn("Acceso denegado. Usuario {} no tiene permiso para actualizar inmueble ID: {}", requestorUserId, existingInmueble.getId());
                            throw new SecurityException("No tiene permiso para actualizar este inmueble.");
                        }
                    })
                    .orElseThrow(() -> new IllegalArgumentException("Inmueble con ID " + inmueble.getId() + " no encontrado para actualizar."));
        } else { // Es una creación
            log.info("Creando nuevo inmueble para usuario ID: {}", requestorUserId);
            inmueble.setCreationDate(LocalDateTime.now());
            inmueble.setUserId(requestorUserId);
            inmueble.setEstado(EstadoInmueble.DISPONIBLE);
            return inmuebleRepository.save(inmueble);
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * Esta implementación primero busca el inmueble por su ID. Si lo encuentra,
     * verifica que el rol del solicitante sea "ROLE_ADMIN" o que el ID del solicitante
     * coincida con el {@code userId} del inmueble antes de proceder con la eliminación.
     * Si no se encuentra el inmueble, la operación finaliza silenciosamente.
     */
    @Override
    @Transactional
    public void deleteInmueble(Long inmuebleId, Long requestorUserId, List<String> requestorRoles) {
        log.info("Procesando eliminación de inmueble ID: {} por usuario ID: {}. Roles: {}", inmuebleId, requestorUserId, requestorRoles);
        inmuebleRepository.findById(inmuebleId)
                .ifPresentOrElse(inmueble -> {
                    // Lógica de permisos para eliminación
                    if (requestorRoles.contains("ROLE_ADMIN") || inmueble.getUserId().equals(requestorUserId)) {
                        log.info("Permiso concedido. Eliminando inmueble ID: {}", inmuebleId);
                        inmuebleRepository.delete(inmueble);
                    } else {
                        log.warn("Acceso denegado. Usuario {} no tiene permiso para eliminar inmueble ID: {}", requestorUserId, inmuebleId);
                        throw new SecurityException("No tiene permiso para eliminar este inmueble.");
                    }
                }, () -> {
                    log.warn("Intento de eliminar un inmueble no existente con ID: {}", inmuebleId);
                });
    }

    /**
     * {@inheritDoc}
     * Esta implementación es de solo lectura.
     */
    @Override
    @Transactional(readOnly = true)
    public List<Inmueble> findAllInmuebles() {
        return inmuebleRepository.findAll();
    }

    /**
     * {@inheritDoc}
     * Esta implementación es una operación de escritura y debe ser transaccional.
     */
    @Override
    @Transactional
    public void updateInmuebleEstado(Long inmuebleId, EstadoInmueble estado) {
        log.info("Actualizando estado del inmueble ID: {} a {}", inmuebleId, estado);
        inmuebleRepository.updateInmuebleEstado(inmuebleId, estado);
    }

    /**
     * {@inheritDoc}
     * Esta implementación es de solo lectura.
     */
    @Override
    @Transactional(readOnly = true)
    public Optional<Inmueble> findById(Long inmuebleId) {
        return inmuebleRepository.findById(inmuebleId);
    }

    /**
     * {@inheritDoc}
     * Esta implementación es de solo lectura.
     */
    @Override
    @Transactional(readOnly = true)
    public List<Inmueble> findAllByUserId(Long userId) {
        return inmuebleRepository.findAllByUserId(userId);
    }
}