package com.dani.spring_boot_microservice_3_api_gateway.dto;

import java.time.LocalDateTime;

/**
 * Data Transfer Object (DTO) para representar la información de un Inmueble
 * dentro del contexto del API Gateway.
 * <p>
 * Esta clase se utiliza para transferir datos de inmuebles entre el API Gateway y
 * otros servicios (como el {@code inmueble-service} a través de Feign clients),
 * así como para exponer datos de inmuebles en los endpoints del API Gateway
 * o para ser utilizada en las vistas de la interfaz de usuario (Thymeleaf).
 * <p>
 * Al ser un {@code record} de Java, es inmutable por naturaleza y proporciona automáticamente
 * constructores, getters, {@code equals()}, {@code hashCode()}, y {@code toString()}.
 *
 * @param id El identificador único del inmueble. Puede ser nulo si el DTO representa un inmueble nuevo antes de ser guardado.
 * @param userId El identificador único del usuario propietario o creador del inmueble.
 * @param name El nombre descriptivo o título del inmueble (ej. "Casa de Playa con Vista al Mar").
 * @param address La dirección física completa del inmueble.
 * @param picture La URL o referencia a la imagen principal del inmueble.
 * @param price El precio de venta o alquiler del inmueble.
 * @param creationDate La fecha y hora en que se registró o creó el inmueble.
 * @param estado El estado actual del inmueble (ej. "DISPONIBLE", "VENDIDO", "RESERVADO"). Representado como String.
 *
 * @author Daniel Núñez Rojas (danidev fullstack software)
 * @version 1.0
 * @since 2025-05-13 (Fecha de creación inicial del DTO)
 */
public record InmuebleDto(
        Long id,
        Long userId,
        String name,
        String address,
        String picture,
        Double price,
        LocalDateTime creationDate,
        String estado // Mantenemos como String para flexibilidad, el enum EstadoInmueble está en el microservicio de inmueble
) {
}