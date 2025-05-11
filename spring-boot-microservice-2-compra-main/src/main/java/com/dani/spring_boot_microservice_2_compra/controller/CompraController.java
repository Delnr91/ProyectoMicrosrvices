package com.dani.spring_boot_microservice_2_compra.controller;

import com.dani.spring_boot_microservice_2_compra.model.Compra;
import com.dani.spring_boot_microservice_2_compra.service.CompraService;
import lombok.RequiredArgsConstructor; // Importar
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

// Imports OpenAPI (opcional)
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.Parameter; // Para describir parámetros

import java.util.List; // Importar

/**
 * Controlador REST para gestionar las operaciones relacionadas con las Compras.
 */
@RestController
@RequestMapping("api/compra")
@RequiredArgsConstructor // Inyección por constructor vía Lombok
@Tag(name = "Gestión de Compras", description = "API para registrar y consultar compras.") // OpenAPI Tag
public class CompraController {

    // Inyección por constructor
    private final CompraService compraService;

    /**
     * Endpoint para guardar (registrar) una nueva compra.
     * @param compra Datos de la compra a registrar (enviados en el cuerpo de la petición).
     * @return ResponseEntity con la compra registrada y estado CREATED.
     */
    @PostMapping()
    @Operation(summary = "Registrar una nueva compra", description = "Guarda los detalles de una nueva compra en la base de datos.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Compra registrada exitosamente",
                    content = { @Content(mediaType = "application/json", schema = @Schema(implementation = Compra.class)) }),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos", content = @Content),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor", content = @Content)
    })
    // ResponseEntity específico <Compra>
    public ResponseEntity<Compra> saveCompra(@RequestBody Compra compra) {
        Compra savedCompra = compraService.saveCompra(compra);
        return new ResponseEntity<>(savedCompra, HttpStatus.CREATED);
    }

    /**
     * Endpoint para obtener todas las compras realizadas por un usuario específico.
     * @param userId El ID del usuario cuyas compras se quieren obtener (recibido en la ruta).
     * @return ResponseEntity con la lista de compras del usuario y estado OK.
     */
    @GetMapping("{userId}")
    @Operation(summary = "Listar compras por usuario", description = "Recupera todas las compras asociadas a un ID de usuario específico.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Compras recuperadas exitosamente",
                    content = { @Content(mediaType = "application/json", schema = @Schema(type = "array", implementation = Compra.class)) }),
            // Podríamos añadir 404 si el usuario no existe o no tiene compras, dependiendo de la lógica deseada.
            @ApiResponse(responseCode = "500", description = "Error interno del servidor", content = @Content)
    })
    // ResponseEntity específico <List<Compra>>
    public ResponseEntity<List<Compra>> getAllComprasOfUser(
            // Describe el path variable userId
            @Parameter(description = "ID del usuario para buscar sus compras", required = true)
            @PathVariable Long userId) {

        List<Compra> compras = compraService.findAllComprasOfUser(userId);
        return ResponseEntity.ok(compras);
    }
}