package com.dani.spring_boot_microservice_3_api_gateway.request;

import feign.auth.BasicAuthRequestInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración común para los clientes Feign.
 * En este caso, configura un interceptor para añadir autenticación básica (Basic Auth)
 * a todas las peticiones realizadas por los clientes Feign que usen esta configuración.
 * Esto es útil para la comunicación segura entre microservicios internos.
 */
@Configuration
public class FeignConfiguration {

    /**
     * Crea un bean BasicAuthRequestInterceptor.
     * Este interceptor añadirá automáticamente el header 'Authorization: Basic base64(username:password)'
     * a cada petición Feign. Las credenciales se toman de application.properties.
     *
     * @param username Nombre de usuario para Basic Auth (inyectado desde properties).
     * @param password Contraseña para Basic Auth (inyectada desde properties).
     * @return Una instancia de BasicAuthRequestInterceptor configurada.
     */
    @Bean
    public BasicAuthRequestInterceptor basicAuthRequestInterceptor(
            @Value("${service.security.secure-key-username}") String username,
            @Value("${service.security.secure-key-password}") String password) {

        return new BasicAuthRequestInterceptor(username, password);
    }
}