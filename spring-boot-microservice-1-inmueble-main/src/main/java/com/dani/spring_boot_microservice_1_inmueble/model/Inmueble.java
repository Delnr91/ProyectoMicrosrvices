package com.dani.spring_boot_microservice_1_inmueble.model;

import jakarta.persistence.*;
import lombok.Data; // Importa la anotación @Data de Lombok para generar automáticamente getters, setters, toString, etc.

// Imports de Jakarta Persistence API (JPA) para el mapeo objeto-relacional.

import java.time.LocalDateTime; // Importa la clase para manejar fechas y horas.

/**
 * Representa la entidad Inmueble en la base de datos.
 * Esta clase define la estructura de la tabla 'inmueble' y sus campos
 * correspondientes.
 * Utiliza Lombok (@Data) para reducir el código boilerplate (getters, setters,
 * etc.).
 *
 * @author TuNombre (o el autor original)
 * @version 1.0
 * @since 2025-05-03 (Fecha de creación o última modificación significativa)
 */
@Data // Anotación de Lombok: genera getters, setters, equals, hashCode y toString.
@Entity // Anotación JPA: marca esta clase como una entidad gestionada.
@Table(name = "inmueble") // Anotación JPA: especifica el nombre de la tabla en la base de datos.
public class Inmueble {

    /**
     * Identificador único del inmueble.
     * Es la clave primaria de la tabla, generada automáticamente por la base de
     * datos (estrategia IDENTITY).
     */
    @Id // Anotación JPA: marca este campo como la clave primaria.
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Anotación JPA: especifica que el valor es generado
                                                        // automáticamente por la BD (autoincremental).
    private Long id;

    /**
     * ID del usuario que creó o posee el inmueble.
     * Este campo puede ser utilizado para establecer relaciones con otras entidades
     * (ej. Usuario).
     */
    @Column(name = "user_id") // Nombre de la columna en la base de datos
    private Long userId; // ID del usuario que creó/posee el inmueble

    /**
     * Nombre descriptivo o título del inmueble.
     * Mapeado a la columna 'nombre' en la base de datos.
     */
    @Column(name = "nombre", length = 255) // Anotación JPA: mapea este campo a la columna 'nombre'. Se puede
                                           // especificar longitud, nulabilidad, etc.
    private String name;

    /**
     * Dirección física del inmueble.
     * Mapeado a la columna 'direccion' en la base de datos.
     */
    @Column(name = "direccion", length = 500) // Mapea a la columna 'direccion'.
    private String address;

    /**
     * URL o referencia a la imagen/foto principal del inmueble.
     * Mapeado a la columna 'foto' en la base de datos.
     * Podría ser una URL o un path a un archivo.
     */
    @Column(name = "foto", length = 1024) // Mapea a la columna 'foto'. Aumentar longitud si son URLs largas.
    private String picture;

    /**
     * Precio de venta o alquiler del inmueble.
     * Mapeado a la columna 'precio' en la base de datos.
     */
    @Column(name = "precio") // Mapea a la columna 'precio'.
    private Double price;

    /**
     * Fecha y hora en que se registró o creó el inmueble en el sistema.
     * Mapeado a la columna 'fecha' en la base de datos.
     * Se asigna automáticamente en la capa de servicio al guardar.
     */
    @Column(name = "fecha") // Mapea a la columna 'fecha'.
    private LocalDateTime creationDate;



    @Enumerated(EnumType.STRING) // Indica que el enum se guardará como String en la BD
    @Column(name = "estado", length = 50) // Define la columna y su longitud
    private EstadoInmueble estado;
}