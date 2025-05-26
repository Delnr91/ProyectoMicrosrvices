package com.dani.spring_boot_microservice_3_api_gateway.controller.ui;

import com.dani.spring_boot_microservice_3_api_gateway.dto.InmuebleDto;
import com.dani.spring_boot_microservice_3_api_gateway.model.User;
import com.dani.spring_boot_microservice_3_api_gateway.request.InmuebleServiceRequest;
import com.dani.spring_boot_microservice_3_api_gateway.security.UserPrincipal;
import com.dani.spring_boot_microservice_3_api_gateway.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable; // Asegúrate de que este import esté
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes; // Para mensajes si no se encuentra

import java.util.Collections;
import java.util.List;

@Controller
@RequestMapping("/ui/catalogo")
@RequiredArgsConstructor
@Slf4j
public class CatalogoUIController {

    private final InmuebleServiceRequest inmuebleServiceRequest;
    private final UserService userService;

    private void addUserToModel(UserPrincipal principal, Model model) {
        if (principal != null) {
            User currentUser = userService.findByUserameReturnToken(principal.getUsername());
            model.addAttribute("currentUser", currentUser);
        }
    }

    @GetMapping
    public String mostrarCatalogo(Model model, @AuthenticationPrincipal UserPrincipal principal) {
        if (principal != null) {
            addUserToModel(principal, model);
        }
        log.info("Accediendo a la página del catálogo de inmuebles.");
        try {
            List<InmuebleDto> todosLosInmuebles = inmuebleServiceRequest.getAllInmuebles();
            model.addAttribute("inmuebles", todosLosInmuebles);
        } catch (Exception e) {
            log.error("Error al cargar todos los inmuebles para el catálogo: {}", e.getMessage(), e);
            model.addAttribute("errorAlCargarInmuebles", "No se pudieron cargar los inmuebles en este momento.");
            model.addAttribute("inmuebles", Collections.emptyList());
        }
        model.addAttribute("pageTitle", "Catálogo de Inmuebles");
        model.addAttribute("activePage", "catalogo");
        return "catalogo/vista-catalogo";
    }

    // NUEVO MÉTODO PARA VER DETALLES
    @GetMapping("/detalle/{inmuebleId}")
    public String mostrarDetalleInmueble(@PathVariable Long inmuebleId, Model model,
                                         @AuthenticationPrincipal UserPrincipal principal,
                                         RedirectAttributes redirectAttributes) {
        if (principal != null) {
            addUserToModel(principal, model); // Para el header
        }
        log.info("Accediendo a la página de detalle del inmueble ID: {}", inmuebleId);

        try {
            InmuebleDto inmueble = inmuebleServiceRequest.getInmuebleById(inmuebleId);

            if (inmueble == null) {
                log.warn("Inmueble con ID {} no encontrado (null devuelto)", inmuebleId);
                redirectAttributes.addFlashAttribute("mensajeErrorCatalogo", "Inmueble con ID " + inmuebleId + " no encontrado.");
                return "redirect:/ui/catalogo";
            }

            model.addAttribute("inmueble", inmueble);
            model.addAttribute("pageTitle", "Detalle: " + (inmueble.name() != null ? inmueble.name() : "Inmueble"));
            model.addAttribute("activePage", "catalogo");
            return "catalogo/detalle-inmueble";

        } catch (feign.FeignException.NotFound e) {
            log.warn("Inmueble no encontrado (Feign 404) con ID: {} al intentar ver detalles: {}", inmuebleId, e.getMessage());
            redirectAttributes.addFlashAttribute("mensajeErrorCatalogo", "Inmueble con ID " + inmuebleId + " no encontrado.");
            return "redirect:/ui/catalogo";
        } catch (Exception e) {
            log.error("Error al cargar detalle del inmueble ID {}: {}", inmuebleId, e.getMessage(), e);
            redirectAttributes.addFlashAttribute("mensajeErrorCatalogo", "Error al cargar los detalles del inmueble. Intente más tarde.");
            return "redirect:/ui/catalogo";
        }
    }
}