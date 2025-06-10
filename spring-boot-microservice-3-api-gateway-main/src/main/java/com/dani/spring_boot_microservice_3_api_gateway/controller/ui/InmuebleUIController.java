package com.dani.spring_boot_microservice_3_api_gateway.controller.ui;

import com.dani.spring_boot_microservice_3_api_gateway.dto.InmuebleDto;
import com.dani.spring_boot_microservice_3_api_gateway.model.Role;
import com.dani.spring_boot_microservice_3_api_gateway.model.User;
import com.dani.spring_boot_microservice_3_api_gateway.request.InmuebleServiceRequest;
import com.dani.spring_boot_microservice_3_api_gateway.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;



/**
 * Controlador para la interfaz de usuario (UI) relacionada con la gestión de inmuebles.
 * Maneja las peticiones web para mostrar y procesar las vistas de Thymeleaf para
 * listar, crear, editar y eliminar inmuebles.
 *
 * @author Daniel Núñez Rojas (danidev fullstack software)
 * @version 1.0
 * @since 2025-05-18 (Fecha de creación o última modificación significativa)
 */
@Controller
@RequestMapping("/ui/inmuebles")
@RequiredArgsConstructor
@Slf4j
public class InmuebleUIController {

    private final InmuebleServiceRequest inmuebleServiceRequest;

    /**
     * Muestra la página principal de gestión de inmuebles.
     * La vista que se muestra y los datos que se cargan dependen del rol del usuario autenticado.
     * <ul>
     * <li><b>ADMIN:</b> Muestra una lista de todos los inmuebles en el sistema.</li>
     * <li><b>USER:</b> Muestra una lista de solo los inmuebles creados por ese usuario.</li>
     * </ul>
     *
     * @param model El objeto {@link Model} para pasar atributos a la vista Thymeleaf.
     * @param principal El objeto {@link UserPrincipal} que representa al usuario autenticado,
     * inyectado por Spring Security.
     * @return El nombre de la vista Thymeleaf a renderizar ({@code "inmuebles/lista-inmuebles"}).
     */
    @GetMapping
    public String showInmueblesPage(Model model, @AuthenticationPrincipal UserPrincipal principal) {
        User user = principal.getUser();
        boolean isAdmin = user.getRole() == Role.ADMIN;
        model.addAttribute("isAdmin", isAdmin);

        try {
            if (isAdmin) {
                log.info("Admin {} solicitando lista de todos los inmuebles.", principal.getUsername());
                model.addAttribute("inmuebles", inmuebleServiceRequest.getAllInmuebles());
            } else {
                log.info("Usuario {} solicitando lista de sus inmuebles.", principal.getUsername());
                model.addAttribute("inmuebles", inmuebleServiceRequest.getAllInmueblesByUserId(user.getId()));
            }
        } catch (Exception e) {
            log.error("Error al obtener la lista de inmuebles para el usuario {}: {}", principal.getUsername(), e.getMessage());
            model.addAttribute("error", "No se pudieron cargar los inmuebles.");
        }

        return "inmuebles/lista-inmuebles";
    }

    /**
     * Muestra el formulario para crear un nuevo inmueble.
     *
     * @param model El objeto {@link Model} para pasar un nuevo {@link InmuebleDto} vacío al formulario.
     * @return El nombre de la vista Thymeleaf del formulario ({@code "inmuebles/form-inmueble"}).
     */
    @GetMapping("/add")
    public String showFormInmueble(Model model) {
        model.addAttribute("inmuebleDto", new InmuebleDto(null, null, null, null, null, 0.0, null, null));
        model.addAttribute("isEditMode", false);
        return "inmuebles/form-inmueble";
    }

    /**
     * Procesa el envío del formulario para guardar o actualizar un inmueble.
     *
     * @param inmuebleDto El objeto {@link InmuebleDto} con los datos del formulario,
     * mapeado con {@link ModelAttribute}.
     * @param principal El {@link UserPrincipal} del usuario autenticado.
     * @param redirectAttributes Utilizado para pasar mensajes (de éxito o error) a la vista
     * después de la redirección.
     * @return Una cadena de redirección a la página principal de gestión de inmuebles ({@code "redirect:/ui/inmuebles"}).
     */
    @PostMapping("/save")
    public String saveOrUpdateInmueble(@ModelAttribute InmuebleDto inmuebleDto,
                                       @AuthenticationPrincipal UserPrincipal principal,
                                       RedirectAttributes redirectAttributes) {
        log.info("Intento de guardar/actualizar inmueble: {}", inmuebleDto.name());
        try {
            inmuebleServiceRequest.saveInmueble(inmuebleDto);
            String message = (inmuebleDto.id() == null) ? "Inmueble guardado exitosamente." : "Inmueble actualizado exitosamente.";
            redirectAttributes.addFlashAttribute("successMessage", message);
        } catch (Exception e) {
            log.error("Error al guardar el inmueble '{}': {}", inmuebleDto.name(), e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "Error al guardar el inmueble.");
        }
        return "redirect:/ui/inmuebles";
    }

    /**
     * Muestra el formulario para editar un inmueble existente.
     *
     * @param inmuebleId El ID del inmueble a editar, obtenido de la URL con {@link PathVariable}.
     * @param model El objeto {@link Model} para pasar los datos del inmueble al formulario.
     * @return El nombre de la vista Thymeleaf del formulario ({@code "inmuebles/form-inmueble"}) o
     * una redirección si el inmueble no se encuentra.
     */
    @GetMapping("/edit/{id}")
    public String editInmueble(@PathVariable("id") Long inmuebleId, Model model) {
        log.info("Solicitando edición para inmueble ID: {}", inmuebleId);
        try {
            InmuebleDto inmuebleDto = inmuebleServiceRequest.getInmuebleById(inmuebleId);
            model.addAttribute("inmuebleDto", inmuebleDto);
            model.addAttribute("isEditMode", true);
            return "inmuebles/form-inmueble";
        } catch (Exception e) {
            log.error("No se pudo encontrar el inmueble con ID {} para editar.", inmuebleId);
            return "redirect:/ui/inmuebles";
        }
    }

    /**
     * Procesa la solicitud para eliminar un inmueble.
     *
     * @param inmuebleId El ID del inmueble a eliminar, obtenido de la URL con {@link PathVariable}.
     * @param redirectAttributes Utilizado para pasar mensajes de éxito o error.
     * @return Una cadena de redirección a la página principal de gestión de inmuebles ({@code "redirect:/ui/inmuebles"}).
     */
    @GetMapping("/delete/{id}")
    public String deleteInmueble(@PathVariable("id") Long inmuebleId, RedirectAttributes redirectAttributes) {
        log.warn("Intento de eliminar inmueble ID: {}", inmuebleId);
        try {
            inmuebleServiceRequest.deleteInmueble(inmuebleId);
            redirectAttributes.addFlashAttribute("successMessage", "Inmueble eliminado exitosamente.");
        } catch (Exception e) {
            log.error("Error al eliminar el inmueble con ID {}: {}", inmuebleId, e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "Error al eliminar el inmueble.");
        }
        return "redirect:/ui/inmuebles";
    }
}