package com.dani.spring_boot_microservice_3_api_gateway.controller.ui;

import com.dani.spring_boot_microservice_3_api_gateway.model.User;
import com.dani.spring_boot_microservice_3_api_gateway.service.AuthenticationService;
import com.dani.spring_boot_microservice_3_api_gateway.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controlador para la interfaz de usuario (UI) relacionada con la autenticación.
 * Maneja las vistas de Thymeleaf para el registro de nuevos usuarios.
 *
 * @author Daniel Núñez Rojas (danidev fullstack software)
 * @version 1.0
 * @since 2025-05-23
 */
@Controller
@RequestMapping("/ui/authentication")
@RequiredArgsConstructor
@Slf4j
public class AuthenticationUIController {

    private final UserService userService;
    private final AuthenticationService authenticationService;

    /**
     * Muestra la página/formulario de registro (sign-up).
     *
     * @param model El objeto {@link Model} para pasar un nuevo objeto User vacío a la vista.
     * @return El nombre de la vista Thymeleaf ({@code "sign-up"}).
     */
    @GetMapping("/sign-up")
    public String showSignUpPage(Model model) {
        model.addAttribute("user", new User());
        return "sign-up";
    }

    /**
     * Procesa el envío del formulario de registro.
     *
     * @param user El objeto {@link User} con los datos del formulario.
     * @param redirectAttributes Utilizado para pasar mensajes flash tras la redirección.
     * @param model El objeto {@link Model} para devolver datos a la vista en caso de error.
     * @return Una cadena de redirección a la página de login si el registro es exitoso,
     * o devuelve la vista de registro con un mensaje de error si el usuario ya existe.
     */
    @PostMapping("/perform-sign-up")
    public String performSignUp(@ModelAttribute("user") User user, RedirectAttributes redirectAttributes, Model model) {
        if (userService.findByUsername(user.getUsername()).isPresent()) {
            model.addAttribute("errorMessage", "El nombre de usuario ya está en uso. Por favor, elige otro.");
            return "sign-up"; // Devuelve a la misma página con el mensaje de error
        }
        userService.saveUser(user);
        redirectAttributes.addFlashAttribute("successMessage", "¡Registro exitoso! Ahora puedes iniciar sesión.");
        return "redirect:/login";
    }
}