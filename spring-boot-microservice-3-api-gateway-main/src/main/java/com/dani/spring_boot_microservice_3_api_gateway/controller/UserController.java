package com.dani.spring_boot_microservice_3_api_gateway.controller;

import com.dani.spring_boot_microservice_3_api_gateway.model.Role;
import com.dani.spring_boot_microservice_3_api_gateway.model.User;
import com.dani.spring_boot_microservice_3_api_gateway.security.UserPrincipal;
import com.dani.spring_boot_microservice_3_api_gateway.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j; // AÑADIR PARA LOGGING
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize; // Para seguridad a nivel de método
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.util.List; // AÑADIR PARA LISTAR USUARIOS
import java.util.Optional;
/**
 * Controlador REST para operaciones relacionadas con la cuenta del usuario autenticado.
 */
@RestController
@RequestMapping("api/user")
@RequiredArgsConstructor // Lombok para inyección por constructor
@Tag(name = "Gestión de Cuenta de Usuario", description = "Operaciones relacionadas con el usuario autenticado.")
// Indica que estos endpoints requieren autenticación (JWT)
@SecurityRequirement(name = "bearerAuth") // Asume que has definido un SecurityScheme llamado "bearerAuth" globalmente (lo veremos después)
@Slf4j
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
    public ResponseEntity<?> changeRol(
            @AuthenticationPrincipal UserPrincipal userPrincipal, // Obtiene el usuario logueado
            @Parameter(description = "El nuevo rol a asignar (USER o ADMIN)", required = true)
            @PathVariable Role role) {

        try {
            log.info("Usuario {} intentando cambiar su propio rol a {}", userPrincipal.getUsername(), role);
            userService.changeRole(role, userPrincipal.getUsername()); // Llama al método existente que tiene la lógica de límite de admins
            log.info("Rol de {} cambiado exitosamente a {}", userPrincipal.getUsername(), role);
            return ResponseEntity.ok(true);
        } catch (RuntimeException e) {
            log.warn("Error al cambiar rol para {}: {}", userPrincipal.getUsername(), e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
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

    // --- ENDPOINTS DE ADMINISTRACIÓN DE USUARIOS ---
    // Estos estarán bajo /api/user/admin/** y requerirán ROLE_ADMIN

    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('ADMIN')") // Solo Admins pueden acceder
    @Operation(summary = "Listar todos los usuarios (Admin)", description = "Obtiene una lista de todos los usuarios registrados. Requiere rol ADMIN.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de usuarios recuperada",
                    content = { @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = User.class))) }),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "No autorizado (no es ADMIN)")
    })
    public ResponseEntity<List<User>> getAllUsers() {
        log.info("Admin: Solicitando lista de todos los usuarios.");
        List<User> users = userService.findAllUsers();
        // Por seguridad, es buena práctica no devolver la contraseña en las listas, incluso hasheada.
        // Puedes mapear a un DTO o poner el campo password como @JsonIgnore en la entidad User al serializar para este caso.
        // Por ahora, lo dejaremos así, pero considéralo. Tokens tampoco son necesarios aquí.
        users.forEach(user -> {
            user.setPassword(null); // No exponer contraseñas
            user.setToken(null);    // No exponer tokens
        });
        return ResponseEntity.ok(users);
    }

    @GetMapping("/admin/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Obtener un usuario por ID (Admin)", description = "Recupera los detalles de un usuario específico por su ID. Requiere rol ADMIN.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuario encontrado",
                    content = { @Content(mediaType = "application/json", schema = @Schema(implementation = User.class)) }),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado"),
            @ApiResponse(responseCode = "403", description = "No autorizado")
    })
    public ResponseEntity<User> getUserByIdForAdmin(@PathVariable Long id) {
        log.info("Admin: Solicitando usuario con ID: {}", id);
        Optional<User> userOptional = userService.findUserById(id);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            user.setPassword(null); // No exponer contraseña
            user.setToken(null);
            return ResponseEntity.ok(user);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/admin/update/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Actualizar un usuario (Admin)", description = "Actualiza el nombre y/o rol de un usuario. Requiere rol ADMIN. No permite modificar al admin principal (testuser) por otros admins, ni cambiar el rol del admin principal a USER.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuario actualizado",
                    content = { @Content(mediaType = "application/json", schema = @Schema(implementation = User.class)) }),
            @ApiResponse(responseCode = "400", description = "Solicitud inválida (ej. violación de reglas de admin)"),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado"),
            @ApiResponse(responseCode = "403", description = "No autorizado")
    })
    public ResponseEntity<?> updateUserByAdmin(
            @PathVariable Long id,
            @RequestBody User userUpdateRequest, // Espera un User con nombre y/o rol
            @AuthenticationPrincipal UserPrincipal adminPrincipal) {
        log.info("Admin {}: Intentando actualizar usuario ID: {} con datos: Nombre={}, Rol={}",
                adminPrincipal.getUsername(), id, userUpdateRequest.getNombre(), userUpdateRequest.getRole());
        try {
            // Creamos un DTO o un User solo con los campos que queremos que se actualicen
            // para evitar que se pasen otros campos sensibles en el request.
            // Por ahora, el userUpdateRequest podría tener solo nombre y rol.
            User updatedUser = userService.updateUserByAdmin(id, userUpdateRequest, adminPrincipal.getUsername());
            updatedUser.setPassword(null); // No devolver contraseña
            updatedUser.setToken(null);
            return ResponseEntity.ok(updatedUser);
        } catch (UsernameNotFoundException e) {
            log.warn("Admin {}: Usuario no encontrado al intentar actualizar ID {}: {}", adminPrincipal.getUsername(), id, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (RuntimeException e) { // Captura las excepciones de lógica de negocio del servicio
            log.warn("Admin {}: Error al actualizar usuario ID {}: {}", adminPrincipal.getUsername(), id, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @DeleteMapping("/admin/delete/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Eliminar un usuario (Admin)", description = "Elimina un usuario por su ID. Requiere rol ADMIN. No permite eliminar al admin principal (testuser).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Usuario eliminado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Solicitud inválida (ej. intento de eliminar admin principal)"),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado"),
            @ApiResponse(responseCode = "403", description = "No autorizado")
    })
    public ResponseEntity<?> deleteUserByAdmin(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal adminPrincipal) {
        log.info("Admin {}: Intentando eliminar usuario ID: {}", adminPrincipal.getUsername(), id);
        try {
            userService.deleteUserByAdmin(id, adminPrincipal.getUsername());
            return ResponseEntity.noContent().build(); // 204 No Content
        } catch (UsernameNotFoundException e) {
            log.warn("Admin {}: Usuario no encontrado al intentar eliminar ID {}: {}", adminPrincipal.getUsername(), id, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (RuntimeException e) { // Captura la excepción si se intenta eliminar al admin principal
            log.warn("Admin {}: Error al eliminar usuario ID {}: {}", adminPrincipal.getUsername(), id, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}