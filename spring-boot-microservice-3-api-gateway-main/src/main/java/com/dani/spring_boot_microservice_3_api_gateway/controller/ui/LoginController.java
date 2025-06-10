package com.dani.spring_boot_microservice_3_api_gateway.controller.ui;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controlador para mostrar la página de inicio de sesión personalizada.
 *
 * @author Daniel Núñez Rojas (danidev fullstack software)
 * @version 1.0
 * @since 2025-05-18
 */
@Controller
public class LoginController {

    /**
     * Mapea la ruta "/login" para mostrar el formulario de inicio de sesión.
     * Esta es la página que Spring Security mostrará cuando se requiera autenticación.
     *
     * @return El nombre de la vista Thymeleaf ({@code "login"}).
     */
    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }
}