package com.dani.spring_boot_microservice_3_api_gateway.controller.ui;

import com.dani.spring_boot_microservice_3_api_gateway.model.Role;
import com.dani.spring_boot_microservice_3_api_gateway.model.User;
import com.dani.spring_boot_microservice_3_api_gateway.security.UserPrincipal;
import com.dani.spring_boot_microservice_3_api_gateway.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/ui/admin/usuarios") // Ruta base para la gestión de usuarios por admin
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')") // Toda esta sección es solo para administradores
@Slf4j
public class AdminUserUIController {

    private final UserService userService;

    @Value("${app.security.principal-admin-username}")
    private String PRINCIPAL_ADMIN_USERNAME; // Para no mostrar al admin principal en la lista editable

    // Método auxiliar para añadir datos comunes al modelo
    private void addUserToModel(UserPrincipal principal, Model model) {
        if (principal != null) {
            User currentUser = userService.findByUserameReturnToken(principal.getUsername());
            model.addAttribute("currentUser", currentUser);
        }
    }

    @GetMapping
    public String listarUsuarios(Model model, @AuthenticationPrincipal UserPrincipal principal) {
        addUserToModel(principal, model);
        log.info("Admin {} accediendo a la lista de gestión de usuarios.", principal.getUsername());

        List<User> todosLosUsuarios = userService.findAllUsers();
        // Filtrar para no mostrar el admin principal en la lista que se puede gestionar directamente,
        // o al menos marcarlo de forma especial si se muestra.
        // La lógica de no poder editar/eliminar al admin principal está en el servicio.
        List<User> usuariosGestionables = todosLosUsuarios.stream()
                //.filter(user -> !user.getUsername().equals(PRINCIPAL_ADMIN_USERNAME)) // Opcional: quitarlo de la lista totalmente
                .collect(Collectors.toList());

        model.addAttribute("usuarios", usuariosGestionables);
        model.addAttribute("principalAdminUsername", PRINCIPAL_ADMIN_USERNAME); // Para la plantilla
        model.addAttribute("pageTitle", "Gestionar Usuarios");
        model.addAttribute("activePage", "usuarios"); // Para el sidebar
        return "admin/usuarios/lista-usuarios"; // Nueva plantilla Thymeleaf
    }

    @GetMapping("/editar/{userId}")
    public String mostrarFormularioEditarUsuario(@PathVariable Long userId, Model model,
                                                 @AuthenticationPrincipal UserPrincipal principal,
                                                 RedirectAttributes redirectAttributes) {
        addUserToModel(principal, model);
        log.info("Admin {} intentando editar usuario con ID: {}", principal.getUsername(), userId);

        if (userId == null) {
            redirectAttributes.addFlashAttribute("mensajeError", "ID de usuario no puede ser nulo.");
            return "redirect:/ui/admin/usuarios";
        }

        // Regla: No permitir editar al admin principal a través de este formulario
        // (la lógica fuerte está en el servicio, pero es bueno tener una redirección aquí)
        Optional<User> userParaVerificar = userService.findUserById(userId);
        if (userParaVerificar.isPresent() && userParaVerificar.get().getUsername().equals(PRINCIPAL_ADMIN_USERNAME)) {
            // Si el admin principal intenta editarse a sí mismo a través de esta UI de admin,
            // podríamos permitirlo pero con campos limitados, o redirigir a un perfil de "mi cuenta".
            // Por ahora, si es el admin principal, y el que edita es él mismo, podría ser una página de perfil.
            // Pero si otro admin intenta editar al admin principal, lo prevenimos en el servicio.
            // Para esta UI de gestión, podríamos simplemente decir que no se edita aquí.
            if (!principal.getUsername().equals(PRINCIPAL_ADMIN_USERNAME)) { // Otro admin intentando editar al principal
                redirectAttributes.addFlashAttribute("mensajeError", "El administrador principal no puede ser editado desde esta interfaz por otros administradores.");
                return "redirect:/ui/admin/usuarios";
            }
            // Si es el admin principal editándose a sí mismo, podríamos tener una vista de "Mi Perfil Admin" diferente.
            // Por simplicidad ahora, si es el admin principal, no le dejamos cambiar su ROL aquí.
        }


        User usuarioAEditar = userParaVerificar
                .orElseThrow(() -> new IllegalArgumentException("Usuario inválido con ID: " + userId));

        model.addAttribute("usuarioAEditar", usuarioAEditar);
        model.addAttribute("todosLosRoles", Role.values()); // Para el desplegable de roles
        model.addAttribute("pageTitle", "Editar Usuario: " + usuarioAEditar.getUsername());
        model.addAttribute("activePage", "usuarios");
        model.addAttribute("isPrincipalAdmin", usuarioAEditar.getUsername().equals(PRINCIPAL_ADMIN_USERNAME));

        return "admin/usuarios/form-usuarios"; // Nueva plantilla Thymeleaf
    }

    @PostMapping("/actualizar")
    public String actualizarUsuario(@ModelAttribute("usuarioAEditar") User usuarioForm,
                                    @AuthenticationPrincipal UserPrincipal adminPrincipal,
                                    RedirectAttributes redirectAttributes) {

        log.info("Admin {} intentando actualizar usuario ID: {} con Rol: {} y Nombre: {}",
                adminPrincipal.getUsername(), usuarioForm.getId(), usuarioForm.getRole(), usuarioForm.getNombre());
        try {
            // El UserService tiene la lógica para no modificar al admin principal incorrectamente
            // y para el límite de admins.
            // Pasamos un objeto User solo con los campos que queremos actualizar.
            User updateRequest = new User();
            updateRequest.setNombre(usuarioForm.getNombre());
            updateRequest.setRole(usuarioForm.getRole());
            // No pasamos username ni password para ser actualizados desde este formulario.

            userService.updateUserByAdmin(usuarioForm.getId(), updateRequest, adminPrincipal.getUsername());
            redirectAttributes.addFlashAttribute("mensajeExito", "Usuario '" + usuarioForm.getUsername() + "' actualizado exitosamente.");
        } catch (RuntimeException e) {
            log.error("Error al actualizar usuario ID {}: {}", usuarioForm.getId(), e.getMessage(), e);
            redirectAttributes.addFlashAttribute("mensajeError", "Error al actualizar usuario: " + e.getMessage());
            // Volver al formulario de edición en caso de error podría ser útil
            return "redirect:/ui/admin/usuarios/editar/" + usuarioForm.getId();
        }
        return "redirect:/ui/admin/usuarios";
    }

    @GetMapping("/eliminar/{userId}")
    public String eliminarUsuario(@PathVariable Long userId,
                                  @AuthenticationPrincipal UserPrincipal adminPrincipal,
                                  RedirectAttributes redirectAttributes) {
        log.info("Admin {} intentando eliminar usuario ID: {}", adminPrincipal.getUsername(), userId);
        try {
            // El UserService tiene la lógica para no eliminar al admin principal.
            userService.deleteUserByAdmin(userId, adminPrincipal.getUsername());
            redirectAttributes.addFlashAttribute("mensajeExito", "Usuario con ID " + userId + " eliminado exitosamente.");
        } catch (RuntimeException e) {
            log.error("Error al eliminar usuario ID {}: {}", userId, e.getMessage(), e);
            redirectAttributes.addFlashAttribute("mensajeError", "Error al eliminar usuario: " + e.getMessage());
        }
        return "redirect:/ui/admin/usuarios";
    }
}