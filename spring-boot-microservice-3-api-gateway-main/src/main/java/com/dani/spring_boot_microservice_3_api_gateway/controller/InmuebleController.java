package com.dani.spring_boot_microservice_3_api_gateway.controller;

import com.dani.spring_boot_microservice_3_api_gateway.dto.InmuebleDto; // Importar DTO
import com.dani.spring_boot_microservice_3_api_gateway.request.InmuebleServiceRequest;
import lombok.RequiredArgsConstructor; // Importar
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

// Imports OpenAPI
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List; // Importar

/**
 * Controlador Gateway que actúa como proxy para el microservicio de Inmuebles.
 * Redirige las peticiones al servicio correspondiente usando Feign.
 */
@RestController
@RequestMapping("gateway/inmueble")
@RequiredArgsConstructor // Lombok para inyección
@Tag(name = "Gateway - Inmuebles", description = "Proxy para operaciones de Inmuebles.")
public class InmuebleController {

    // Inyección por constructor del cliente Feign
    private final InmuebleServiceRequest inmuebleServiceRequest;

    /**
     * Endpoint Gateway para guardar un nuevo inmueble.
     * Llama al endpoint correspondiente en inmueble-service.
     * @param inmuebleDto Datos del inmueble a guardar.
     * @return ResponseEntity con el inmueble guardado (como DTO) y estado CREATED.
     */
    @PostMapping
    @Operation(summary = "Crear Inmueble (vía Gateway)", description = "Envía la petición de creación al servicio de inmuebles.")
    @ApiResponses(value = { @ApiResponse(responseCode = "201", description = "Inmueble creado") }) // Simplificado
    // Usa DTO en RequestBody y ResponseEntity
    public ResponseEntity<InmuebleDto> saveInmueble(@RequestBody InmuebleDto inmuebleDto) {
        InmuebleDto savedDto = inmuebleServiceRequest.saveInmueble(inmuebleDto);
        return new ResponseEntity<>(savedDto, HttpStatus.CREATED);
    }

    /**
     * Endpoint Gateway para eliminar un inmueble por ID.
     * Llama al endpoint correspondiente en inmueble-service.
     * @param inmuebleId ID del inmueble a eliminar.
     * @return ResponseEntity con estado OK.
     */
    @DeleteMapping("{inmuebleId}")
    @Operation(summary = "Eliminar Inmueble (vía Gateway)", description = "Envía la petición de eliminación al servicio de inmuebles.")
    @ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Inmueble eliminado (o no encontrado)") }) // Simplificado
    public ResponseEntity<Void> deleteInmueble(@PathVariable("inmuebleId") Long inmuebleId) {
        inmuebleServiceRequest.deleteInmueble(inmuebleId);
        return ResponseEntity.ok().build();
    }

    /**
     * Endpoint Gateway para obtener todos los inmuebles.
     * Llama al endpoint correspondiente en inmueble-service.
     * @return ResponseEntity con la lista de inmuebles (como DTOs) y estado OK.
     */
    @GetMapping()
    @Operation(summary = "Listar todos los Inmuebles (vía Gateway)", description = "Solicita la lista completa de inmuebles al servicio correspondiente.")
    @ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Lista obtenida") }) // Simplificado
    // Usa List<InmuebleDto> en ResponseEntity
    public ResponseEntity<List<InmuebleDto>> getAllInmuebles() {
        List<InmuebleDto> inmuebles = inmuebleServiceRequest.getAllInmuebles();
        return ResponseEntity.ok(inmuebles);
    }

}