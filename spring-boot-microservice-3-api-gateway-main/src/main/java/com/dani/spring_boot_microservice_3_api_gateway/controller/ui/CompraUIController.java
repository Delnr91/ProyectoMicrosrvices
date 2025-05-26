package com.dani.spring_boot_microservice_3_api_gateway.controller.ui;

import com.dani.spring_boot_microservice_3_api_gateway.dto.CompraDto;
import com.dani.spring_boot_microservice_3_api_gateway.model.User;
import com.dani.spring_boot_microservice_3_api_gateway.request.CompraServiceRequest;
import com.dani.spring_boot_microservice_3_api_gateway.security.UserPrincipal;
import com.dani.spring_boot_microservice_3_api_gateway.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Collections;
import java.util.List;

@Controller
@RequestMapping("/ui/mis-compras") // Define la ruta base para este controlador
@RequiredArgsConstructor
@Slf4j
public class CompraUIController {

    private final CompraServiceRequest compraServiceRequest;
    private final UserService userService; // Para obtener datos del usuario para el header

    // Método auxiliar para añadir datos comunes al modelo (ej. usuario para el header)
    private void addUserToModel(UserPrincipal principal, Model model) {
        if (principal != null) {
            // Busca el usuario para obtener su nombre y otros detalles si son necesarios para la vista.
            User currentUser = userService.findByUserameReturnToken(principal.getUsername());
            model.addAttribute("currentUser", currentUser);
        }
    }

    @GetMapping
    public String mostrarMisCompras(Model model, @AuthenticationPrincipal UserPrincipal principal) {
        addUserToModel(principal, model); // Añade información del usuario actual al modelo para el header

        if (principal == null || principal.getId() == null) {
            log.warn("Intento de acceso a 'Mis Compras' sin usuario autenticado o sin ID.");
            // Redirige a la página de login si no hay usuario o ID.
            return "redirect:/login";
        }

        log.info("Usuario ID: {} accediendo a la página 'Mis Compras'", principal.getId());
        List<CompraDto> misCompras;
        try {
            // Llama al servicio de compras a través de Feign para obtener las compras del usuario.
            misCompras = compraServiceRequest.getAllComprasOfUser(principal.getId());
            model.addAttribute("compras", misCompras); // Añade la lista de compras al modelo.
            log.debug("Compras cargadas para usuario ID {}: {}", principal.getId(), misCompras.size());
        } catch (Exception e) {
            log.error("Error al cargar las compras para el usuario ID {}: {}", principal.getId(), e.getMessage(), e);
            model.addAttribute("errorAlCargarCompras", "No se pudieron cargar tus compras en este momento. Intenta más tarde.");
            model.addAttribute("compras", Collections.emptyList()); // Provee una lista vacía en caso de error.
        }

        model.addAttribute("pageTitle", "Mis Compras Realizadas"); // Título para la página.
        model.addAttribute("activePage", "compras"); // Para marcar el enlace activo en el sidebar.
        return "compras/mis-compras"; // Nombre de la plantilla Thymeleaf a renderizar.
    }
}