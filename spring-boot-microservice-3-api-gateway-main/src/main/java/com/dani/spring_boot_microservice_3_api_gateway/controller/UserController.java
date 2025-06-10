package com.dani.spring_boot_microservice_3_api_gateway.controller;

import com.dani.spring_boot_microservice_3_api_gateway.model.Role;
import com.dani.spring_boot_microservice_3_api_gateway.model.User;
import com.dani.spring_boot_microservice_3_api_gateway.security.UserPrincipal;
import com.dani.spring_boot_microservice_3_api_gateway.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para la gestión de usuarios.
 * <p>
 * Proporciona endpoints de API para que los usuarios gestionen su propia información
 * y para que los administradores gestionen todos los usuarios del sistema.
 * Las operaciones sensibles están protegidas mediante autorización a nivel de método
 * con {@link PreAuthorize}.
 *
 * @author Daniel Núñez Rojas (danidev fullstack software)
 * @version 1.1
 * @since 2025-06-05 (Actualizado con JavaDoc completo)
 */
@RestController
@RequestMapping("api/user")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;

    /**
     * Permite a un usuario autenticado cambiar su propio rol.
     * La lógica de negocio para permitir o denegar el cambio (ej. límite de admins)
     * se encuentra en el {@link UserService}.
     *
     * @param userPrincipal El principal del usuario autenticado, inyectado por Spring Security.
     * @param role El nuevo rol a asignar, extraído de la ruta.
     * @return {@link ResponseEntity} con estado 200 (OK) si el cambio es exitoso,
     * o 400 (Bad Request) con un mensaje de error si no es posible.
     */
    @PutMapping("change/{role}")
    public ResponseEntity<?> changeRol(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Role role) {

        try {
            log.info("Usuario {} intentando cambiar su propio rol a {}", userPrincipal.getUsername(), role);
            userService.changeRole(role, userPrincipal.getUsername());
            log.info("Rol de {} cambiado exitosamente a {}", userPrincipal.getUsername(), role);
            return ResponseEntity.ok(true);
        } catch (RuntimeException e) {
            log.warn("Error al cambiar rol para {}: {}", userPrincipal.getUsername(), e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Devuelve los detalles del usuario actualmente autenticado, incluyendo un token JWT refrescado.
     *
     * @param userPrincipal El principal del usuario autenticado.
     * @return {@link ResponseEntity} con el objeto {@link User} (y su token) y estado 200 (OK).
     */
    @GetMapping()
    public ResponseEntity<User> getCurrentUser(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        User userWithToken = userService.findByUserameReturnToken(userPrincipal.getUsername());
        return new ResponseEntity<>(userWithToken, HttpStatus.OK);
    }

    // --- ENDPOINTS DE ADMINISTRACIÓN ---

    /**
     * [ADMIN] Devuelve una lista de todos los usuarios registrados.
     * Protegido para ser accesible solo por usuarios con rol 'ADMIN'.
     * Las contraseñas y tokens se eliminan de la respuesta por seguridad.
     *
     * @return {@link ResponseEntity} con una lista de todos los usuarios y estado 200 (OK).
     */
    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<User>> getAllUsers() {
        log.info("Admin: Solicitando lista de todos los usuarios.");
        List<User> users = userService.findAllUsers();
        users.forEach(user -> {
            user.setPassword(null);
            user.setToken(null);
        });
        return ResponseEntity.ok(users);
    }

    /**
     * [ADMIN] Busca y devuelve un usuario específico por su ID.
     * Protegido para ser accesible solo por usuarios con rol 'ADMIN'.
     *
     * @param id El ID del usuario a buscar.
     * @return {@link ResponseEntity} con el usuario encontrado y estado 200 (OK),
     * o 404 (Not Found) si no existe.
     */
    @GetMapping("/admin/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<User> getUserByIdForAdmin(@PathVariable Long id) {
        log.info("Admin: Solicitando usuario con ID: {}", id);
        return userService.findUserById(id)
                .map(user -> {
                    user.setPassword(null);
                    user.setToken(null);
                    return ResponseEntity.ok(user);
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * [ADMIN] Actualiza los datos de un usuario (nombre y rol).
     * Protegido para ser accesible solo por usuarios con rol 'ADMIN'.
     *
     * @param id El ID del usuario a actualizar.
     * @param userUpdateRequest Objeto {@link User} con los nuevos datos.
     * @param adminPrincipal El principal del administrador que realiza la acción.
     * @return {@link ResponseEntity} con el usuario actualizado y estado 200 (OK),
     * o un estado de error (404, 400) si ocurre un problema.
     */
    @PutMapping("/admin/update/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateUserByAdmin(
            @PathVariable Long id,
            @RequestBody User userUpdateRequest,
            @AuthenticationPrincipal UserPrincipal adminPrincipal) {
        log.info("Admin {}: Intentando actualizar usuario ID: {}", adminPrincipal.getUsername(), id);
        try {
            User updatedUser = userService.updateUserByAdmin(id, userUpdateRequest, adminPrincipal.getUsername());
            updatedUser.setPassword(null);
            updatedUser.setToken(null);
            return ResponseEntity.ok(updatedUser);
        } catch (UsernameNotFoundException e) {
            log.warn("Admin {}: Usuario no encontrado al intentar actualizar ID {}", adminPrincipal.getUsername(), id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (RuntimeException e) {
            log.warn("Admin {}: Error al actualizar usuario ID {}: {}", adminPrincipal.getUsername(), id, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    /**
     * [ADMIN] Elimina un usuario por su ID.
     * Protegido para ser accesible solo por usuarios con rol 'ADMIN'.
     *
     * @param id El ID del usuario a eliminar.
     * @param adminPrincipal El principal del administrador que realiza la acción.
     * @return {@link ResponseEntity} con estado 204 (No Content) si es exitoso,
     * o un estado de error (404, 400) si ocurre un problema.
     */
    @DeleteMapping("/admin/delete/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteUserByAdmin(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal adminPrincipal) {
        log.info("Admin {}: Intentando eliminar usuario ID: {}", adminPrincipal.getUsername(), id);
        try {
            userService.deleteUserByAdmin(id, adminPrincipal.getUsername());
            return ResponseEntity.noContent().build();
        } catch (UsernameNotFoundException e) {
            log.warn("Admin {}: Usuario no encontrado al intentar eliminar ID {}: {}", adminPrincipal.getUsername(), id, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (RuntimeException e) {
            log.warn("Admin {}: Error al eliminar usuario ID {}: {}", adminPrincipal.getUsername(), id, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}