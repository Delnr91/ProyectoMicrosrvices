package com.dani.spring_boot_microservice_3_api_gateway.controller;

import com.dani.spring_boot_microservice_3_api_gateway.dto.InmuebleDto;
import com.dani.spring_boot_microservice_3_api_gateway.request.InmuebleServiceRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST que actúa como un proxy o fachada para el microservicio de Inmuebles.
 * <p>
 * Expone endpoints en el API Gateway que redirigen las peticiones al servicio
 * {@code inmueble-service} a través de un cliente Feign ({@link InmuebleServiceRequest}).
 * Esto centraliza el acceso y la seguridad sin exponer directamente los microservicios internos.
 *
 * @author Daniel Núñez Rojas (danidev fullstack software)
 * @version 1.1
 * @since 2025-06-09 (Actualizado con JavaDoc completo)
 */
@RestController
@RequestMapping("gateway/inmueble")
@RequiredArgsConstructor
public class InmuebleController {

    private final InmuebleServiceRequest inmuebleServiceRequest;

    /**
     * Endpoint para guardar o actualizar un inmueble.
     * Delega la llamada al endpoint POST del {@code inmueble-service}.
     *
     * @param inmuebleDto DTO del inmueble a guardar.
     * @return Un {@link ResponseEntity} con el DTO del inmueble guardado y estado CREATED.
     */
    @PostMapping
    public ResponseEntity<InmuebleDto> saveInmueble(@RequestBody InmuebleDto inmuebleDto) {
        InmuebleDto savedDto = inmuebleServiceRequest.saveInmueble(inmuebleDto);
        return new ResponseEntity<>(savedDto, HttpStatus.CREATED);
    }

    /**
     * Endpoint para eliminar un inmueble por su ID.
     * Delega la llamada al endpoint DELETE del {@code inmueble-service}.
     *
     * @param inmuebleId El ID del inmueble a eliminar.
     * @return Un {@link ResponseEntity} con estado OK si la operación fue exitosa.
     */
    @DeleteMapping("{inmuebleId}")
    public ResponseEntity<Void> deleteInmueble(@PathVariable("inmuebleId") Long inmuebleId) {
        inmuebleServiceRequest.deleteInmueble(inmuebleId);
        return ResponseEntity.ok().build();
    }

    /**
     * Endpoint para obtener todos los inmuebles.
     * Delega la llamada al endpoint GET del {@code inmueble-service}.
     *
     * @return Un {@link ResponseEntity} con una lista de todos los inmuebles y estado OK.
     */
    @GetMapping()
    public ResponseEntity<List<InmuebleDto>> getAllInmuebles() {
        List<InmuebleDto> inmuebles = inmuebleServiceRequest.getAllInmuebles();
        return ResponseEntity.ok(inmuebles);
    }
}