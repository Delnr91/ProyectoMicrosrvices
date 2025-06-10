package com.dani.spring_boot_microservice_3_api_gateway.controller.ui;

import com.dani.spring_boot_microservice_3_api_gateway.model.Role;
import com.dani.spring_boot_microservice_3_api_gateway.model.User;
import com.dani.spring_boot_microservice_3_api_gateway.security.UserPrincipal;
import com.dani.spring_boot_microservice_3_api_gateway.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * Controlador para la interfaz de usuario (UI) de administración de usuarios.
 * <p>
 * Maneja las vistas de Thymeleaf que permiten a los administradores listar,
 * ver, editar y eliminar usuarios del sistema. Todos los endpoints en este
 * controlador están implícitamente protegidos por la configuración de seguridad
 * para requerir el rol de ADMIN.
 *
 * @author Daniel Núñez Rojas (danidev fullstack software)
 * @version 1.0
 * @since 2025-05-27
 */
@Controller
@RequestMapping("/ui/admin/usuarios")
@RequiredArgsConstructor
@Slf4j
public class AdminUserUIController {

    private final UserService userService;

    /**
     * Muestra la página con la lista de todos los usuarios registrados.
     *
     * @param model El objeto {@link Model} para pasar la lista de usuarios a la vista.
     * @return El nombre de la vista Thymeleaf ({@code "admin/usuarios/lista-usuarios"}).
     */
    @GetMapping
    public String viewUsersPage(Model model) {
        List<User> users = userService.findAllUsers();
        users.forEach(u -> u.setPassword(null)); // Asegurar de no pasar contraseñas a la vista
        model.addAttribute("users", users);
        return "admin/usuarios/lista-usuarios";
    }

    /**
     * Muestra el formulario para editar un usuario específico.
     *
     * @param userId El ID del usuario a editar.
     * @param model El objeto {@link Model} para pasar los datos del usuario al formulario.
     * @return El nombre de la vista Thymeleaf ({@code "admin/usuarios/form-usuarios"}).
     */
    @GetMapping("/edit/{id}")
    public String showEditUserForm(@PathVariable("id") Long userId, Model model) {
        User user = userService.findUserById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid user Id:" + userId));
        model.addAttribute("user", user);
        model.addAttribute("allRoles", Role.values());
        return "admin/usuarios/form-usuarios";
    }

    /**
     * Procesa la solicitud de actualización de un usuario por parte de un administrador.
     *
     * @param userId El ID del usuario que se está actualizando.
     * @param user El objeto {@link User} con los datos actualizados desde el formulario.
     * @param adminPrincipal El principal del administrador que realiza la operación.
     * @param redirectAttributes Utilizado para pasar mensajes flash tras la redirección.
     * @return Una cadena de redirección a la lista de usuarios.
     */
    @PostMapping("/update/{id}")
    public String updateUserByAdmin(@PathVariable("id") Long userId,
                                    @ModelAttribute("user") User user,
                                    @AuthenticationPrincipal UserPrincipal adminPrincipal,
                                    RedirectAttributes redirectAttributes) {
        try {
            userService.updateUserByAdmin(userId, user, adminPrincipal.getUsername());
            redirectAttributes.addFlashAttribute("successMessage", "Usuario actualizado correctamente.");
        } catch (Exception e) {
            log.error("Error al actualizar usuario ID {}: {}", userId, e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/ui/admin/usuarios";
    }

    /**
     * Procesa la solicitud de eliminación de un usuario por parte de un administrador.
     *
     * @param userId El ID del usuario a eliminar.
     * @param adminPrincipal El principal del administrador que realiza la operación.
     * @param redirectAttributes Utilizado para pasar mensajes flash tras la redirección.
     * @return Una cadena de redirección a la lista de usuarios.
     */
    @GetMapping("/delete/{id}")
    public String deleteUserByAdmin(@PathVariable("id") Long userId,
                                    @AuthenticationPrincipal UserPrincipal adminPrincipal,
                                    RedirectAttributes redirectAttributes) {
        try {
            userService.deleteUserByAdmin(userId, adminPrincipal.getUsername());
            redirectAttributes.addFlashAttribute("successMessage", "Usuario eliminado correctamente.");
        } catch (Exception e) {
            log.error("Error al eliminar usuario ID {}: {}", userId, e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/ui/admin/usuarios";
    }
}