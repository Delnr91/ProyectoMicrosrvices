package com.dani.spring_boot_microservice_1_inmueble.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Entidad JPA que representa un Inmueble en el sistema.
 * <p>
 * Esta clase es el objeto de dominio principal para el {@code inmueble-service}.
 * Almacena toda la información relevante de una propiedad, como su nombre, dirección,
 * precio, estado y propietario.
 * Está mapeada a la tabla "inmueble" en la base de datos PostgreSQL.
 *
 * @author Daniel Núñez Rojas (danidev fullstack software)
 * @version 1.1
 * @since 2025-05-13 (Actualizado con JavaDoc completo)
 */
@Entity
@Table(name = "inmueble")
@Data
public class Inmueble {

    /**
     * Identificador único del inmueble, generado automáticamente por la base de datos.
     * Es la clave primaria de la tabla {@code inmueble}.
     * Se utiliza la estrategia de generación {@link GenerationType#IDENTITY}.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Nombre descriptivo o título del inmueble.
     * Este campo es obligatorio.
     * Mapeado a la columna {@code nombre}.
     */
    @Column(name = "nombre", nullable = false)
    private String name;

    /**
     * Dirección física completa del inmueble.
     * Este campo es obligatorio.
     * Mapeado a la columna {@code direccion}.
     */
    @Column(name = "direccion", nullable = false)
    private String address;

    /**
     * URL que apunta a la imagen principal del inmueble.
     * Este campo es opcional.
     * Mapeado a la columna {@code foto}.
     */
    @Column(name = "foto")
    private String picture;

    /**
     * Precio de venta o alquiler del inmueble.
     * Este campo es obligatorio.
     * Mapeado a la columna {@code precio}.
     */
    @Column(name = "precio", nullable = false)
    private Double price;

    /**
     * Fecha y hora exactas en que el inmueble fue registrado o creado en el sistema.
     * Este campo se asigna automáticamente al momento de la creación.
     * Mapeado a la columna {@code fecha_creacion}.
     */
    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime creationDate;

    /**
     * El identificador único del usuario (del sistema API Gateway) que es propietario
     * o ha creado este inmueble.
     * Este campo es crucial para la lógica de permisos.
     * Mapeado a la columna {@code user_id}.
     */
    @Column(name = "user_id")
    private Long userId;

    /**
     * El estado actual del inmueble (ej. {@link EstadoInmueble#DISPONIBLE}, {@link EstadoInmueble#VENDIDO}).
     * Se almacena como una cadena de texto en la base de datos.
     * Mapeado a la columna {@code estado}.
     *
     * @see EstadoInmueble Para los posibles valores.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "estado")
    private EstadoInmueble estado;
}