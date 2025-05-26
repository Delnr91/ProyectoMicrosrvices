package com.dani.spring_boot_microservice_1_inmueble.service;

import com.dani.spring_boot_microservice_1_inmueble.model.EstadoInmueble;
import com.dani.spring_boot_microservice_1_inmueble.model.Inmueble;
import com.dani.spring_boot_microservice_1_inmueble.repository.InmuebleRepository;
import lombok.RequiredArgsConstructor; // Importar para inyección por constructor
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // Importar para gestión transaccional

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Implementación concreta de la interfaz InmuebleService.
 * Contiene la lógica de negocio para la gestión de inmuebles.
 * Interactúa con InmuebleRepository para la persistencia de datos.
 */
@Service // Marca esta clase como un bean de servicio de Spring.
@RequiredArgsConstructor // Lombok: Genera constructor para inyección de dependencias final.
@Slf4j
public class InmuebleServiceImpl implements InmuebleService {

    // Inyección por constructor de la dependencia del repositorio. Es final.
    private final InmuebleRepository inmuebleRepository;

    /**
     * {@inheritDoc}
     * Antes de guardar, establece la fecha de creación del inmueble a la fecha y
     * hora actuales.
     */
    @Override
    @Transactional // Asegura que la operación se ejecute dentro de una transacción.
    public Inmueble saveInmueble(Inmueble inmueble, Long requestorUserId, String requestorRoles) {
        log.debug("Intentando guardar/actualizar inmueble: {} por usuario ID: {} con roles: {}", inmueble, requestorUserId, requestorRoles);

        // Si es una ACTUALIZACIÓN (el inmueble ya tiene un ID)
        if (inmueble.getId() != null) {
            Inmueble existingInmueble = inmuebleRepository.findById(inmueble.getId())
                    .orElseThrow(() -> {
                        log.warn("Actualización fallida: Inmueble con ID {} no encontrado.", inmueble.getId());
                        return new RuntimeException("Inmueble no encontrado con ID: " + inmueble.getId()); // O una excepción más específica
                    });

            // Verificar permisos para actualizar
            boolean isAdmin = requestorRoles != null && requestorRoles.contains("ROLE_ADMIN");
            if (!isAdmin && (existingInmueble.getUserId() == null || !existingInmueble.getUserId().equals(requestorUserId))) {
                log.warn("Usuario ID {} intentó actualizar inmueble ID {} sin ser propietario ni admin.", requestorUserId, inmueble.getId());
                throw new SecurityException("No tienes permiso para actualizar este inmueble."); // O UnauthorizedOperationException
            }
            // Mantener la fecha de creación original y el creador original al actualizar,
            // a menos que la lógica de negocio permita cambiar el propietario (lo cual no es el caso aquí).
            inmueble.setCreationDate(existingInmueble.getCreationDate());
            inmueble.setUserId(existingInmueble.getUserId());
            if (inmueble.getEstado() == null) {
                inmueble.setEstado(existingInmueble.getEstado());
            }
            // El propietario no cambia en una edición normal por el usuario
        } else { // Es una CREACIÓN nueva
            if (inmueble.getCreationDate() == null) {
                inmueble.setCreationDate(LocalDateTime.now());
            }
            if (requestorUserId != null) {
                inmueble.setUserId(requestorUserId); // Asignar el creador
            } else {
                // ¿Qué hacer si no hay requestorUserId? Depende de tu lógica.
                // Podría ser un error, o podría ser un inmueble "del sistema".
                log.warn("Creando inmueble sin un requestorUserId especificado.");
                // throw new IllegalArgumentException("El ID del usuario creador es requerido para nuevos inmuebles.");
            }
            inmueble.setEstado(EstadoInmueble.DISPONIBLE);
        }
        return inmuebleRepository.save(inmueble);
    }
    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional // Asegura que la operación se ejecute dentro de una transacción.
    public void deleteInmueble(Long inmuebleId, Long requestorUserId, String requestorRoles) {
        log.debug("Intentando eliminar inmueble ID: {} por usuario ID: {} con roles: {}", inmuebleId, requestorUserId, requestorRoles);

        Inmueble inmuebleToDelete = inmuebleRepository.findById(inmuebleId)
                .orElseThrow(() -> {
                    log.warn("Eliminación fallida: Inmueble con ID {} no encontrado.", inmuebleId);
                    // No lanzar error si no se encuentra es una opción para DELETE idempotente,
                    // pero para feedback al usuario, es mejor indicar que no se encontró.
                    return new RuntimeException("Inmueble no encontrado con ID: " + inmuebleId);
                });

        // Verificar permisos para eliminar
        boolean isAdmin = requestorRoles != null && requestorRoles.contains("ROLE_ADMIN");
        if (!isAdmin && (inmuebleToDelete.getUserId() == null || !inmuebleToDelete.getUserId().equals(requestorUserId))) {
            log.warn("Usuario ID {} intentó eliminar inmueble ID {} sin ser propietario ni admin.", requestorUserId, inmuebleId);
            throw new SecurityException("No tienes permiso para eliminar este inmueble."); // O UnauthorizedOperationException
        }

        inmuebleRepository.deleteById(inmuebleId);
        log.info("Inmueble ID {} eliminado exitosamente.", inmuebleId);
    }
    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true) // Indica que esta transacción es de solo lectura (optimización).
     public List<Inmueble> findAllInmuebles() {
        log.debug("Obteniendo todos los inmuebles.");
        return inmuebleRepository.findAll();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true) // Indica que esta transacción es de solo lectura (optimización).       
     public Optional<Inmueble> findById(Long inmuebleId) {
        log.debug("Buscando inmueble por ID: {}", inmuebleId);
        return inmuebleRepository.findById(inmuebleId);
    }

    /**
     * {@inheritDoc}
     */
    @Override       
    @Transactional(readOnly = true) //
     public List<Inmueble> findAllByUserId(Long userId) {
        log.debug("Buscando todos los inmuebles para el usuario ID: {}", userId);
        return inmuebleRepository.findAllByUserId(userId);
    }

    // NUEVO MÉTODO PARA ACTUALIZAR ESTADO

    @Override
    @Transactional
    public Inmueble updateEstado(Long inmuebleId, EstadoInmueble nuevoEstado) {
        log.debug("Intentando actualizar estado del inmueble ID: {} a {}", inmuebleId, nuevoEstado);
        Inmueble inmueble = inmuebleRepository.findById(inmuebleId)
                .orElseThrow(() -> {
                    log.warn("Actualización de estado fallida: Inmueble con ID {} no encontrado.", inmuebleId);
                    return new RuntimeException("Inmueble no encontrado con ID: " + inmuebleId); // Considerar usar una excepción más específica
                });
        inmueble.setEstado(nuevoEstado);
        Inmueble updatedInmueble = inmuebleRepository.save(inmueble);
        log.info("Estado del inmueble ID: {} actualizado a {}", inmuebleId, nuevoEstado);
        return updatedInmueble;
    }
}