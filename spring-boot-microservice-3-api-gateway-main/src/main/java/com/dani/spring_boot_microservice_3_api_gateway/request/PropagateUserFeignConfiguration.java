package com.dani.spring_boot_microservice_3_api_gateway.request;

import feign.RequestInterceptor;
import feign.auth.BasicAuthRequestInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración de Feign que incluye interceptores para:
 * 1. Autenticación Básica entre servicios (para la comunicación interna segura).
 * 2. Propagación del contexto del usuario final (X-User-ID, X-User-Roles).
 *
 * @author Daniel Núñez Rojas (danidev fullstack software)
 */
@Configuration
public class PropagateUserFeignConfiguration {

    /**
     * Bean para el interceptor de Autenticación Básica.
     */
    @Bean
    public BasicAuthRequestInterceptor basicAuthRequestInterceptor(
            @Value("${service.security.secure-key-username}") String username,
            @Value("${service.security.secure-key-password}") String password) {
        return new BasicAuthRequestInterceptor(username, password);
    }

    /**
     * Bean para el interceptor que propaga el ID y Roles del usuario final.
     * Se inyecta la instancia de UserContextRequestInterceptor gestionada por Spring (gracias al @Component).
     */
    @Bean
    public RequestInterceptor userContextRequestInterceptor() {

        return new UserContextRequestInterceptor();
    }
}