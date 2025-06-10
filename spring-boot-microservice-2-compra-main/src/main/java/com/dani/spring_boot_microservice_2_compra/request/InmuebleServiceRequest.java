package com.dani.spring_boot_microservice_2_compra.request;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Cliente Feign declarativo para interactuar con el microservicio {@code inmueble-service}.
 * <p>
 * Permite al {@code compra-service} realizar llamadas HTTP al {@code inmueble-service}
 * de una manera sencilla, como si estuviera llamando a un método Java local.
 * <p>
 * Esta interfaz está configurada para:
 * <ul>
 * <li>Apuntar al servicio con nombre "inmueble-service" registrado en Eureka.</li>
 * <li>Utilizar la configuración definida en {@link BasicAuthFeignConfiguration}, que
 * añade autenticación básica a todas las peticiones salientes.</li>
 * </ul>
 *
 * @see FeignClient Anotación principal de Spring Cloud OpenFeign.
 * @see BasicAuthFeignConfiguration Configuración personalizada para este cliente Feign.
 * @author Daniel Núñez Rojas (danidev fullstack software)
 * @version 1.0
 * @since 2025-05-18
 */
@FeignClient(
        value = "inmueble-service", // Nombre del servicio en Eureka
        configuration = BasicAuthFeignConfiguration.class
)
public interface InmuebleServiceRequest {

    /**
     * Llama al endpoint {@code PUT /api/inmueble/{inmuebleId}/estado} del servicio de inmuebles
     * para actualizar el estado de un inmueble específico.
     * <p>
     * Este método se utiliza típicamente después de una compra exitosa para marcar
     * el inmueble como "VENDIDO".
     *
     * @param inmuebleId El ID del inmueble cuyo estado se va a actualizar.
     * @param estado El nuevo estado a asignar al inmueble (ej. "VENDIDO").
     */
    @PutMapping("/api/inmueble/{inmuebleId}/estado")
    void updateInmuebleEstado(@PathVariable("inmuebleId") Long inmuebleId, @RequestParam("estado") String estado);
}