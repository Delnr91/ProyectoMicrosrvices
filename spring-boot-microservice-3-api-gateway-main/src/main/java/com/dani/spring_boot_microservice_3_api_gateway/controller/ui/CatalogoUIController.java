package com.dani.spring_boot_microservice_3_api_gateway.controller.ui;

import com.dani.spring_boot_microservice_3_api_gateway.request.InmuebleServiceRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Controlador para la interfaz de usuario (UI) del catálogo público de inmuebles.
 *
 * @author Daniel Núñez Rojas (danidev fullstack software)
 * @version 1.0
 * @since 2025-05-21
 */
@Controller
@RequestMapping("/ui/catalogo")
@RequiredArgsConstructor
@Slf4j
public class CatalogoUIController {

    private final InmuebleServiceRequest inmuebleServiceRequest;

    /**
     * Muestra la página principal del catálogo con la lista de todos los inmuebles.
     *
     * @param model El objeto {@link Model} para pasar la lista de inmuebles a la vista.
     * @return El nombre de la vista Thymeleaf ({@code "catalogo/vista-catalogo"}).
     */
    @GetMapping
    public String mostrarCatalogo(Model model) {
        try {
            model.addAttribute("inmuebles", inmuebleServiceRequest.getAllInmuebles());
        } catch (Exception e) {
            log.error("Error al cargar el catálogo de inmuebles: {}", e.getMessage());
            model.addAttribute("error", "No se pudo cargar el catálogo de inmuebles en este momento.");
        }
        return "catalogo/vista-catalogo";
    }

    /**
     * Muestra la página de detalle para un inmueble específico.
     *
     * @param inmuebleId El ID del inmueble a mostrar.
     * @param model El objeto {@link Model} para pasar los datos del inmueble a la vista.
     * @return El nombre de la vista Thymeleaf ({@code "catalogo/detalle-inmueble"}).
     */
    @GetMapping("/detalle/{id}")
    public String verDetalleInmueble(@PathVariable("id") Long inmuebleId, Model model) {
        try {
            model.addAttribute("inmueble", inmuebleServiceRequest.getInmuebleById(inmuebleId));
        } catch (Exception e) {
            log.error("Error al obtener detalle del inmueble ID {}: {}", inmuebleId, e.getMessage());
            return "redirect:/ui/catalogo?error=notfound";
        }
        return "catalogo/detalle-inmueble";
    }
}