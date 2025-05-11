package com.dani.spring_boot_microservice_3_api_gateway.controller.ui;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LoginController {

    @GetMapping("/login")
    public String loginPage() {
        return "login"; // Devuelve el nombre de la plantilla Thymeleaf: login.html
    }

    // Opcional: Crear un endpoint para una futura página de registro si quieres una UI para ello.
    @GetMapping("/api/authentication/sign-up-page")
    public String signUpPage() {
        // return "sign-up"; // Suponiendo que tienes un sign-up.html
        // Por ahora, podemos redirigir o simplemente no implementarlo si el registro es solo vía API
        return "redirect:/login"; // O mostrar un mensaje de "Registro no disponible vía UI aún"
    }
}