package com.dani.spring_boot_microservice_3_api_gateway.controller.ui;

import com.dani.spring_boot_microservice_3_api_gateway.model.User;
import com.dani.spring_boot_microservice_3_api_gateway.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException; // Importante para capturar error de username duplicado
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult; // Para validación futura con @Valid
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

// Si usas validación con @Valid, necesitarás:
// import jakarta.validation.Valid;

/**
 * Controlador MVC para gestionar la interfaz de usuario (UI) relacionada con la autenticación,
 * como el login y el registro de usuarios.
 *
 * @author Daniel Núñez Rojas (danidev fullstack software)
 * @version 1.0
 * @since 2025-05-13
 */
@Controller
@RequestMapping("/ui/authentication") // Prefijo para las rutas de UI de autenticación
@RequiredArgsConstructor
@Slf4j
public class AuthenticationUIController {

    private final UserService userService;

    /**
     * Muestra la página de login.
     * La ruta "/login" principal es manejada por Spring Security para la redirección,
     * pero podemos tener un mapeo explícito si quisiéramos añadir lógica aquí.
     * Por ahora, Spring Security maneja el GET a /login.
     * Este controlador se enfoca en el registro y podría extenderse a recuperación de contraseña, etc.
     */
    // El GET a /login es manejado por la configuración de Spring Security (.loginPage("/login"))
    // y el LoginController que ya tienes o que podríamos fusionar aquí.
    // Por simplicidad, dejaremos que Spring Security y un LoginController simple manejen el GET /login.

    /**
     * Muestra la página de registro de nuevos usuarios.
     *
     * @param model El modelo para pasar datos a la vista (un objeto User vacío).
     * @return El nombre de la plantilla Thymeleaf "sign-up".
     */
    @GetMapping("/sign-up") //  Esta ruta será enlazada desde login.html
    public String mostrarPaginaDeRegistro(Model model) {
        log.info("Accediendo a la página de UI para registro de nuevo usuario.");
        if (!model.containsAttribute("user")) { // Si no viene un usuario con errores de un intento anterior
            model.addAttribute("user", new User()); // Provee un objeto User vacío para el th:object
        }
        model.addAttribute("pageTitle", "Registrarse"); // Para el <title> de la página
        return "sign-up"; // Devuelve /templates/sign-up.html
    }

    /**
     * Procesa la petición de registro de un nuevo usuario enviada desde el formulario de la UI.
     * Intenta guardar el usuario usando {@link UserService}.
     *
     * @param user               El objeto {@link User} con los datos del formulario. Podría anotarse con @Valid para validaciones.
     * @param bindingResult      Resultados de la validación (si se usa @Valid).
     * @param redirectAttributes Para pasar mensajes flash a la página de redirección (login).
     * @param model              El modelo, para devolver el usuario y mensajes de error al formulario si falla el registro.
     * @return Redirección a la página de login si el registro es exitoso,
     * o de vuelta al formulario de registro si hay errores.
     */
    @PostMapping("/perform-sign-up") // Esta es la URL en el th:action del formulario de sign-up.html
    public String procesarRegistroUsuario(
            @ModelAttribute("user") User user, // Aquí podrías añadir @Valid antes de User user
            BindingResult bindingResult,      // Y aquí BindingResult bindingResult si usas @Valid
            RedirectAttributes redirectAttributes,
            Model model) {

        log.info("Procesando intento de registro para el usuario: {}", user.getUsername());

        // Aquí iría la validación si usas @Valid y BindingResult
        // if (bindingResult.hasErrors()) {
        //     log.warn("Errores de validación al registrar usuario '{}': {}", user.getUsername(), bindingResult.getAllErrors());
        //     model.addAttribute("user", user); // Devolver el usuario con errores para que el formulario los muestre
        //     model.addAttribute("pageTitle", "Registrarse");
        //     // No es necesario añadir "errorMessage" aquí si los errores de campo se muestran individualmente
        //     return "sign-up"; // Volver a la página de registro
        // }

        // Lógica para verificar si la contraseña y la confirmación de contraseña coinciden (si tuvieras ese campo)
        // if (user.getPassword() != null && !user.getPassword().equals(user.getConfirmPassword())) {
        //     log.warn("Las contraseñas no coinciden para el usuario: {}", user.getUsername());
        //     model.addAttribute("user", user);
        //     model.addAttribute("errorMessage", "Las contraseñas no coinciden.");
        //     model.addAttribute("pageTitle", "Registrarse");
        //     return "sign-up";
        // }

        try {
            userService.saveUser(user); // El método saveUser ya encripta la contraseña y asigna ROLE_USER
            log.info("Usuario '{}' registrado exitosamente desde la UI.", user.getUsername());
            redirectAttributes.addFlashAttribute("registroExitoso",
                    "¡Registro exitoso! Por favor, inicia sesión con tu nueva cuenta.");
            return "redirect:/login"; // Redirigir a la página de login
        } catch (DataIntegrityViolationException e) {
            // Esta excepción es común si el username ya existe y hay una restricción UNIQUE en la BD.
            log.warn("Error de integridad de datos al registrar usuario '{}' (probablemente username duplicado): {}", user.getUsername(), e.getMessage());
            model.addAttribute("user", user); // Devolver el usuario para que no pierda los datos ingresados
            model.addAttribute("errorMessage", "El nombre de usuario '" + user.getUsername() + "' ya está en uso. Por favor, elige otro.");
            model.addAttribute("pageTitle", "Registrarse");
            return "sign-up"; // Volver a la página de registro
        } catch (Exception e) {
            log.error("Error inesperado durante el registro del usuario '{}': {}", user.getUsername(), e.getMessage(), e);
            model.addAttribute("user", user);
            model.addAttribute("errorMessage", "Ocurrió un error inesperado durante el registro. Por favor, inténtalo de nuevo más tarde.");
            model.addAttribute("pageTitle", "Registrarse");
            return "sign-up";
        }
    }
}