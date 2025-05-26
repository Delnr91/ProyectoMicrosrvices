package com.dani.spring_boot_microservice_1_inmueble.controller;

import com.dani.spring_boot_microservice_1_inmueble.model.EstadoInmueble;
import com.dani.spring_boot_microservice_1_inmueble.model.Inmueble;
import com.dani.spring_boot_microservice_1_inmueble.service.InmuebleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * Controlador REST para gestionar las operaciones CRUD de Inmuebles.
 * Expone los endpoints de la API relacionados con los inmuebles,
 * incluyendo la lógica de permisos basada en la información del solicitante (pasada por cabeceras).
 *
 * @author Daniel Núñez Rojas (danidev fullstack software)
 * @version 1.1
 * @since 2025-05-13 (Adaptado para permisos basados en cabeceras X-User-ID y X-User-Roles)
 */
@RestController
@RequestMapping("api/inmueble")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Gestión de Inmuebles", description = "API para realizar operaciones CRUD sobre inmuebles.")
public class InmuebleController {

    private final InmuebleService inmuebleService;

    /**
     * Endpoint para guardar un nuevo inmueble o actualizar uno existente.
     * La lógica de permisos (quién puede crear/actualizar qué) se maneja en la capa de servicio
     * utilizando la información de requestorUserId y requestorRoles.
     *
     * @param inmueble         El objeto Inmueble recibido en el cuerpo de la petición.
     * @param requestorUserId  (Opcional) El ID del usuario que realiza la petición, pasado por cabecera X-User-ID.
     * @param requestorRoles   (Opcional) Los roles del usuario que realiza la petición, pasados por cabecera X-User-Roles.
     * @return ResponseEntity con el inmueble guardado/actualizado y el estado HTTP correspondiente.
     */
    @PostMapping
    @Operation(summary = "Crear o Actualizar un Inmueble",
               description = "Registra un nuevo inmueble o actualiza uno existente. Requiere información del solicitante para permisos.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Inmueble creado exitosamente",
                         content = { @Content(mediaType = "application/json", schema = @Schema(implementation = Inmueble.class)) }),
            @ApiResponse(responseCode = "200", description = "Inmueble actualizado exitosamente",
                         content = { @Content(mediaType = "application/json", schema = @Schema(implementation = Inmueble.class)) }),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos o faltan cabeceras requeridas para la creación"),
            @ApiResponse(responseCode = "403", description = "No autorizado para realizar esta operación"),
            @ApiResponse(responseCode = "404", description = "Inmueble no encontrado (para actualización)"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    public ResponseEntity<?> saveInmueble(
            @RequestBody Inmueble inmueble,
            @Parameter(description = "ID del usuario que realiza la solicitud (inyectado por el Gateway)", required = false, hidden = true)
            @RequestHeader(value = "X-User-ID", required = false) Long requestorUserId,
            @Parameter(description = "Roles del usuario que realiza la solicitud (inyectado por el Gateway)", required = false, hidden = true)
            @RequestHeader(value = "X-User-Roles", required = false) String requestorRoles) {

        log.info("POST /api/inmueble llamado por UserID: {}, Roles: {}. Payload: {}", requestorUserId, requestorRoles, inmueble);

        // Validar que para una NUEVA creación, tengamos el userId del creador.
        if (inmueble.getId() == null && requestorUserId == null) {
            log.warn("Intento de crear un inmueble sin X-User-ID.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                 .body("La cabecera X-User-ID es requerida para crear un nuevo inmueble.");
        }

        try {
            // El userId del inmueble se setea en el servicio si es una creación.
            Inmueble savedInmueble = inmuebleService.saveInmueble(inmueble, requestorUserId, requestorRoles);
            // Determinar si fue CREATED o OK basado en si el ID original del DTO era null
            boolean isNew = (inmueble.getId() == null && savedInmueble.getId() != null);
            return new ResponseEntity<>(savedInmueble, isNew ? HttpStatus.CREATED : HttpStatus.OK);
        } catch (SecurityException e) {
            log.warn("Operación no autorizada en saveInmueble: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (RuntimeException e) { // Por ejemplo, si el servicio lanza "Inmueble no encontrado" para una actualización
            log.error("Error en saveInmueble: {}", e.getMessage());
            if (e.getMessage() != null && e.getMessage().contains("no encontrado")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage()); // O INTERNAL_SERVER_ERROR si es más genérico
        }
    }

    /**
     * Endpoint para eliminar un inmueble por su ID.
     * La lógica de permisos se maneja en la capa de servicio.
     *
     * @param inmuebleId      El ID del inmueble a eliminar.
     * @param requestorUserId (Opcional) El ID del usuario que realiza la petición.
     * @param requestorRoles  (Opcional) Los roles del usuario que realiza la petición.
     * @return ResponseEntity indicando el resultado de la operación.
     */
    @DeleteMapping("/{inmuebleId}")
    @Operation(summary = "Eliminar un Inmueble por ID",
               description = "Elimina un inmueble existente. Requiere información del solicitante para permisos.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Inmueble eliminado exitosamente"),
            @ApiResponse(responseCode = "403", description = "No autorizado para realizar esta operación"),
            @ApiResponse(responseCode = "404", description = "Inmueble no encontrado"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    public ResponseEntity<Void> deleteInmueble(
            @PathVariable Long inmuebleId,
            @Parameter(description = "ID del usuario que realiza la solicitud", required = false, hidden = true)
            @RequestHeader(value = "X-User-ID", required = false) Long requestorUserId,
            @Parameter(description = "Roles del usuario que realiza la solicitud", required = false, hidden = true)
            @RequestHeader(value = "X-User-Roles", required = false) String requestorRoles) {

        log.info("DELETE /api/inmueble/{} llamado por UserID: {}, Roles: {}", inmuebleId, requestorUserId, requestorRoles);

        // Validar que tengamos la información del solicitante si se requiere para los permisos.
        if (requestorUserId == null || requestorRoles == null || requestorRoles.isEmpty()) {
             log.warn("Intento de eliminar inmueble ID {} sin cabeceras X-User-ID/X-User-Roles completas.", inmuebleId);
             // Dependiendo de la política: si siempre se requiere un usuario para eliminar
             // return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
             // O permitir que el servicio lo maneje si hay casos donde no se necesiten (poco probable para delete)
        }

        try {
            inmuebleService.deleteInmueble(inmuebleId, requestorUserId, requestorRoles);
            return ResponseEntity.noContent().build(); // 204 No Content
        } catch (SecurityException e) {
            log.warn("Operación no autorizada en deleteInmueble para ID {}: {}", inmuebleId, e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (RuntimeException e) { // Por ejemplo, si el servicio lanza "Inmueble no encontrado"
            log.warn("Error en deleteInmueble para ID {}: {}", inmuebleId, e.getMessage());
            if (e.getMessage() != null && e.getMessage().contains("no encontrado")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build(); // O un error más específico
        }
    }

    /**
     * Endpoint para obtener todos los inmuebles registrados.
     * No requiere información del solicitante ya que es una operación de lectura pública (según la lógica actual).
     *
     * @return ResponseEntity con la lista de inmuebles y el estado OK.
     */
    @GetMapping
    @Operation(summary = "Listar todos los Inmuebles", description = "Recupera una lista completa de todos los inmuebles registrados.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de inmuebles recuperada exitosamente",
                         content = { @Content(mediaType = "application/json", schema = @Schema(type = "array", implementation = Inmueble.class)) }),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    public ResponseEntity<List<Inmueble>> getAllInmuebles() {
        log.debug("GET /api/inmueble - obteniendo todos los inmuebles.");
        List<Inmueble> inmuebles = inmuebleService.findAllInmuebles();
        return ResponseEntity.ok(inmuebles);
    }

    /**
     * Endpoint para obtener un inmueble específico por su ID.
     *
     * @param inmuebleId El ID del inmueble a obtener.
     * @return ResponseEntity con el inmueble encontrado o estado NOT_FOUND.
     */
    @GetMapping("/{inmuebleId}")
    @Operation(summary = "Obtener un Inmueble por ID", description = "Recupera los detalles de un inmueble específico basado en su ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Inmueble encontrado",
                         content = { @Content(mediaType = "application/json", schema = @Schema(implementation = Inmueble.class)) }),
            @ApiResponse(responseCode = "404", description = "Inmueble no encontrado")
    })
    public ResponseEntity<Inmueble> getInmuebleById(@PathVariable Long inmuebleId) {
        log.debug("GET /api/inmueble/{} - obteniendo inmueble por ID.", inmuebleId);
        Optional<Inmueble> inmuebleOptional = inmuebleService.findById(inmuebleId);
        return inmuebleOptional.map(ResponseEntity::ok)
                               .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Endpoint para obtener todos los inmuebles creados por un usuario específico.
     * Requiere que el solicitante sea el mismo usuario o un administrador.
     *
     * @param userId           El ID del usuario cuyos inmuebles se quieren listar.
     * @param requestorUserId  El ID del usuario que realiza la petición (de cabecera X-User-ID).
     * @param requestorRoles   Los roles del usuario que realiza la petición (de cabecera X-User-Roles).
     * @return ResponseEntity con la lista de inmuebles del usuario o estado FORBIDDEN.
     */
    @GetMapping("/user/{userId}")
    @Operation(summary = "Listar Inmuebles por Propietario (UserID)",
               description = "Recupera todos los inmuebles asociados a un ID de usuario específico. Requiere ser el propietario o un admin.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Inmuebles del usuario recuperados exitosamente",
                         content = { @Content(mediaType = "application/json", schema = @Schema(type = "array", implementation = Inmueble.class)) }),
            @ApiResponse(responseCode = "403", description = "No autorizado para ver los inmuebles de este usuario"),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado (si se validara existencia de usuario)")
    })
    public ResponseEntity<List<Inmueble>> getAllInmueblesByUserId(
            @PathVariable Long userId,
            @Parameter(description = "ID del usuario que realiza la solicitud", required = true, hidden = true) // Marcado como requerido para esta lógica
            @RequestHeader(value = "X-User-ID", required = true) Long requestorUserId,
            @Parameter(description = "Roles del usuario que realiza la solicitud", required = true, hidden = true) // Marcado como requerido
            @RequestHeader(value = "X-User-Roles", required = true) String requestorRoles) {

        log.info("GET /api/inmueble/user/{} llamado por UserID: {}, Roles: {}", userId, requestorUserId, requestorRoles);

        boolean isAdmin = requestorRoles.contains("ROLE_ADMIN");
        if (!isAdmin && !requestorUserId.equals(userId)) {
            log.warn("Intento no autorizado de usuario {} para ver inmuebles del usuario {}", requestorUserId, userId);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        List<Inmueble> inmuebles = inmuebleService.findAllByUserId(userId);
        return ResponseEntity.ok(inmuebles);
    }

    // NUEVO ENDPOINT PARA ACTUALIZAR ESTADO
    @PutMapping("/{inmuebleId}/estado")
    @Operation(summary = "Actualizar el estado de un Inmueble",
            description = "Permite cambiar el estado de un inmueble (ej. a VENDIDO). Este endpoint está pensado para ser llamado por otros servicios (ej. servicio de compras) o por un administrador con permisos específicos. La seguridad de quién puede llamar y cambiar a qué estado se maneja internamente o mediante la configuración de seguridad general del servicio.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Estado del inmueble actualizado exitosamente",
                    content = { @Content(mediaType = "application/json", schema = @Schema(implementation = Inmueble.class)) }),
            @ApiResponse(responseCode = "400", description = "Parámetros inválidos (ej. estado no reconocido)"),
            @ApiResponse(responseCode = "403", description = "No autorizado para realizar esta operación (si se implementan permisos granulares aquí)"),
            @ApiResponse(responseCode = "404", description = "Inmueble no encontrado"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    public ResponseEntity<?> updateEstadoInmueble(
            @Parameter(description = "ID del inmueble a actualizar.")
            @PathVariable Long inmuebleId,
            @Parameter(description = "El nuevo estado para el inmueble (DISPONIBLE, VENDIDO, RESERVADO).")
            @RequestParam EstadoInmueble nuevoEstado,
            // Estas cabeceras son opcionales aquí pero pueden ser útiles para auditoría o si se necesita
            // una capa de permiso adicional directamente en este endpoint. Por ahora, la lógica principal
            // de si un servicio puede llamar a otro se basa en la autenticación entre servicios (Basic Auth).
            @Parameter(description = "ID del usuario/servicio que realiza la solicitud (inyectado por el Gateway o por el servicio llamante)", required = false, hidden = true)
            @RequestHeader(value = "X-User-ID", required = false) Long requestorUserId,
            @Parameter(description = "Roles del usuario/servicio que realiza la solicitud", required = false, hidden = true)
            @RequestHeader(value = "X-User-Roles", required = false) String requestorRoles) {

        log.info("PUT /api/inmueble/{}/estado - Cambiando estado a {} (Solicitado por UserID: {}, Roles: {})",
                inmuebleId, nuevoEstado, requestorUserId, requestorRoles);
        try {
            Inmueble updatedInmueble = inmuebleService.updateEstado(inmuebleId, nuevoEstado);
            return ResponseEntity.ok(updatedInmueble);
        } catch (RuntimeException e) { // Idealmente, una excepción más específica como ResourceNotFoundException
            log.warn("Error al actualizar estado para inmueble ID {}: {}", inmuebleId, e.getMessage());
            // Devolver un cuerpo de error más informativo podría ser mejor
            if (e.getMessage() != null && e.getMessage().contains("no encontrado")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Inmueble no encontrado con ID: " + inmuebleId);
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error al actualizar el estado: " + e.getMessage());
        }
    }
}