package com.dani.spring_boot_microservice_3_api_gateway.controller.ui;

import com.dani.spring_boot_microservice_3_api_gateway.model.Role;
import com.dani.spring_boot_microservice_3_api_gateway.request.InmuebleServiceRequest;
import com.dani.spring_boot_microservice_3_api_gateway.security.UserPrincipal;
import com.dani.spring_boot_microservice_3_api_gateway.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Controlador para la interfaz de usuario (UI) del Dashboard o panel de control.
 * <p>
 * Muestra una vista principal con información y estadísticas relevantes para el usuario
 * autenticado. La información que se presenta varía según el rol del usuario (ADMIN o USER).
 *
 * @author Daniel Núñez Rojas (danidev fullstack software)
 * @version 1.0
 * @since 2025-05-23
 */
@Controller
@RequestMapping("/ui/dashboard")
@RequiredArgsConstructor
@Slf4j
public class DashboardController {

    private final InmuebleServiceRequest inmuebleServiceRequest;
    private final UserService userService;

    /**
     * Prepara y muestra la página del dashboard.
     * <p>
     * Carga estadísticas diferentes en el modelo dependiendo de si el usuario
     * es un administrador o un usuario estándar.
     *
     * @param model El objeto {@link Model} para pasar datos a la vista.
     * @param principal El principal del usuario autenticado.
     * @return El nombre de la vista Thymeleaf ({@code "dashboard"}).
     */
    @GetMapping
    public String dashboard(Model model, @AuthenticationPrincipal UserPrincipal principal) {
        log.info("Usuario {} ({}) accediendo al dashboard.", principal.getUsername(), principal.getUser().getRole());
        model.addAttribute("user", principal.getUser());

        if (principal.getUser().getRole() == Role.ADMIN) {
            // Cargar datos para el dashboard de Administrador
            try {
                model.addAttribute("totalInmuebles", inmuebleServiceRequest.getAllInmuebles().size());
            } catch (Exception e) {
                log.error("Error al obtener el conteo total de inmuebles: {}", e.getMessage());
                model.addAttribute("totalInmuebles", "N/A");
            }
            try {
                model.addAttribute("totalUsuarios", userService.findAllUsers().size());
            } catch (Exception e) {
                log.error("Error al obtener el conteo total de usuarios: {}", e.getMessage());
                model.addAttribute("totalUsuarios", "N/A");
            }
        }

        // Cargar datos para las estadísticas personales del usuario (sea admin o no)
        try {
            model.addAttribute("misInmuebles", inmuebleServiceRequest.getAllInmueblesByUserId(principal.getId()).size());
        } catch (Exception e) {
            log.error("Error al obtener el conteo de inmuebles para el usuario {}: {}", principal.getUsername(), e.getMessage());
            model.addAttribute("misInmuebles", "N/A");
        }

        return "dashboard";
    }
}