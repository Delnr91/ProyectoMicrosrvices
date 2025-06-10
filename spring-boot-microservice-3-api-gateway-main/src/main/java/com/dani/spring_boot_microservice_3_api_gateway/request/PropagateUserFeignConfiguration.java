package com.dani.spring_boot_microservice_3_api_gateway.request;

import feign.RequestInterceptor;
import feign.auth.BasicAuthRequestInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración personalizada para los clientes Feign.
 * <p>
 * Esta clase define beans para {@link RequestInterceptor} que se aplican
 * a las peticiones salientes realizadas por los clientes Feign.
 * Actualmente, configura dos interceptores principales:
 * <ol>
 * <li>Un {@link BasicAuthRequestInterceptor} para añadir autenticación básica
 * a las llamadas entre el API Gateway y los microservicios internos.
 * Las credenciales para esta autenticación se cargan desde las propiedades de la aplicación.</li>
 * <li>Un {@link UserContextRequestInterceptor} personalizado para propagar la identidad
 * del usuario final (ID y roles) a los servicios downstream a través de cabeceras HTTP.</li>
 * </ol>
 * Esta configuración se aplica a los clientes Feign que la referencian explícitamente
 * en su anotación {@code @FeignClient(configuration = PropagateUserFeignConfiguration.class)}.
 *
 * @see FeignClient Anotación para declarar un cliente Feign.
 * @see BasicAuthRequestInterceptor Interceptor estándar de Feign para autenticación básica.
 * @see UserContextRequestInterceptor Interceptor personalizado para propagar el contexto del usuario.
 * @author Daniel Núñez Rojas (danidev fullstack software)
 * @version 1.0
 * @since 2025-05-13 (Fecha de creación o última modificación significativa)
 */
@Configuration
public class PropagateUserFeignConfiguration {

    /**
     * Define un bean para {@link BasicAuthRequestInterceptor}.
     * <p>
     * Este interceptor añade automáticamente un header de "Authorization" con
     * credenciales de autenticación básica (username y password) a cada petición
     * realizada por el cliente Feign.
     * <p>
     * El nombre de usuario y la contraseña se inyectan desde las propiedades de la aplicación
     * ({@code service.security.secure-key-username} y {@code service.security.secure-key-password}),
     * permitiendo una comunicación segura entre el API Gateway y los microservicios internos
     * que esperan este tipo de autenticación.
     *
     * @param username El nombre de usuario para la autenticación básica, inyectado desde propiedades.
     * @param password La contraseña para la autenticación básica, inyectada desde propiedades.
     * @return Una instancia de {@link BasicAuthRequestInterceptor} configurada con las credenciales proporcionadas.
     */
    @Bean
    public BasicAuthRequestInterceptor basicAuthRequestInterceptor(
            @Value("${service.security.secure-key-username}") String username,
            @Value("${service.security.secure-key-password}") String password) {
        return new BasicAuthRequestInterceptor(username, password);
    }

    /**
     * Define un bean para {@link UserContextRequestInterceptor}.
     * <p>
     * Este interceptor personalizado se encarga de extraer la información del usuario
     * autenticado actualmente en el API Gateway (ID y roles) del {@code SecurityContextHolder}
     * y añadirla como cabeceras HTTP ({@code X-User-ID} y {@code X-User-Roles})
     * a las peticiones Feign salientes.
     * <p>
     * Esto permite a los microservicios downstream conocer la identidad del usuario
     * original que inició la petición, lo cual es crucial para la auditoría,
     * la personalización y la lógica de permisos detallada en esos servicios.
     *
     * @return Una nueva instancia de {@link UserContextRequestInterceptor}.
     */
    @Bean
    public RequestInterceptor userContextRequestInterceptor() {
        return new UserContextRequestInterceptor();
    }
}