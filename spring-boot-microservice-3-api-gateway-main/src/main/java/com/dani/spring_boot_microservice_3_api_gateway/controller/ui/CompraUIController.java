package com.dani.spring_boot_microservice_3_api_gateway.controller.ui;

import com.dani.spring_boot_microservice_3_api_gateway.request.CompraServiceRequest;
import com.dani.spring_boot_microservice_3_api_gateway.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Controlador para la interfaz de usuario (UI) de las compras de un usuario.
 *
 * @author Daniel Núñez Rojas (danidev fullstack software)
 * @version 1.0
 * @since 2025-05-22
 */
@Controller
@RequestMapping("/ui/mis-compras")
@RequiredArgsConstructor
@Slf4j
public class CompraUIController {

    private final CompraServiceRequest compraServiceRequest;

    /**
     * Muestra la página con el historial de compras del usuario autenticado.
     *
     * @param model El objeto {@link Model} para pasar la lista de compras a la vista.
     * @param principal El principal del usuario autenticado.
     * @return El nombre de la vista Thymeleaf ({@code "compras/mis-compras"}).
     */
    @GetMapping
    public String misCompras(Model model, @AuthenticationPrincipal UserPrincipal principal) {
        try {
            var compras = compraServiceRequest.getAllComprasOfUser(principal.getId());
            model.addAttribute("compras", compras);
            log.info("Mostrando {} compras para el usuario ID {}", compras.size(), principal.getId());
        } catch (Exception e) {
            log.error("Error al obtener las compras para el usuario ID {}: {}", principal.getId(), e.getMessage());
            model.addAttribute("error", "No se pudieron cargar tus compras.");
        }
        return "compras/mis-compras";
    }
}