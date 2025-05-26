package com.dani.spring_boot_microservice_3_api_gateway.dto;

import java.time.LocalDateTime;

/**
 * Data Transfer Object (DTO) para representar un Inmueble en el API Gateway.
 * Usado para desacoplar la API del modelo de datos interno de inmueble-service.
 * Definido como un Record para inmutabilidad y concisión.
 *
 * @param id Identificador único.
 * @param name Nombre descriptivo.
 * @param address Dirección.
 * @param picture URL o referencia a la imagen.
 * @param price Precio.
 * @param creationDate Fecha de creación.
 */
public record InmuebleDto(
        Long id,
        Long userId,
        String name,
        String address,
        String picture,
        Double price,
        LocalDateTime creationDate,
        String estado
) {}