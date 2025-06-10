package com.dani.spring_boot_microservice_3_api_gateway.dto;

import java.time.LocalDateTime;

/**
 * Data Transfer Object (DTO) para representar la información de una Compra
 * dentro del contexto del API Gateway.
 * <p>
 * Esta clase se utiliza para transferir datos de compras entre el API Gateway y
 * el {@code compra-service} (a través de Feign clients). También puede ser utilizada
 * para exponer datos de compras en los endpoints del API Gateway o en las vistas de la UI.
 * <p>
 * Al ser un {@code record} de Java, es inmutable y proporciona automáticamente
 * constructores, getters, {@code equals()}, {@code hashCode()}, y {@code toString()}.
 *
 * @param id           El identificador único de la transacción de compra. Puede ser nulo si el DTO representa una nueva compra antes de ser guardada.
 * @param userId       El identificador único del usuario que realizó la compra.
 * @param inmuebleId   El identificador único del inmueble que fue comprado.
 * @param title        Un título o descripción breve asociado a la compra (ej. el nombre del inmueble en el momento de la compra).
 * @param price        El precio final al que se realizó la compra del inmueble.
 * @param purchaseDate La fecha y hora exactas en que se registró la transacción de compra. Puede ser nulo si el DTO representa una nueva compra antes de ser guardada, ya que el servicio de compras suele asignar este valor.
 * @author Daniel Núñez Rojas (danidev fullstack software)
 * @version 1.0
 * @since 2025-05-13 (Fecha de creación inicial del DTO)
 */
public record CompraDto(
        Long id,
        Long userId,
        Long inmuebleId,
        String title,
        Double price,
        LocalDateTime purchaseDate
) {
    /* Como es un record, los métodos estándar (getters, equals, hashCode, toString)
     y el constructor canónico son generados automáticamente.
     Solo agregar código adicional aquí a menos que se desee una lógica o validación muy específica
     en el constructor, lo cual es menos común para DTOs puros. */
}