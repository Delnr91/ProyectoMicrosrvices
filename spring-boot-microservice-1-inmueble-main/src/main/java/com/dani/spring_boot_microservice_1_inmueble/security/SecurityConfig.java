package com.dani.spring_boot_microservice_1_inmueble.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Clase de configuración para Spring Security en el microservicio de Inmuebles.
 * <p>
 * Al igual que en el {@code compra-service}, esta configuración define la política de
 * seguridad para un servicio interno que es consumido por otros servicios
 * (principalmente el API Gateway y el compra-service). No maneja usuarios finales
 * directamente, sino que asegura la comunicación de servicio a servicio.
 * <p>
 * Configura:
 * <ul>
 * <li>Una política de seguridad stateless.</li>
 * <li>Autenticación Básica (Basic Auth) para todas las peticiones a la API.</li>
 * <li>Dos usuarios en memoria para validar las credenciales de Basic Auth. Esto permite
 * que tanto el API Gateway como el compra-service se autentiquen con credenciales diferentes
 * si fuera necesario, mejorando la granularidad de la seguridad.</li>
 * <li>Desactivación de CSRF.</li>
 * </ul>
 *
 * @author Daniel Núñez Rojas (danidev fullstack software)
 * @version 1.0
 * @since 2025-05-18
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    // Credenciales para el primer usuario de servicio (ej. API Gateway)
    @Value("${service.security.secure-key-username}")
    private String SECURE_KEY_USERNAME;
    @Value("${service.security.secure-key-password}")
    private String SECURE_KEY_PASSWORD;

    // Credenciales para el segundo usuario de servicio (ej. compra-service)
    @Value("${service.security.secure-key-username-2}")
    private String SECURE_KEY_USERNAME_2;
    @Value("${service.security.secure-key-password-2}")
    private String SECURE_KEY_PASSWORD_2;

    /**
     * Define y expone el {@link PasswordEncoder} como un bean.
     * @return una instancia de {@link BCryptPasswordEncoder}.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Configura un {@link UserDetailsService} con dos usuarios de servicio en memoria.
     * <p>
     * Estos usuarios son utilizados por otros microservicios para autenticarse con
     * este servicio a través de Autenticación Básica.
     *
     * @param passwordEncoder el codificador para hashear las contraseñas de los usuarios en memoria.
     * @return un {@link UserDetailsService} configurado con los usuarios de servicio.
     */
    @Bean
    public UserDetailsService userDetailsService(PasswordEncoder passwordEncoder) {
        UserDetails user = User.withUsername(SECURE_KEY_USERNAME)
                .password(passwordEncoder.encode(SECURE_KEY_PASSWORD))
                .roles("USER")
                .build();

        UserDetails user2 = User.withUsername(SECURE_KEY_USERNAME_2)
                .password(passwordEncoder.encode(SECURE_KEY_PASSWORD_2))
                .roles("USER")
                .build();
        return new InMemoryUserDetailsManager(user, user2);
    }

    /**
     * Define la cadena de filtros de seguridad principal para el microservicio de inmuebles.
     * <p>
     * Configura la seguridad para que todas las peticiones a {@code /api/**}
     * requieran autenticación y utilicen Autenticación Básica (HTTP Basic).
     *
     * @param http El objeto {@link HttpSecurity} para configurar la seguridad web.
     * @return La cadena de filtros de seguridad configurada.
     * @throws Exception si ocurre un error durante la configuración.
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http.csrf((csrf) -> csrf.disable())
                .authorizeHttpRequests(authRequest ->
                        authRequest.requestMatchers("/api/**").authenticated()
                                .anyRequest().permitAll()
                )
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .httpBasic(Customizer.withDefaults())
                .build();
    }
}