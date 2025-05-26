// En: ProyectoMicrosrvices/spring-boot-microservice-3-api-gateway-main/src/main/java/com/dani/spring_boot_microservice_3_api_gateway/controller/CompraController.java
package com.dani.spring_boot_microservice_3_api_gateway.controller;

import com.dani.spring_boot_microservice_3_api_gateway.dto.CompraDto;
import com.dani.spring_boot_microservice_3_api_gateway.request.CompraServiceRequest;
import com.dani.spring_boot_microservice_3_api_gateway.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j; // Añadir para logging
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller; // Cambiar a @Controller si va a redirigir
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.net.URI; // Para la redirección
import java.util.List;

@Controller // CAMBIADO de @RestController a @Controller para permitir redirecciones y flash attributes
@RequestMapping("gateway/compra") // Mantenemos el prefijo para la API
@RequiredArgsConstructor
@Tag(name = "Gateway - Compras", description = "Proxy para operaciones de Compras.")
@SecurityRequirement(name = "bearerAuth")
@Slf4j // AÑADIDO
public class CompraController {

    private final CompraServiceRequest compraServiceRequest;

    /**
     * Endpoint Gateway para registrar una nueva compra.
     * Este método está diseñado para ser llamado desde un formulario Thymeleaf.
     * Recibe los datos de la compra como parámetros, añade el ID del usuario autenticado,
     * y llama al servicio de compras. Luego redirige al catálogo con un mensaje.
     *
     * @param inmuebleId ID del inmueble a comprar.
     * @param title Título del inmueble.
     * @param price Precio del inmueble.
     * @param principal El UserPrincipal del usuario autenticado.
     * @param redirectAttributes Para enviar mensajes flash a la vista de redirección.
     * @return Una cadena de redirección (String) a la página del catálogo.
     */
    @PostMapping // Este endpoint será llamado por el formulario de la UI
    public String saveCompraFromUI( // Nombre del método cambiado para claridad
                                    @RequestParam("inmuebleId") Long inmuebleId,
                                    @RequestParam("title") String title,
                                    @RequestParam("price") Double price,
                                    @AuthenticationPrincipal UserPrincipal principal,
                                    RedirectAttributes redirectAttributes) {

        log.info("Intento de compra desde UI por usuario ID: {} para inmueble ID: {}", principal != null ? principal.getId() : "ANONYMOUS", inmuebleId);

        if (principal == null || principal.getId() == null) {
            // Si no hay usuario autenticado, no se puede comprar.
            // Spring Security debería prevenir esto si el endpoint está protegido,
            // pero una verificación adicional no hace daño.
            // Redirigir a login podría ser una opción, o mostrar error.
            redirectAttributes.addFlashAttribute("mensajeErrorCompra", "Debes iniciar sesión para comprar.");
            return "redirect:/login"; // O "redirect:/ui/catalogo" con el mensaje de error
        }

        // Construir el DTO para enviar al microservicio de compras
        CompraDto compraParaEnviar = new CompraDto(
                null,             // id de la compra (generado por el servicio de compras)
                principal.getId(),// userId del usuario autenticado
                inmuebleId,       // id del inmueble
                title,            // título del inmueble
                price,            // precio del inmueble
                null              // purchaseDate (generado por el servicio de compras)
        );

        try {
            log.debug("Enviando DTO de compra al servicio: {}", compraParaEnviar);
            CompraDto compraGuardada = compraServiceRequest.saveCompra(compraParaEnviar);
            log.info("Compra registrada exitosamente: {}", compraGuardada);
            redirectAttributes.addFlashAttribute("mensajeExitoCompra", "¡Inmueble '" + compraGuardada.title() + "' comprado exitosamente!");
        } catch (Exception e) {
            log.error("Error al procesar la compra para el inmueble ID {} por usuario ID {}: {}",
                    inmuebleId, principal.getId(), e.getMessage(), e); // Log con más detalle
            redirectAttributes.addFlashAttribute("mensajeErrorCompra", "Hubo un error al procesar tu compra. Por favor, inténtalo de nuevo.");
        }
        // Redirige de vuelta al catálogo después de la compra (o intento).
        return "redirect:/ui/catalogo";
    }

    /**
     * Endpoint Gateway para obtener todas las compras del usuario autenticado.
     * Este es un endpoint API que devuelve JSON, útil si se necesitara para JS o una app móvil.
     * La UI "Mis Compras" usará un método similar en un controlador de UI.
     * @param userPrincipal Detalles del usuario autenticado.
     * @return ResponseEntity con la lista de compras (DTOs) y estado OK.
     */
    @GetMapping("/api/mis-compras") // Ruta diferenciada para la API JSON
    @ResponseBody // Asegura que devuelve JSON y no busca una vista
    @Operation(summary = "Listar mis Compras (vía Gateway API)", description = "Solicita las compras del usuario autenticado al servicio de compras.")
    public ResponseEntity<List<CompraDto>> getAllComprasOfUserAPI(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        if (userPrincipal == null || userPrincipal.getId() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        log.debug("API: Solicitando compras para usuario ID: {}", userPrincipal.getId());
        List<CompraDto> compras = compraServiceRequest.getAllComprasOfUser(userPrincipal.getId());
        return ResponseEntity.ok(compras);
    }
}