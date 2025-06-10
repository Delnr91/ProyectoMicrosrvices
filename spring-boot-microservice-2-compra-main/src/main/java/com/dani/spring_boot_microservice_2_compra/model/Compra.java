package com.dani.spring_boot_microservice_2_compra.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Entidad JPA que representa una transacción de compra en el sistema.
 * <p>
 * Esta clase es el objeto de dominio principal para el {@code compra-service}.
 * Almacena el registro de qué usuario compró qué inmueble, a qué precio y cuándo.
 * Está mapeada a la tabla "compras" en la base de datos PostgreSQL.
 *
 * @author Daniel Núñez Rojas (danidev fullstack software)
 * @version 1.0
 * @since 2025-05-13
 */
@Entity
@Table(name = "compras")
@Data
public class Compra {

    /**
     * Identificador único de la transacción de compra, generado automáticamente.
     * Es la clave primaria de la tabla {@code compras}.
     * Se utiliza la estrategia de generación {@link GenerationType#IDENTITY}.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Identificador del usuario que realizó la compra.
     * Este ID corresponde al ID del usuario en el sistema API Gateway.
     * Este campo es obligatorio.
     * Mapeado a la columna {@code user_id}.
     */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /**
     * Identificador del inmueble que fue comprado.
     * Este ID corresponde al ID del inmueble en el {@code inmueble-service}.
     * Este campo es obligatorio.
     * Mapeado a la columna {@code inmueble_id}.
     */
    @Column(name = "inmueble_id", nullable = false)
    private Long inmuebleId;

    /**
     * Título o nombre del inmueble en el momento de la compra.
     * Se almacena aquí para mantener un registro histórico, incluso si el
     * nombre del inmueble original cambia posteriormente.
     * Este campo es obligatorio.
     * Mapeado a la columna {@code titulo}.
     */
    @Column(name = "titulo", nullable = false)
    private String title;

    /**
     * Precio final al que se realizó la compra.
     * Se almacena aquí para mantener un registro histórico del precio de la transacción.
     * Este campo es obligatorio.
     * Mapeado a la columna {@code precio}.
     */
    @Column(name = "precio", nullable = false)
    private Double price;

    /**
     * Fecha y hora exactas en que se registró la compra.
     * Este campo se asigna automáticamente al momento de la creación de la compra.
     * Mapeado a la columna {@code fecha_compra}.
     */
    @Column(name = "fecha_compra", nullable = false)
    private LocalDateTime purchaseDate;
}