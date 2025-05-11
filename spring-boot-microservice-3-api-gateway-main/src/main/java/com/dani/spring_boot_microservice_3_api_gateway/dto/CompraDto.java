package com.dani.spring_boot_microservice_3_api_gateway.dto;

import java.time.LocalDateTime;

/**
 * Data Transfer Object (DTO) para representar una Compra en el API Gateway.
 * Usado para desacoplar la API del modelo de datos interno de compra-service.
 * Definido como un Record.
 *
 * @param id Identificador único de la compra.
 * @param userId ID del usuario que compró.
 * @param inmuebleId ID del inmueble comprado.
 * @param title Título (posiblemente del inmueble).
 * @param price Precio de compra.
 * @param purchaseDate Fecha de la compra.
 */
public record CompraDto(
        Long id,
        Long userId,
        Long inmuebleId,
        String title,
        Double price,
        LocalDateTime purchaseDate
) {}