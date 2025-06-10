package com.dani.spring_boot_microservice_3_api_gateway.request;

import com.dani.spring_boot_microservice_3_api_gateway.dto.CompraDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import java.util.List;

/**
 * Cliente Feign declarativo para interactuar con el microservicio {@code compra-service}.
 * <p>
 * Define los métodos Java que se mapean a los endpoints expuestos por {@code compra-service}.
 * Spring Cloud OpenFeign se encarga de la implementación en tiempo de ejecución, manejando
 * la comunicación HTTP, el balanceo de carga (a través de Eureka) y la serialización/deserialización
 * de objetos usando DTOs como {@link CompraDto}.
 * <p>
 * Esta interfaz está configurada para:
 * <ul>
 * <li>Apuntar al servicio con nombre "compra-service" registrado en Eureka.</li>
 * <li>Usar el path base "/api/compra" para todas sus peticiones al servicio destino.</li>
 * <li>Utilizar la configuración definida en {@link PropagateUserFeignConfiguration}, que
 * incluye interceptores para la autenticación básica y la propagación del contexto del usuario.</li>
 * </ul>
 *
 * @see FeignClient Anotación principal de Spring Cloud OpenFeign.
 * @see CompraDto DTO utilizado para la transferencia de datos de compras.
 * @see PropagateUserFeignConfiguration Configuración personalizada para este cliente Feign.
 * @author Daniel Núñez Rojas (danidev fullstack software) // O tu nombre
 * @version 1.0
 * @since 2025-05-13 // Fecha de creación o última modificación significativa
 */
@FeignClient(
        value="compra-service",
        path="api/compra",
        configuration = PropagateUserFeignConfiguration.class
)
public interface CompraServiceRequest {

    /**
     * Llama al endpoint {@code POST /api/compra} del servicio de compras para
     * registrar (guardar) una nueva compra.
     *
     * @param requestBody Un objeto {@link CompraDto} que contiene los detalles de la compra a registrar.
     * Se espera que el {@code userId} y el {@code inmuebleId} estén presentes en el DTO.
     * @return El {@link CompraDto} representando la compra guardada, tal como lo devuelve el servicio
     * (incluyendo el ID de compra asignado y la fecha de compra).
     */
    @PostMapping
    CompraDto saveCompra(@RequestBody CompraDto requestBody); //

    /**
     * Llama al endpoint {@code GET /api/compra/{userId}} del servicio de compras para
     * obtener todas las compras realizadas por un usuario específico.
     *
     * @param userId El ID del usuario cuyas compras se desean recuperar.
     * @return Una lista de {@link CompraDto} representando todas las compras asociadas al usuario.
     * Puede devolver una lista vacía si el usuario no ha realizado compras.
     */
    @GetMapping("{userId}")
    List<CompraDto> getAllComprasOfUser(@PathVariable("userId") Long userId); //
}