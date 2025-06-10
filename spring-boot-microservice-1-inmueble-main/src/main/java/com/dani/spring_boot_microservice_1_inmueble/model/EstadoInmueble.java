package com.dani.spring_boot_microservice_1_inmueble.model;

/**
 * Enumeración que representa los posibles estados de un {@link Inmueble}.
 * <p>
 * Define un conjunto fijo de estados que un inmueble puede tener a lo largo de su ciclo de vida
 * en el sistema, como "DISPONIBLE" para la venta/alquiler, "VENDIDO" o "RESERVADO".
 *
 * @author Daniel Núñez Rojas (danidev fullstack software)
 * @version 1.0
 * @since 2025-05-13
 */
public enum EstadoInmueble {

    /**
     * El inmueble está disponible para ser comprado o alquilado.
     */
    DISPONIBLE,

    /**
     * El inmueble ya ha sido vendido y no está disponible.
     */
    VENDIDO,

    /**
     * El inmueble está actualmente reservado por un posible comprador,
     * y no está disponible temporalmente.
     */
    RESERVADO
}