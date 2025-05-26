package com.dani.spring_boot_microservice_3_api_gateway.controller.ui;

import com.dani.spring_boot_microservice_3_api_gateway.model.Role;
import com.dani.spring_boot_microservice_3_api_gateway.model.User;
import com.dani.spring_boot_microservice_3_api_gateway.request.InmuebleServiceRequest;
import com.dani.spring_boot_microservice_3_api_gateway.security.UserPrincipal;
import com.dani.spring_boot_microservice_3_api_gateway.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
// Para lista vacía en caso de error
import java.util.Collections;


/**
 * Controlador MVC para gestionar la vista principal del Dashboard y otras páginas de UI generales
 * que no pertenecen a un módulo específico como Inmuebles o Usuarios.
 *
 * @author Daniel Núñez Rojas (danidev fullstack software)
 * @version 1.1
 * @since 2025-05-13 (Refactorizado para separar lógica de Inmuebles)
 */
@Controller
@RequestMapping("/ui") // Sigue siendo el prefijo general para la UI
@RequiredArgsConstructor
@Slf4j
public class DashboardController {

    private final UserService userService; // Necesario para obtener datos del usuario para el saludo
    private final InmuebleServiceRequest inmuebleServiceRequest; // Inyectar el cliente Feign de Inmueble
    // private final CompraServiceRequest compraServiceRequest; // Descomentar si se añaden stats de compras
    /**
     * Maneja las peticiones GET a "/ui/dashboard" para mostrar la página principal del panel de control.
     *
     * @param model     El objeto Model para pasar datos a la vista (ej. usuario actual, página activa).
     * @param principal El UserPrincipal del usuario autenticado (inyectado por Spring Security).
     * @return El nombre de la plantilla Thymeleaf "dashboard" a renderizar.
     */
    @GetMapping("/dashboard")
    public String dashboardPage(Model model, @AuthenticationPrincipal UserPrincipal principal) {
        log.info("Accediendo a la página principal del dashboard.");
        if (principal != null) {
            User currentUser = userService.findByUserameReturnToken(principal.getUsername());
            model.addAttribute("currentUser", currentUser);
            log.debug("Usuario actual para el dashboard: {}", currentUser.getUsername());

            // Verificar si el usuario es ADMIN para cargar estadísticas
            boolean isAdmin = principal.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .anyMatch("ROLE_ADMIN"::equals);

            if (isAdmin) {
                log.debug("Usuario {} es ADMIN, cargando estadísticas.", principal.getUsername());
                try {
                    // Obtener total de inmuebles
                    int totalInmuebles = inmuebleServiceRequest.getAllInmuebles().size();
                    model.addAttribute("totalInmuebles", totalInmuebles);
                    log.debug("Total inmuebles: {}", totalInmuebles);
                } catch (Exception e) {
                    log.error("Error al obtener total de inmuebles para el dashboard: {}", e.getMessage());
                    model.addAttribute("errorEstadisticasInmuebles", "No se pudo cargar el total de inmuebles.");
                    model.addAttribute("totalInmuebles", "N/A"); // O algún valor indicativo de error
                }

                try {
                    int totalUsuarios = userService.findAllUsers().size();
                    model.addAttribute("totalUsuarios", totalUsuarios);
                    log.debug("Total usuarios: {}", totalUsuarios);
                } catch (Exception e) {
                    log.error("Error al obtener total de usuarios para el dashboard admin: {}", e.getMessage());
                    model.addAttribute("errorEstadisticasUsuarios", "No se pudo cargar el total de usuarios.");
                    model.addAttribute("totalUsuarios", "N/A");
                }
            } else if (currentUser.getRole() == Role.USER) { // Es USER y no ADMIN
                log.debug("Usuario {} es USER, cargando estadísticas de usuario.", principal.getUsername());
                try {
                    // Conteo de inmuebles publicados por el usuario
                    int misInmueblesPublicados = inmuebleServiceRequest.getAllInmueblesByUserId(principal.getId()).size(); //
                    model.addAttribute("misInmueblesPublicados", misInmueblesPublicados);
                    log.debug("Inmuebles publicados por {}: {}", principal.getUsername(), misInmueblesPublicados);
                } catch (Exception e) {
                    log.error("Error al obtener conteo de inmuebles para el usuario {} en dashboard: {}", principal.getUsername(), e.getMessage());
                    model.addAttribute("errorMisInmueblesPublicados", "No se pudo cargar el conteo de tus inmuebles.");
                    model.addAttribute("misInmueblesPublicados", "N/A");
                }
            }
        }
        model.addAttribute("activePage", "dashboard");
        model.addAttribute("pageTitle", "Dashboard Principal");
        return "dashboard"; //
    }

    // Todos los métodos relacionados con "/ui/inmuebles/..." han sido movidos a InmuebleUIController.java
}