package com.dani.spring_boot_microservice_3_api_gateway.model;

// Imports Lombok y Time
import lombok.Data;
import java.time.LocalDateTime;

// Imports Jakarta Persistence API (JPA) ACTUALIZADOS
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType; // Importar GenerationType
import jakarta.persistence.Column;
import jakarta.persistence.Enumerated; // Importar Enumerated
import jakarta.persistence.EnumType; // Importar EnumType
import jakarta.persistence.Transient; // Importar Transient

/**
 * Representa la entidad Usuario en la base de datos del API Gateway.
 * Almacena credenciales, información personal y roles para la autenticación y autorización.
 * Mapeada a la tabla 'users'.
 */
@Entity
@Table(name= "users") // Define el nombre de la tabla
@Data // Lombok: genera getters, setters, etc.
public class User {

    /**
     * Identificador único del usuario (clave primaria).
     * Generado automáticamente por la base de datos (estrategia IDENTITY).
     */
    @Id // Marca como clave primaria
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Define estrategia de generación de ID
    private Long id;

    /**
     * Nombre de usuario único utilizado para el login.
     * Mapeado a la columna 'username'.
     */
    @Column (name="username", unique = true, nullable = false, length = 100) // Columna 'username', debe ser única y no nula
    private String username;

    /**
     * Contraseña del usuario (almacenada de forma segura, hasheada).
     * Mapeado a la columna 'password'.
     */
    @Column(name="password", nullable = false, length = 255) // Columna 'password', no nula
    private String password;

    /**
     * Nombre real o completo del usuario.
     * Mapeado a la columna 'nombre'.
     */
    @Column(name="nombre", length = 150) // Columna 'nombre'
    private String nombre;

    /**
     * Fecha y hora en que se creó la cuenta del usuario.
     * Mapeado a la columna 'fecha_creacion'.
     */
    @Column(name="fecha_creacion", nullable = false) // Columna 'fecha_creacion', no nula
    private LocalDateTime fechaCreacion;

    /**
     * Rol asignado al usuario (USER o ADMIN). Determina los permisos.
     * Mapeado a la columna 'role', almacenado como String (EnumType.STRING).
     */
    @Enumerated(EnumType.STRING) // Especifica que el Enum se guarda como String en la BD
    @Column(name="role", nullable = false, length = 50) // Columna 'role', no nula
    private Role role;

    /**
     * Campo temporal para almacenar el token JWT generado durante el login.
     * No se persiste en la base de datos.
     */
    @Transient // Indica a JPA que este campo no debe ser mapeado a una columna de la BD
    private String token;
}