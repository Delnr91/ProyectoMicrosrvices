package com.dani.spring_boot_microservice_3_api_gateway.request;

import com.dani.spring_boot_microservice_3_api_gateway.dto.InmuebleDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * Cliente Feign declarativo para interactuar con el microservicio 'inmueble-service'.
 * Define los endpoints expuestos por 'inmueble-service' que serán consumidos por el gateway.
 * Utiliza DTOs para la transferencia de datos.
 */
@FeignClient(
        value = "inmueble-service", // Nombre del servicio registrado en Eureka
        path = "/api/inmueble",     // Path base en el servicio destino
        // url = "${inmueble.service.url}", // Descomentar si no se usa Eureka y se configura URL directa
        configuration = FeignConfiguration.class // Configuración común (ej. Basic Auth)
)
public interface InmuebleServiceRequest {

    /** Llama al endpoint POST /api/inmueble del servicio de inmuebles. */
    @PostMapping
    InmuebleDto saveInmueble(@RequestBody InmuebleDto requestBody);

    /** Llama al endpoint DELETE /api/inmueble/{inmuebleId} del servicio de inmuebles. */
    @DeleteMapping("{inmuebleId}")
    void deleteInmueble(@PathVariable("inmuebleId") Long inmuebleId);

    /** Llama al endpoint GET /api/inmueble del servicio de inmuebles. */
    @GetMapping()
    List<InmuebleDto> getAllInmuebles();
}