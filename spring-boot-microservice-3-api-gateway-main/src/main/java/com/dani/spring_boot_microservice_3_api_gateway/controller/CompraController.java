package com.dani.spring_boot_microservice_3_api_gateway.controller;

import com.dani.spring_boot_microservice_3_api_gateway.dto.CompraDto; // Importar DTO
import com.dani.spring_boot_microservice_3_api_gateway.request.CompraServiceRequest;
import com.dani.spring_boot_microservice_3_api_gateway.security.UserPrincipal;
import lombok.RequiredArgsConstructor; // Importar
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

// Imports OpenAPI
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List; // Importar

/**
 * Controlador Gateway que actúa como proxy para el microservicio de Compras.
 * Redirige las peticiones al servicio correspondiente usando Feign.
 * Aplica seguridad basada en el usuario autenticado.
 */
@RestController
@RequestMapping("gateway/compra")
@RequiredArgsConstructor // Lombok para inyección
@Tag(name = "Gateway - Compras", description = "Proxy para operaciones de Compras.")
@SecurityRequirement(name = "bearerAuth") // Requiere autenticación JWT
public class CompraController {

    // Inyección por constructor del cliente Feign
    private final CompraServiceRequest compraServiceRequest;

    /**
     * Endpoint Gateway para guardar una nueva compra.
     * Llama al endpoint correspondiente en compra-service.
     * @param compraDto Datos de la compra a guardar.
     * @return ResponseEntity con la compra guardada (como DTO) y estado CREATED.
     */
    @PostMapping
    @Operation(summary = "Registrar Compra (vía Gateway)", description = "Envía la petición de registro de compra al servicio de compras.")
    @ApiResponses(value = { @ApiResponse(responseCode = "201", description = "Compra registrada") }) // Simplificado
    // Usa DTO en RequestBody y ResponseEntity
    public ResponseEntity<CompraDto> saveCompra(@RequestBody CompraDto compraDto) {
        // NOTA: Aquí podríamos querer asignar el userId del usuario autenticado
        // si el DTO no lo trae o para asegurar que sea el correcto.
        // Ejemplo: compraDto.setUserId(userPrincipal.getId()); --> Necesitaría que CompraDto fuera mutable o usar un builder.
        // O pasar userPrincipal.getId() al método saveCompra si la interfaz Feign lo permite.
        // Por ahora, asumimos que el DTO viene completo o el servicio destino lo maneja.
        CompraDto savedDto = compraServiceRequest.saveCompra(compraDto);
        return new ResponseEntity<>(savedDto, HttpStatus.CREATED);
    }

    /**
     * Endpoint Gateway para obtener todas las compras del usuario autenticado.
     * Llama al endpoint correspondiente en compra-service usando el ID del usuario autenticado.
     * @param userPrincipal Detalles del usuario autenticado inyectado por Spring Security.
     * @return ResponseEntity con la lista de compras (como DTOs) y estado OK.
     */
    @GetMapping()
    @Operation(summary = "Listar mis Compras (vía Gateway)", description = "Solicita las compras del usuario autenticado al servicio de compras.")
    @ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Lista obtenida") }) // Simplificado
    // Usa List<CompraDto> en ResponseEntity
    public ResponseEntity<List<CompraDto>> getAllComprasOfUser(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        // Usa el ID del UserPrincipal para llamar al servicio
        List<CompraDto> compras = compraServiceRequest.getAllComprasOfUser(userPrincipal.getId());
        return ResponseEntity.ok(compras);
    }
}