package com.dani.spring_boot_microservice_3_api_gateway.controller.ui;
import com.dani.spring_boot_microservice_3_api_gateway.model.User; // Importa tu entidad User
import com.dani.spring_boot_microservice_3_api_gateway.security.UserPrincipal;
import com.dani.spring_boot_microservice_3_api_gateway.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Controlador MVC para gestionar las vistas del Dashboard utilizando Thymeleaf.
 */
@Controller // ¡OJO! @Controller, no @RestController, porque devolvemos nombres de vistas.
@RequestMapping("/ui") // Un prefijo para todas las rutas de UI, ej. /ui/dashboard
@RequiredArgsConstructor
public class DashboardController {

    private final UserService userService; // Para obtener datos del usuario si es necesario

    /**
     * Maneja las peticiones a la página principal del dashboard.
     * @param model El objeto Model para pasar datos a la vista.
     * @param principal El UserPrincipal del usuario autenticado (si existe).
     * @return El nombre de la plantilla Thymeleaf a renderizar ("dashboard").
     */
    @GetMapping("/dashboard")
    public String dashboardPage(Model model, @AuthenticationPrincipal UserPrincipal principal) {
        if (principal != null) {
            // Buscamos el usuario completo para tener todos sus datos (nombre, rol, etc.)
            // findByUserameReturnToken ya devuelve el User con el token (aunque el token no se use en la UI)
            User currentUser = userService.findByUserameReturnToken(principal.getUsername());
            model.addAttribute("currentUser", currentUser);
        }
        // Aquí podrías añadir más datos al modelo si los necesitas para el dashboard,
        // por ejemplo, llamando a InmuebleServiceRequest o CompraServiceRequest
        // model.addAttribute("listaInmuebles", inmuebleServiceRequest.getAllInmuebles());

        return "dashboard"; // Devuelve el nombre del archivo HTML (sin .html) en /resources/templates/
    }
}