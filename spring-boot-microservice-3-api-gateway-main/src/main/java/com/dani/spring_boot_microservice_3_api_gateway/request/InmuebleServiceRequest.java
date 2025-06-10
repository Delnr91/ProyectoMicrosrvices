package com.dani.spring_boot_microservice_3_api_gateway.request;

import com.dani.spring_boot_microservice_3_api_gateway.dto.InmuebleDto;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

/**
 * Cliente Feign declarativo para interactuar con el microservicio {@code inmueble-service}.
 * <p>
 * Define los métodos Java que se mapean a los endpoints expuestos por {@code inmueble-service}.
 * Spring Cloud OpenFeign se encarga de generar la implementación de esta interfaz en tiempo de ejecución,
 * incluyendo la lógica para la comunicación HTTP, el balanceo de carga (si se usa con Eureka/LoadBalancer),
 * y la serialización/deserialización de objetos (usando DTOs como {@link InmuebleDto}).
 * <p>
 * Esta interfaz está configurada para:
 * <ul>
 * <li>Apuntar al servicio con nombre "inmueble-service" registrado en Eureka.</li>
 * <li>Usar el path base "/api/inmueble" para todas sus peticiones al servicio destino.</li>
 * <li>Utilizar la configuración definida en {@link PropagateUserFeignConfiguration}, que
 * incluye interceptores para la autenticación básica entre servicios y la propagación
 * del contexto del usuario final (X-User-ID, X-User-Roles).</li>
 * <li>Incorpora un Circuit Breaker (Resilience4j) para el método {@link #getAllInmuebles()}
 * con un método de fallback.</li>
 * </ul>
 *
 * @see FeignClient Anotación principal de Spring Cloud OpenFeign.
 * @see InmuebleDto DTO utilizado para la transferencia de datos de inmuebles.
 * @see PropagateUserFeignConfiguration Configuración personalizada para este cliente Feign.
 * @see CircuitBreaker Anotación para la implementación del patrón Circuit Breaker.
 * @author Daniel Núñez Rojas (danidev fullstack software) // O tu nombre
 * @version 1.1
 * @since 2025-05-13 (Actualizado con Circuit Breaker y JavaDoc)
 */
@FeignClient(
        value = "inmueble-service",
        path = "/api/inmueble",
        configuration = PropagateUserFeignConfiguration.class
)
public interface InmuebleServiceRequest {

    /**
     * Llama al endpoint {@code POST /api/inmueble} del servicio de inmuebles para
     * guardar un nuevo inmueble o actualizar uno existente.
     *
     * @param requestBody Un objeto {@link InmuebleDto} que contiene los datos del inmueble a guardar/actualizar.
     * @return El {@link InmuebleDto} representando el inmueble guardado o actualizado, tal como lo devuelve el servicio.
     */
    @PostMapping
    InmuebleDto saveInmueble(@RequestBody InmuebleDto requestBody); //

    /**
     * Llama al endpoint {@code DELETE /api/inmueble/{inmuebleId}} del servicio de inmuebles
     * para eliminar un inmueble específico.
     *
     * @param inmuebleId El ID del inmueble a eliminar.
     */
    @DeleteMapping("{inmuebleId}")
    void deleteInmueble(@PathVariable("inmuebleId") Long inmuebleId); //

    /**
     * Llama al endpoint {@code GET /api/inmueble} del servicio de inmuebles para
     * obtener una lista de todos los inmuebles registrados.
     * <p>
     * Este método está protegido por un Circuit Breaker llamado "inmuebleServiceCircuitBreaker".
     * Si el servicio de inmuebles no está disponible o responde con errores repetidamente,
     * el circuito se abrirá y se ejecutará el método de fallback {@link #fallbackGetAllInmuebles(Throwable)}.
     * La configuración del Circuit Breaker (umbrales, timeouts, etc.) se define en
     * el archivo {@code application.properties} del API Gateway.
     *
     * @return Una lista de {@link InmuebleDto} representando todos los inmuebles.
     * Puede devolver una lista vacía si no hay inmuebles o si el fallback se activa.
     */
    @GetMapping()
    @CircuitBreaker(name = "inmuebleServiceCircuitBreaker", fallbackMethod = "fallbackGetAllInmuebles") //
    List<InmuebleDto> getAllInmuebles(); //

    /**
     * Método de fallback para {@link #getAllInmuebles()}.
     * Se ejecuta cuando el Circuit Breaker "inmuebleServiceCircuitBreaker" está abierto
     * o si la llamada original al servicio de inmuebles falla de una manera que activa el Circuit Breaker.
     * <p>
     * Este método proporciona una respuesta degradada (una lista vacía) en lugar de propagar el error,
     * mejorando la resiliencia del API Gateway.
     * Imprime un mensaje de error en la consola de error estándar para alertar sobre el problema.
     *
     * @param throwable La excepción que causó la activación del fallback (útil para logging y diagnóstico).
     * @return Una lista vacía de {@link InmuebleDto} como respuesta de contingencia.
     */
    default List<InmuebleDto> fallbackGetAllInmuebles(Throwable throwable) { //
        // Para logging en una interfaz con métodos default, no se puede usar @Slf4j directamente.
        // Se podría instanciar un logger explícitamente:
        // org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(InmuebleServiceRequest.class);
        // logger.warn("Fallback para getAllInmuebles activado debido a: {}", throwable.getMessage());
        System.err.println("Fallback para InmuebleServiceRequest.getAllInmuebles() activado. Causa: " + (throwable != null ? throwable.getMessage() : "Desconocida"));
        return Collections.emptyList();
    }

    /**
     * Llama al endpoint {@code GET /api/inmueble/{inmuebleId}} del servicio de inmuebles
     * para obtener los detalles de un inmueble específico por su ID.
     *
     * @param inmuebleId El ID del inmueble a recuperar.
     * @return Un {@link InmuebleDto} con los detalles del inmueble encontrado.
     * @throws feign.FeignException.NotFound Si el inmueble con el ID especificado no se encuentra en el servicio destino.
     */
    @GetMapping("/{inmuebleId}")
    InmuebleDto getInmuebleById(@PathVariable("inmuebleId") Long inmuebleId); //

    /**
     * Llama al endpoint {@code GET /api/inmueble/user/{userId}} del servicio de inmuebles
     * para obtener todos los inmuebles asociados a un ID de usuario específico.
     *
     * @param userId El ID del usuario cuyos inmuebles se desean recuperar.
     * @return Una lista de {@link InmuebleDto} representando los inmuebles del usuario.
     * Puede devolver una lista vacía si el usuario no tiene inmuebles.
     */
    @GetMapping("/user/{userId}")
    List<InmuebleDto> getAllInmueblesByUserId(@PathVariable("userId") Long userId); //

}