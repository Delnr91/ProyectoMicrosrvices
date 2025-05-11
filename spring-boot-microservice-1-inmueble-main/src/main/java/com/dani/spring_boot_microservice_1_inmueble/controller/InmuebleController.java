package com.dani.spring_boot_microservice_1_inmueble.controller;

import com.dani.spring_boot_microservice_1_inmueble.model.Inmueble;
import com.dani.spring_boot_microservice_1_inmueble.service.InmuebleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

// Imports para anotaciones OpenAPI/Swagger
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content; // Para describir contenido de respuesta
import io.swagger.v3.oas.annotations.media.Schema; // Para describir estructura de datos
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag; // Para agrupar endpoints

import java.util.List;

/**
 * Controlador REST para gestionar las operaciones CRUD de Inmuebles.
 * Expone los endpoints de la API relacionados con los inmuebles.
 * Documentado con OpenAPI para Swagger UI.
 */
@RestController
@RequestMapping("api/inmueble")
@RequiredArgsConstructor
// @Tag: Agrupa los endpoints de este controlador bajo el nombre "Gestión de Inmuebles" en Swagger UI.
@Tag(name = "Gestión de Inmuebles", description = "API para realizar operaciones CRUD sobre inmuebles.")
public class InmuebleController {

    private final InmuebleService inmuebleService;

    /**
     * Endpoint para guardar un nuevo inmueble.
     * @param inmueble El objeto Inmueble recibido en el cuerpo de la petición.
     * @return ResponseEntity con el inmueble guardado y el estado CREATED.
     */
    @PostMapping
    // @Operation: Describe qué hace este endpoint.
    @Operation(summary = "Crear un nuevo inmueble", description = "Registra un nuevo inmueble en la base de datos.")
    // @ApiResponses: Define las posibles respuestas HTTP.
    @ApiResponses(value = {
            // Describe la respuesta 201 CREATED: Indica éxito y devuelve el inmueble creado.
            @ApiResponse(responseCode = "201", description = "Inmueble creado exitosamente",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Inmueble.class)) }),
            // Describe una posible respuesta 400 BAD REQUEST: Si los datos enviados no son válidos (ejemplo).
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos", content = @Content),
            // Describe una posible respuesta 500 INTERNAL SERVER ERROR.
            @ApiResponse(responseCode = "500", description = "Error interno del servidor", content = @Content)
    })
    public ResponseEntity<Inmueble> saveInmueble(@RequestBody Inmueble inmueble) {
        Inmueble savedInmueble = inmuebleService.saveInmueble(inmueble);
        // Devuelve 201 Created en lugar de 200 OK para operaciones de creación.
        return new ResponseEntity<>(savedInmueble, HttpStatus.CREATED);
    }

    /**
     * Endpoint para eliminar un inmueble por su ID.
     * @param inmuebleId El ID del inmueble a eliminar (recibido en la ruta).
     * @return ResponseEntity con estado OK si la operación fue exitosa.
     */
    @DeleteMapping("{inmuebleId}")
    @Operation(summary = "Eliminar un inmueble", description = "Elimina un inmueble existente basado en su ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Inmueble eliminado exitosamente", content = @Content),
            // Podríamos añadir 404 Not Found si el ID no existe, aunque el servicio actual no lo maneja explícitamente.
            @ApiResponse(responseCode = "404", description = "Inmueble no encontrado", content = @Content),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor", content = @Content)
    })
    public ResponseEntity<Void> deleteInmueble(@PathVariable Long inmuebleId) {
        // NOTA: Para devolver 404, el servicio debería lanzar una excepción si no encuentra el ID,
        // y tendríamos un @ExceptionHandler o similar para convertirla en 404.
        // Por ahora, asumimos que siempre funciona o lanza una excepción genérica (500).
        inmuebleService.deleteInmueble(inmuebleId);
        return ResponseEntity.ok().build();
    }

    /**
     * Endpoint para obtener todos los inmuebles registrados.
     * @return ResponseEntity con la lista de inmuebles y el estado OK.
     */
    @GetMapping
    @Operation(summary = "Listar todos los inmuebles", description = "Recupera una lista completa de todos los inmuebles registrados.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de inmuebles recuperada exitosamente",
                    content = { @Content(mediaType = "application/json",
                            // Indica que la respuesta es un array de objetos Inmueble.
                            schema = @Schema(type = "array", implementation = Inmueble.class)) }),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor", content = @Content)
    })
    public ResponseEntity<List<Inmueble>> getAllInmuebles() {
        List<Inmueble> inmuebles = inmuebleService.findAllInmuebles();
        return ResponseEntity.ok(inmuebles);
    }
}