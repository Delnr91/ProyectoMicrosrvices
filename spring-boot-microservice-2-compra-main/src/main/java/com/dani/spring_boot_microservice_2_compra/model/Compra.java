package com.dani.spring_boot_microservice_2_compra.model;

// Imports de Lombok para generar código boilerplate.
import lombok.*; // Importa Data, AllArgsConstructor, NoArgsConstructor, Getter, Setter

// Imports de Jakarta Persistence API (JPA) actualizados.
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Column;

import java.time.LocalDateTime; // Para manejar fechas y horas.

/**
 * Representa una compra realizada por un usuario sobre un inmueble.
 * Esta entidad almacena la información de la transacción de compra.
 * Mapeada a la tabla 'compra' en la base de datos.
 *
 * @author TuNombre (o el autor original)
 * @version 1.0
 * @since 2025-05-04 (Fecha de creación o última modificación)
 */
@Entity // Marca esta clase como una entidad JPA.
@Table(name="compra") // Especifica el nombre de la tabla en la BD.
// Anotaciones Lombok:
@Data // Genera getters, setters, toString, equals, hashCode.
@AllArgsConstructor // Genera un constructor con todos los argumentos.
@NoArgsConstructor // Genera un constructor sin argumentos (requerido por JPA).
// @Getter y @Setter son redundantes si se usa @Data, pero se dejan si estaban antes.
// @Getter
// @Setter
public class Compra {

    /**
     * Identificador único de la compra (clave primaria).
     * Generado automáticamente por la base de datos (estrategia IDENTITY).
     */
    @Id // Marca como clave primaria.
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Estrategia de generación de ID.
    private Long id;

    /**
     * ID del usuario que realizó la compra.
     * Este campo vincula la compra con un usuario (posiblemente de otro microservicio o tabla).
     * Mapeado a la columna 'user_id'.
     */
    @Column(name="user_id", nullable = false) // Columna 'user_id', no puede ser nulo.
    private Long userId;

    /**
     * ID del inmueble que fue comprado.
     * Este campo vincula la compra con un inmueble (posiblemente del microservicio inmueble-service).
     * Mapeado a la columna 'inmueble_id'.
     */
    @Column(name="inmueble_id", nullable = false) // Columna 'inmueble_id', no puede ser nulo.
    private Long inmuebleId;

    /**
     * Título o descripción breve de la compra/inmueble en el momento de la compra.
     * Mapeado a la columna 'titulo'.
     */
    @Column(name="titulo", length = 255) // Columna 'titulo'.
    private String title;

    /**
     * Precio al que se realizó la compra.
     * Mapeado a la columna 'precio'.
     */
    @Column(name="precio", nullable = false) // Columna 'precio', no puede ser nulo.
    private Double price;

    /**
     * Fecha y hora exactas en que se registró la compra.
     * Se asigna automáticamente en la capa de servicio al guardar.
     * Mapeado a la columna 'fecha_compra'.
     */
    @Column(name="fecha_compra") // Columna 'fecha_compra'.
    private LocalDateTime purchaseDate;

}