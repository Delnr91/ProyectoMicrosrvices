package com.dani.spring_boot_microservice_3_api_gateway.controller;

import com.dani.spring_boot_microservice_3_api_gateway.dto.CompraDto;
import com.dani.spring_boot_microservice_3_api_gateway.request.CompraServiceRequest;
import com.dani.spring_boot_microservice_3_api_gateway.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * Controlador que gestiona las operaciones relacionadas con las compras,
 * actuando como un proxy hacia el microservicio de compras.
 * <p>
 * Este controlador es un {@link Controller} estándar de Spring MVC, no un {@link RestController},
 * ya que maneja tanto peticiones de API que devuelven JSON como peticiones de formularios
 * Thymeleaf que resultan en redirecciones.
 *
 * @author Daniel Núñez Rojas (danidev fullstack software)
 * @version 1.1
 * @since 2025-06-05 (Actualizado con JavaDoc completo)
 */
@Controller
@RequestMapping("gateway/compra")
@RequiredArgsConstructor
@Slf4j
public class CompraController {

    private final CompraServiceRequest compraServiceRequest;

    /**
     * Procesa una solicitud de compra proveniente de un formulario de la UI (Thymeleaf).
     * <p>
     * Construye un {@link CompraDto} y lo envía al servicio de compras a través del
     * cliente Feign. Maneja los resultados para mostrar mensajes de éxito o error
     * al usuario a través de {@link RedirectAttributes}.
     *
     * @param inmuebleId El ID del inmueble que se está comprando.
     * @param title El título del inmueble.
     * @param price El precio del inmueble.
     * @param principal El objeto {@link UserPrincipal} del usuario autenticado que realiza la compra.
     * @param redirectAttributes Utilizado para pasar mensajes flash a la vista después de la redirección.
     * @return Una cadena de texto que indica la redirección a la página del catálogo.
     */
    @PostMapping
    public String saveCompraFromUI(
            @RequestParam("inmuebleId") Long inmuebleId,
            @RequestParam("title") String title,
            @RequestParam("price") Double price,
            @AuthenticationPrincipal UserPrincipal principal,
            RedirectAttributes redirectAttributes) {

        log.info("Intento de compra desde UI por usuario ID: {} para inmueble ID: {}", principal != null ? principal.getId() : "ANONYMOUS", inmuebleId);

        if (principal == null || principal.getId() == null) {
            redirectAttributes.addFlashAttribute("mensajeErrorCompra", "Debes iniciar sesión para comprar.");
            return "redirect:/login";
        }

        CompraDto compraParaEnviar = new CompraDto(
                null,
                principal.getId(),
                inmuebleId,
                title,
                price,
                null
        );

        try {
            log.debug("Enviando DTO de compra al servicio: {}", compraParaEnviar);
            CompraDto compraGuardada = compraServiceRequest.saveCompra(compraParaEnviar);
            log.info("Compra registrada exitosamente: {}", compraGuardada);
            redirectAttributes.addFlashAttribute("mensajeExitoCompra", "¡Inmueble '" + compraGuardada.title() + "' comprado exitosamente!");
        } catch (Exception e) {
            log.error("Error al procesar la compra para el inmueble ID {} por usuario ID {}: {}",
                    inmuebleId, principal.getId(), e.getMessage(), e);
            redirectAttributes.addFlashAttribute("mensajeErrorCompra", "Hubo un error al procesar tu compra. Por favor, inténtalo de nuevo.");
        }

        return "redirect:/ui/catalogo";
    }

    /**
     * Endpoint de API para obtener todas las compras realizadas por el usuario autenticado.
     *
     * @param userPrincipal El principal del usuario autenticado.
     * @return Un {@link ResponseEntity} con una lista de {@link CompraDto} y estado 200 (OK),
     * o 401 (Unauthorized) si no hay un usuario autenticado.
     */
    @GetMapping("/api/mis-compras")
    @ResponseBody
    public ResponseEntity<List<CompraDto>> getAllComprasOfUserAPI(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        if (userPrincipal == null || userPrincipal.getId() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        log.debug("API: Solicitando compras para usuario ID: {}", userPrincipal.getId());
        List<CompraDto> compras = compraServiceRequest.getAllComprasOfUser(userPrincipal.getId());
        return ResponseEntity.ok(compras);
    }
}