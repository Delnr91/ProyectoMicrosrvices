package com.dani.spring_boot_microservice_2_compra.request;

import feign.auth.BasicAuthRequestInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Clase de configuración para los clientes Feign de este microservicio.
 * <p>
 * Define un bean para {@link BasicAuthRequestInterceptor} que se aplicará
 * a las peticiones salientes realizadas por los clientes Feign que referencien
 * esta configuración. Su propósito es asegurar la comunicación de servicio a servicio.
 *
 * @author Daniel Núñez Rojas (danidev fullstack software)
 * @version 1.0
 * @since 2025-05-18
 */
@Configuration
public class BasicAuthFeignConfiguration {

    /**
     * Define un bean para {@link BasicAuthRequestInterceptor}.
     * <p>
     * Este interceptor añade automáticamente un header de "Authorization" con
     * credenciales de autenticación básica (username y password) a cada petición
     * realizada por el cliente Feign.
     * <p>
     * Las credenciales se cargan desde las propiedades de la aplicación
     * ({@code service.security.secure-key-username-2} y {@code service.security.secure-key-password-2}),
     * permitiendo que este servicio se autentique de forma segura con otros servicios
     * internos (como el {@code inmueble-service}).
     *
     * @param username El nombre de usuario para la autenticación básica, inyectado desde propiedades.
     * @param password La contraseña para la autenticación básica, inyectada desde propiedades.
     * @return Una instancia de {@link BasicAuthRequestInterceptor} configurada.
     */
    @Bean
    public BasicAuthRequestInterceptor basicAuthRequestInterceptor(
            @Value("${service.security.secure-key-username-2}") String username,
            @Value("${service.security.secure-key-password-2}") String password
    ) {
        return new BasicAuthRequestInterceptor(username, password);
    }
}