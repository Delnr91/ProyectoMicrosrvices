package com.dani.spring_boot_microservice_3_api_gateway.controller.ui;

import com.dani.spring_boot_microservice_3_api_gateway.dto.InmuebleDto;
import com.dani.spring_boot_microservice_3_api_gateway.model.User;
import com.dani.spring_boot_microservice_3_api_gateway.request.InmuebleServiceRequest;
import com.dani.spring_boot_microservice_3_api_gateway.security.UserPrincipal;
import com.dani.spring_boot_microservice_3_api_gateway.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Controlador MVC para gestionar la interfaz de usuario (UI) relacionada con las operaciones CRUD de Inmuebles.
 * Todas las rutas de este controlador estarán bajo el prefijo "/ui/inmuebles".
 *
 * @author Daniel Núñez Rojas (danidev fullstack software)
 * @version 1.0
 * @since 2025-05-13
 */
@Controller
@RequestMapping("/ui/inmuebles") // Prefijo para todas las rutas de este controlador
@RequiredArgsConstructor
@Slf4j // Lombok para logging fácil con 'log.info()', 'log.error()', etc.
public class InmuebleUIController {

    private final InmuebleServiceRequest inmuebleServiceRequest;
    private final UserService userService; // Necesario para obtener el currentUser para el layout

    /**
     * Prepara los datos comunes del modelo que se necesitan en todas las vistas gestionadas por este controlador.
     * Actualmente, añade el usuario autenticado al modelo para el saludo en el header.
     *
     * @param principal El UserPrincipal del usuario autenticado.
     * @param model     El objeto Model para pasar datos a la vista.
     */
    private void addUserToModel(UserPrincipal principal, Model model) {
        if (principal != null) {
            User currentUser = userService.findByUserameReturnToken(principal.getUsername());
            model.addAttribute("currentUser", currentUser);
        }
    }

    /**
     * Maneja las peticiones GET a "/ui/inmuebles" para mostrar la lista de todos los inmuebles.
     * Obtiene los inmuebles a través del cliente Feign {@link InmuebleServiceRequest}.
     *
     * @param model     El objeto Model para pasar datos a la vista.
     * @param principal El UserPrincipal del usuario autenticado.
     * @return El nombre de la plantilla Thymeleaf "inmuebles/lista-inmuebles".
     */
    @GetMapping
    public String listarInmuebles(Model model, @AuthenticationPrincipal UserPrincipal principal) {
        addUserToModel(principal, model);
        log.info("Accediendo a la página de listado de inmuebles por usuario: {}", principal.getUsername());

        List<InmuebleDto> listaInmuebles;
        String pageTitle = "Gestionar Mis Inmuebles"; // Título por defecto para USER

        try {
            // Determinar si el usuario es ADMIN
            boolean isAdmin = principal.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .anyMatch("ROLE_ADMIN"::equals);

            if (isAdmin) {
                log.debug("Usuario {} es ADMIN. Cargando todos los inmuebles.", principal.getUsername());
                listaInmuebles = inmuebleServiceRequest.getAllInmuebles();
                pageTitle = "Gestionar Todos los Inmuebles (Admin)";
            } else { // Es ROLE_USER (o cualquier otro rol no-ADMIN)
                log.debug("Usuario {} es USER. Cargando solo sus inmuebles.", principal.getUsername());
                if (principal.getId() != null) {
                    listaInmuebles = inmuebleServiceRequest.getAllInmueblesByUserId(principal.getId());
                } else {
                    log.warn("UserPrincipal para {} no tiene ID, no se pueden cargar sus inmuebles.", principal.getUsername());
                    listaInmuebles = Collections.emptyList();
                    model.addAttribute("errorAlCargarInmuebles", "No se pudo determinar tu ID de usuario para cargar tus inmuebles.");
                }
            }
            model.addAttribute("inmuebles", listaInmuebles);
            log.debug("Inmuebles cargados para {}: {}", principal.getUsername(), listaInmuebles.size());

        } catch (Exception e) {
            log.error("Error al obtener inmuebles para {}: {}", principal.getUsername(), e.getMessage(), e);
            model.addAttribute("errorAlCargarInmuebles", "No se pudieron cargar los inmuebles en este momento. Intente más tarde.");
            model.addAttribute("inmuebles", Collections.emptyList()); // Lista vacía para evitar errores en Thymeleaf
        }

        model.addAttribute("activePage", "inmuebles"); // Para el sidebar
        model.addAttribute("pageTitle", pageTitle); // Título dinámico
        // Pasamos un flag para la plantilla, indicando si es vista de admin (todos los inmuebles)
        // o vista de user (solo los suyos, donde todos son gestionables por él)
        model.addAttribute("isAdminView", principal.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch("ROLE_ADMIN"::equals));
        return "inmuebles/lista-inmuebles";
    }


    /**
     * Maneja las peticiones GET a "/ui/inmuebles/nuevo" para mostrar el formulario de creación de un nuevo inmueble.
     *
     * @param model     El objeto Model para pasar datos a la vista.
     * @param principal El UserPrincipal del usuario autenticado.
     * @return El nombre de la plantilla Thymeleaf "inmuebles/form-inmueble".
     */
    @GetMapping("/nuevo")
    public String mostrarFormularioNuevoInmueble(Model model, @AuthenticationPrincipal UserPrincipal principal) {
        addUserToModel(principal, model);
        log.info("Accediendo al formulario para añadir un nuevo inmueble.");

        // Se provee un DTO vacío para enlazar con los campos del formulario th:object
        model.addAttribute("inmueble", new InmuebleDto(null, null, "", "", "", 0.0, null, "DISPONIBLE"));
        model.addAttribute("activePage", "inmuebles");
        // Ajustar título si es necesario, o mantenerlo genérico
        boolean isAdmin = principal.getAuthorities().stream().map(GrantedAuthority::getAuthority).anyMatch("ROLE_ADMIN"::equals);
        model.addAttribute("pageTitle", (isAdmin ? "Añadir Nuevo Inmueble (Admin)" : "Publicar Nuevo Inmueble"));
        return "inmuebles/form-inmueble";
    }

    /**
     * Maneja las peticiones GET a "/ui/inmuebles/editar/{idInmueble}" para mostrar el formulario de edición
     * con los datos del inmueble especificado.
     *
     * @param idInmueble El ID del inmueble a editar, obtenido de la ruta.
     * @param model      El objeto Model para pasar datos a la vista.
     * @param principal  El UserPrincipal del usuario autenticado.
     * @param redirectAttributes Atributos para pasar mensajes en caso de redirección.
     * @return El nombre de la plantilla Thymeleaf "inmuebles/form-inmueble" o una redirección si el inmueble no se encuentra.
     */
    @GetMapping("/editar/{idInmueble}")
    public String mostrarFormularioEditarInmueble(@PathVariable Long idInmueble, Model model,
                                                  @AuthenticationPrincipal UserPrincipal principal, RedirectAttributes redirectAttributes) {
        addUserToModel(principal, model);
        log.info("Accediendo al formulario para editar el inmueble con ID: {} por usuario {}", idInmueble, principal.getUsername());

        InmuebleDto inmuebleAEditar;
        try {
            inmuebleAEditar = inmuebleServiceRequest.getInmuebleById(idInmueble);
            if (inmuebleAEditar == null) {
                log.warn("Intento de editar inmueble no encontrado con ID: {}", idInmueble);
                redirectAttributes.addFlashAttribute("mensajeError", "Error: Inmueble con ID " + idInmueble + " no encontrado.");
                return "redirect:/ui/inmuebles";
            }

            // Verificación de permisos antes de mostrar el formulario de edición
            boolean isAdmin = principal.getAuthorities().stream().map(GrantedAuthority::getAuthority).anyMatch("ROLE_ADMIN"::equals);
            // Si no es admin, verificar que sea el propietario
            if (!isAdmin && (inmuebleAEditar.userId() == null || !principal.getId().equals(inmuebleAEditar.userId()))) {
                log.warn("Usuario {} no autorizado para editar inmueble ID {}", principal.getUsername(), idInmueble);
                redirectAttributes.addFlashAttribute("mensajeError", "No tienes permiso para editar este inmueble.");
                return "redirect:/ui/inmuebles"; // O a una página de error específica
            }

            model.addAttribute("inmueble", inmuebleAEditar);
        } catch (feign.FeignException.NotFound e) {
            log.warn("Inmueble no encontrado (Feign 404) al intentar editar: ID {}", idInmueble, e);
            redirectAttributes.addFlashAttribute("mensajeError", "Error: Inmueble con ID " + idInmueble + " no encontrado.");
            return "redirect:/ui/inmuebles";
        } catch (Exception e) {
            log.error("Error al cargar inmueble para editar con ID {}: {}", idInmueble, e.getMessage(), e);
            redirectAttributes.addFlashAttribute("mensajeError", "Error al cargar datos del inmueble para editar. Intente más tarde.");
            return "redirect:/ui/inmuebles";
        }

        model.addAttribute("activePage", "inmuebles");
        model.addAttribute("pageTitle", "Editar Inmueble" + (inmuebleAEditar.name() != null ? ": " + inmuebleAEditar.name() : ""));
        return "inmuebles/form-inmueble";
    }

    /**
     * Maneja las peticiones POST a "/ui/inmuebles/guardar" para crear un nuevo inmueble o actualizar uno existente.
     * Los datos del inmueble se reciben en un {@link InmuebleDto}.
     *
     * @param inmuebleDto      El DTO del inmueble con los datos del formulario.
     * @param redirectAttributes Atributos para pasar mensajes de éxito/error tras la redirección.
     * @return Una cadena de redirección a la lista de inmuebles "/ui/inmuebles".
     */
    @PostMapping("/guardar")
    public String guardarOActualizarInmueble(@ModelAttribute("inmueble") InmuebleDto inmuebleDto,
                                             RedirectAttributes redirectAttributes) {
        String accion = (inmuebleDto.id() == null) ? "guardar" : "actualizar";
        log.info("Intentando {} inmueble: {}", accion, inmuebleDto);

        try {
            InmuebleDto inmuebleGuardado = inmuebleServiceRequest.saveInmueble(inmuebleDto);
            String mensajeAccion = (accion.equals("guardar")) ? "guardado" : "actualizado";
            redirectAttributes.addFlashAttribute("mensajeExito",
                    "Inmueble '" + inmuebleGuardado.name() + "' " + mensajeAccion + " exitosamente.");
            log.info("Inmueble {} exitosamente con ID: {}", mensajeAccion, inmuebleGuardado.id());
        } catch (Exception e) {
            log.error("Error al {} el inmueble: {}", accion, e.getMessage(), e);
            redirectAttributes.addFlashAttribute("mensajeError",
                    "Error al " + accion + " el inmueble. Por favor, inténtelo de nuevo. Detalle: " + e.getMessage());
            // Si se quisiera volver al formulario en caso de error al guardar, se necesitaría el 'model'
            // y no hacer redirect, sino retornar "inmuebles/form-inmueble".
            // Por ahora, siempre redirigimos a la lista.
        }
        return "redirect:/ui/inmuebles";
    }

    /**
     * Maneja las peticiones GET a "/ui/inmuebles/eliminar/{idInmueble}" para eliminar un inmueble.
     *
     * @param idInmueble      El ID del inmueble a eliminar.
     * @param redirectAttributes Atributos para pasar mensajes de éxito/error tras la redirección.
     * @return Una cadena de redirección a la lista de inmuebles "/ui/inmuebles".
     */
    @GetMapping("/eliminar/{idInmueble}")
    public String eliminarInmueble(@PathVariable Long idInmueble, RedirectAttributes redirectAttributes) {
        log.info("Intentando eliminar inmueble con ID: {}", idInmueble);
        try {
            inmuebleServiceRequest.deleteInmueble(idInmueble);
            redirectAttributes.addFlashAttribute("mensajeExito",
                    "Inmueble con ID " + idInmueble + " eliminado exitosamente.");
            log.info("Inmueble con ID {} eliminado exitosamente.", idInmueble);
        } catch (feign.FeignException.NotFound e) {
            log.warn("Inmueble no encontrado (Feign 404) al intentar eliminar: ID {}", idInmueble, e);
            redirectAttributes.addFlashAttribute("mensajeError",
                    "Error: No se pudo encontrar el inmueble con ID " + idInmueble + " para eliminar.");
        } catch (Exception e) {
            log.error("Error al eliminar el inmueble con ID {}: {}", idInmueble, e.getMessage(), e);
            redirectAttributes.addFlashAttribute("mensajeError",
                    "Error al eliminar el inmueble con ID " + idInmueble + ". Detalle: " + e.getMessage());
        }
        return "redirect:/ui/inmuebles";
    }
}