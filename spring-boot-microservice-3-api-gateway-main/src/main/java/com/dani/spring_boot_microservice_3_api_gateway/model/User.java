package com.dani.spring_boot_microservice_3_api_gateway.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * Entidad JPA que representa a un usuario en el sistema API Gateway.
 * Almacena la información de autenticación (credenciales), datos personales básicos,
 * el rol del usuario para la autorización, y la fecha de creación de la cuenta.
 * Esta entidad está mapeada a la tabla "users" en la base de datos.
 *
 * @author Daniel Núñez Rojas (danidev fullstack software)
 * @version 1.1
 * @since 2025-05-04 (Fecha de creación inicial o última modificación significativa de la entidad)
 */
@Entity
@Table(name= "users")
@Data
public class User {

    /**
     * Identificador único del usuario, generado automáticamente por la base de datos.
     * Es la clave primaria de la tabla {@code users}.
     * Se utiliza la estrategia de generación {@link GenerationType#IDENTITY}.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Nombre de usuario único utilizado para el inicio de sesión (login).
     * Este campo es obligatorio y debe ser único en toda la tabla.
     * Su longitud máxima está definida en 100 caracteres.
     * Mapeado a la columna {@code username}.
     */
    @Column (name="username", unique = true, nullable = false, length = 100)
    private String username;

    /**
     * Contraseña del usuario. Se almacena de forma segura en la base de datos
     * después de ser codificada (hasheada) usando un {@link org.springframework.security.crypto.password.PasswordEncoder}.
     * Este campo es obligatorio. Su longitud máxima es de 255 caracteres para acomodar el hash.
     * Mapeado a la columna {@code password}.
     */
    @Column(name="password", nullable = false, length = 255)
    private String password;

    /**
     * Nombre real o completo del usuario.
     * Este campo es opcional y tiene una longitud máxima de 150 caracteres.
     * Mapeado a la columna {@code nombre}.
     */
    @Column(name="nombre", length = 150)
    private String nombre;

    /**
     * Fecha y hora exactas en que se creó la cuenta del usuario en el sistema.
     * Este campo es obligatorio y se asigna automáticamente al momento de la creación del usuario.
     * Mapeado a la columna {@code fecha_creacion}.
     */
    @Column(name="fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;

    /**
     * Rol asignado al usuario (ej. {@link Role#USER}, {@link Role#ADMIN}).
     * Determina los permisos y el nivel de acceso del usuario dentro del sistema.
     * Este campo es obligatorio y se almacena como una cadena de texto (String) en la base de datos.
     * Mapeado a la columna {@code role}.
     * * @see Role Para los posibles valores de roles.
     */
    @Enumerated(EnumType.STRING)
    @Column(name="role", nullable = false, length = 50)
    private Role role;

    /**
     * Campo temporal (transitorio) utilizado para almacenar el token JWT (JSON Web Token)
     * generado para el usuario después de una autenticación exitosa o durante su creación.
     * Este campo **no se persiste** en la base de datos, como indica la anotación {@link Transient}.
     * Se utiliza para enviar el token de vuelta al cliente como parte del objeto User.
     */
    @Transient
    private String token;
}