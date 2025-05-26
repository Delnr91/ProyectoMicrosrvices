package com.dani.spring_boot_microservice_3_api_gateway.controller.ui;

import com.dani.spring_boot_microservice_3_api_gateway.dto.InmuebleDto;
import com.dani.spring_boot_microservice_3_api_gateway.request.InmuebleServiceRequest;
import com.dani.spring_boot_microservice_3_api_gateway.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
@Slf4j
public class HomeController {

    private final InmuebleServiceRequest inmuebleServiceRequest;

    @GetMapping({"/", "/index", "/home"})
    public String homePage(Model model, @AuthenticationPrincipal UserPrincipal principal) {
        // Si el usuario ya está autenticado, podríamos querer pasar su nombre al header del index.
        // Opcional: Podrías redirigir al dashboard si ya está autenticado.
        // if (principal != null) {
        // return "redirect:/ui/dashboard";
        // }
        // Por ahora, simplemente mostraremos el index.html y el header se adaptará.

        log.info("Accediendo a la página de inicio.");
        model.addAttribute("pageTitle", "Bienvenido al Portal Inmobiliario");
        model.addAttribute("isHomePage", true); // Un flag para el header si necesita comportarse diferente

        // Lógica para cargar inmuebles destacados (Paso 4 de esta fase)
         /*
        try {
            List<InmuebleDto> todosLosInmuebles = inmuebleServiceRequest.getAllInmuebles();
            List<InmuebleDto> inmueblesDestacados = todosLosInmuebles.stream()
                    .filter(inm -> "DISPONIBLE".equalsIgnoreCase(inm.estado())) // Solo disponibles
                    .limit(6) // Mostrar hasta 6 destacados
                    .collect(Collectors.toList());
            model.addAttribute("inmueblesDestacados", inmueblesDestacados);
        } catch (Exception e) {
            log.error("Error al cargar inmuebles destacados para la página de inicio: {}", e.getMessage());
            model.addAttribute("inmueblesDestacados", Collections.emptyList());
            model.addAttribute("errorCargaDestacados", "No se pudieron cargar los inmuebles destacados.");
        }
        */

        return "index"; // Servirá /templates/index.html
    }
}