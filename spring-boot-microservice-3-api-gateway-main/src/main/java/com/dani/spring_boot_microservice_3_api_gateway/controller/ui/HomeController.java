package com.dani.spring_boot_microservice_3_api_gateway.controller.ui;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controlador para la página de inicio pública de la aplicación.
 *
 * @author Daniel Núñez Rojas (danidev fullstack software)
 * @version 1.0
 * @since 2025-05-18
 */
@Controller
public class HomeController {

    /**
     * Mapea la raíz del sitio ("/") a la vista de inicio.
     *
     * @return El nombre de la vista Thymeleaf ({@code "index"}).
     */
    @GetMapping("/")
    public String home() {
        return "index";
    }
}