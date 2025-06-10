package com.dani.spring_boot_microservice_1_inmueble.controller;

import com.dani.spring_boot_microservice_1_inmueble.model.EstadoInmueble;
import com.dani.spring_boot_microservice_1_inmueble.model.Inmueble;
import com.dani.spring_boot_microservice_1_inmueble.service.InmuebleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para gestionar las operaciones CRUD de la entidad {@link Inmueble}.
 * <p>
 * Expone endpoints HTTP que son consumidos principalmente por el API Gateway.
 * Este controlador delega toda la lógica de negocio, incluyendo las validaciones
 * de permisos, al {@link InmuebleService}.
 * <p>
 * Los métodos de modificación y eliminación requieren cabeceras HTTP personalizadas
 * ({@code X-User-ID} y {@code X-User-Roles}) para obtener el contexto del usuario
 * que realiza la petición, propagado por el API Gateway.
 *
 * @author Daniel Núñez Rojas (danidev fullstack software)
 * @version 1.1
 * @since 2025-06-09 (Actualizado con JavaDoc completo)
 */
@RestController
@RequestMapping("api/inmueble")
@RequiredArgsConstructor
@Slf4j
public class InmuebleController {

    private final InmuebleService inmuebleService;

    /**
     * Endpoint para crear o actualizar un inmueble.
     * <p>
     * La lógica para determinar si es una creación o actualización y para verificar
     * los permisos se encuentra en el {@link InmuebleService}.
     *
     * @param inmueble El objeto {@link Inmueble} con los datos a guardar/actualizar,
     * recibido en el cuerpo de la petición.
     * @param userId El ID del usuario que realiza la petición, extraído de la cabecera "X-User-ID".
     * @param userRoles Los roles del usuario que realiza la petición, extraídos de la cabecera "X-User-Roles".
     * @return Un {@link ResponseEntity} con el inmueble guardado y el estado HTTP 201 (Created).
     */
    @PostMapping
    public ResponseEntity<Inmueble> saveInmueble(@RequestBody Inmueble inmueble,
                                                 @RequestHeader("X-User-ID") Long userId,
                                                 @RequestHeader("X-User-Roles") List<String> userRoles) {
        log.info("Recibida petición para guardar/actualizar inmueble con nombre: {} por usuario ID: {}", inmueble.getName(), userId);
        Inmueble savedInmueble = inmuebleService.saveInmueble(inmueble, userId, userRoles);
        return new ResponseEntity<>(savedInmueble, HttpStatus.CREATED);
    }

    /**
     * Endpoint para eliminar un inmueble por su ID.
     * <p>
     * La lógica de verificación de permisos (si el usuario es propietario o administrador)
     * se encuentra en el {@link InmuebleService}.
     *
     * @param inmuebleId El ID del inmueble a eliminar, extraído de la ruta.
     * @param userId El ID del usuario que realiza la petición, extraído de la cabecera "X-User-ID".
     * @param userRoles Los roles del usuario que realiza la petición, extraídos de la cabecera "X-User-Roles".
     * @return Un {@link ResponseEntity} con estado HTTP 200 (OK) si la operación fue exitosa.
     */
    @DeleteMapping("{inmuebleId}")
    public ResponseEntity<Void> deleteInmueble(@PathVariable Long inmuebleId,
                                               @RequestHeader("X-User-ID") Long userId,
                                               @RequestHeader("X-User-Roles") List<String> userRoles) {
        log.info("Recibida petición para eliminar inmueble ID: {} por usuario ID: {}", inmuebleId, userId);
        inmuebleService.deleteInmueble(inmuebleId, userId, userRoles);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * Endpoint para obtener todos los inmuebles registrados.
     * Este endpoint es público y no requiere cabeceras de usuario.
     *
     * @return Un {@link ResponseEntity} con una lista de todos los inmuebles y estado HTTP 200 (OK).
     */
    @GetMapping
    public ResponseEntity<List<Inmueble>> getAllInmuebles() {
        log.debug("Recibida petición para obtener todos los inmuebles.");
        return new ResponseEntity<>(inmuebleService.findAllInmuebles(), HttpStatus.OK);
    }

    /**
     * Endpoint para obtener todos los inmuebles de un usuario específico.
     *
     * @param userId El ID del usuario cuyos inmuebles se desean obtener.
     * @return Un {@link ResponseEntity} con una lista de los inmuebles del usuario y estado HTTP 200 (OK).
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Inmueble>> getAllInmueblesByUserId(@PathVariable Long userId) {
        log.debug("Recibida petición para obtener inmuebles del usuario ID: {}", userId);
        return ResponseEntity.ok(inmuebleService.findAllByUserId(userId));
    }

    /**
     * Endpoint para actualizar únicamente el estado de un inmueble.
     * <p>
     * Este endpoint es utilizado internamente por otros servicios, como el {@code compra-service},
     * para marcar un inmueble como "VENDIDO" después de una transacción exitosa.
     * Está protegido por la configuración de seguridad del servicio.
     *
     * @param inmuebleId El ID del inmueble cuyo estado se actualizará.
     * @param estado El nuevo {@link EstadoInmueble} a asignar.
     * @return Un {@link ResponseEntity} con un mensaje de éxito y estado HTTP 200 (OK).
     */
    @PutMapping("/{inmuebleId}/estado")
    public ResponseEntity<String> updateInmuebleEstado(@PathVariable Long inmuebleId, @RequestParam EstadoInmueble estado) {
        log.info("Recibida petición para actualizar estado del inmueble ID: {} a {}", inmuebleId, estado);
        try {
            inmuebleService.updateInmuebleEstado(inmuebleId, estado);
            return ResponseEntity.ok("Estado del inmueble actualizado correctamente.");
        } catch (Exception e) {
            log.error("Error al actualizar estado para el inmueble ID {}: {}", inmuebleId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al actualizar estado del inmueble.");
        }
    }

    /**
     * Endpoint para obtener un inmueble específico por su ID.
     *
     * @param inmuebleId El ID del inmueble a buscar.
     * @return Un {@link ResponseEntity} que contiene el {@link Inmueble} si se encuentra (con estado 200 OK),
     * o un estado 404 (Not Found) si no existe.
     */
    @GetMapping("/{inmuebleId}")
    public ResponseEntity<Inmueble> getInmuebleById(@PathVariable Long inmuebleId) {
        log.debug("Recibida petición para obtener inmueble por ID: {}", inmuebleId);
        return inmuebleService.findById(inmuebleId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}