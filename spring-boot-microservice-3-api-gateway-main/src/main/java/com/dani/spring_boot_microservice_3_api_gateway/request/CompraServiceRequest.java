package com.dani.spring_boot_microservice_3_api_gateway.request;

import com.dani.spring_boot_microservice_3_api_gateway.dto.CompraDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import java.util.List;

/**
 * Cliente Feign declarativo para interactuar con el microservicio 'compra-service'.
 * Define los endpoints expuestos por 'compra-service'.
 * Utiliza DTOs para la transferencia de datos.
 */
@FeignClient(
        value="compra-service", // Nombre del servicio en Eureka
        path="api/compra",      // Path base en el servicio destino
        // url="${compras.service.url}", // Descomentar si no se usa Eureka
        configuration = FeignConfiguration.class // Configuración común (ej. Basic Auth)
)
public interface CompraServiceRequest {

    /** Llama al endpoint POST /api/compra del servicio de compras. */
    @PostMapping
    CompraDto saveCompra(@RequestBody CompraDto requestBody);

    /** Llama al endpoint GET /api/compra/{userId} del servicio de compras. */
    @GetMapping("{userId}")
    List<CompraDto> getAllComprasOfUser(@PathVariable("userId") Long userId);
}