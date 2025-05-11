package com.dani.spring_boot_microservice_3_api_gateway.controller;

import com.dani.spring_boot_microservice_3_api_gateway.model.Role;
import com.dani.spring_boot_microservice_3_api_gateway.model.User; // Importar User
import com.dani.spring_boot_microservice_3_api_gateway.security.UserPrincipal;
import com.dani.spring_boot_microservice_3_api_gateway.service.UserService;
import lombok.RequiredArgsConstructor; // Importar
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

// Imports OpenAPI
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Controlador REST para operaciones relacionadas con la cuenta del usuario autenticado.
 */
@RestController
@RequestMapping("api/user")
@RequiredArgsConstructor // Lombok para inyección por constructor
@Tag(name = "Gestión de Cuenta de Usuario", description = "Operaciones relacionadas con el usuario autenticado.")
// Indica que estos endpoints requieren autenticación (JWT)
@SecurityRequirement(name = "bearerAuth") // Asume que has definido un SecurityScheme llamado "bearerAuth" globalmente (lo veremos después)
public class UserController {

    // Inyección por constructor (final)
    private final UserService userService;

    /**
     * Endpoint para cambiar el rol de un usuario (actualmente el propio usuario autenticado).
     * Requiere que el usuario esté autenticado.
     * @param userPrincipal Objeto con detalles del usuario autenticado (inyectado por Spring Security).
     * @param role El nuevo rol a asignar (recibido como PathVariable).
     * @return ResponseEntity con estado OK si el cambio fue exitoso.
     */
    @PutMapping("change/{role}")
    @Operation(summary = "Cambiar el rol del usuario autenticado", description = "Permite al usuario autenticado cambiar su propio rol (ej. USER a ADMIN si tiene permisos).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Rol cambiado exitosamente", content = @Content),
            @ApiResponse(responseCode = "400", description = "Rol inválido", content = @Content),
            @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content),
            @ApiResponse(responseCode = "403", description = "No autorizado para cambiar rol", content = @Content), // Si hubiera lógica de permisos
            @ApiResponse(responseCode = "500", description = "Error interno del servidor", content = @Content)
    })
    // ResponseEntity específico <Boolean> o <Void>
    public ResponseEntity<Boolean> changeRol(
            @AuthenticationPrincipal UserPrincipal userPrincipal, // Obtiene el usuario logueado
            @Parameter(description = "El nuevo rol a asignar (USER o ADMIN)", required = true)
            @PathVariable Role role) {

        userService.changeRole(role, userPrincipal.getUsername());
        // Devolver true o simplemente OK
        return ResponseEntity.ok(true);
        // o return ResponseEntity.ok().build(); si no se necesita cuerpo
    }

    /**
     * Endpoint para obtener los detalles del usuario actualmente autenticado.
     * @param userPrincipal Objeto con detalles del usuario autenticado.
     * @return ResponseEntity con los datos del usuario (incluyendo un token JWT refrescado) y estado OK.
     */
    @GetMapping()
    @Operation(summary = "Obtener datos del usuario autenticado", description = "Devuelve la información del usuario que ha iniciado sesión, incluyendo un token JWT.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Datos del usuario obtenidos",
                    content = { @Content(mediaType = "application/json", schema = @Schema(implementation = User.class)) }),
            @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor", content = @Content)
    })
    // ResponseEntity específico <User>
    public ResponseEntity<User> getCurrentUser(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        // Llama al servicio que busca por username y añade el token
        User userWithToken = userService.findByUserameReturnToken(userPrincipal.getUsername());
        return new ResponseEntity<>(userWithToken, HttpStatus.OK);
    }
}