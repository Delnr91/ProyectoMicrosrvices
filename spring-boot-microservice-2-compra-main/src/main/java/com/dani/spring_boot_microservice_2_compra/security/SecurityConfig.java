package com.dani.spring_boot_microservice_2_compra.security;

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
 * Clase de configuración para Spring Security en el microservicio de Compras.
 * <p>
 * Esta configuración define la política de seguridad para este servicio, que es
 * un servicio interno consumido por el API Gateway. No maneja usuarios finales
 * directamente, sino que asegura la comunicación de servicio a servicio.
 * <p>
 * Configura:
 * <ul>
 * <li>Una política de seguridad stateless, ya que no mantiene sesiones de usuario.</li>
 * <li>Autenticación Básica (Basic Auth) para todas las peticiones a la API.</li>
 * <li>Un único usuario en memoria para validar las credenciales de Basic Auth
 * enviadas por los servicios llamadores (como el API Gateway).</li>
 * <li>Desactivación de CSRF, lo cual es común para APIs no basadas en navegador.</li>
 * </ul>
 *
 * @author Daniel Núñez Rojas (danidev fullstack software)
 * @version 1.1
 * @since 2025-06-09 (Actualizado con JavaDoc completo)
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Value("${service.security.secure-key-username}")
    private String SECURE_KEY_USERNAME;

    @Value("${service.security.secure-key-password}")
    private String SECURE_KEY_PASSWORD;

    /**
     * Define y expone el {@link PasswordEncoder} como un bean.
     * Se utiliza la implementación {@link BCryptPasswordEncoder}, que es el estándar recomendado.
     *
     * @return una instancia de {@link BCryptPasswordEncoder}.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Configura un {@link UserDetailsService} con un único usuario en memoria.
     * <p>
     * Este usuario no representa a un usuario final, sino a un "usuario de servicio"
     * cuyas credenciales son utilizadas por otros microservicios (como el API Gateway)
     * para autenticarse con este servicio a través de Autenticación Básica.
     * Las credenciales se cargan desde las propiedades de la aplicación para mayor seguridad.
     *
     * @param passwordEncoder el codificador de contraseñas para hashear la contraseña del usuario en memoria.
     * @return un {@link UserDetailsService} configurado con el usuario de servicio.
     */
    @Bean
    public UserDetailsService userDetailsService(PasswordEncoder passwordEncoder) {
        UserDetails user = User.withUsername(SECURE_KEY_USERNAME)
                .password(passwordEncoder.encode(SECURE_KEY_PASSWORD))
                .roles("USER") // Rol básico suficiente para comunicación interna
                .build();
        return new InMemoryUserDetailsManager(user);
    }

    /**
     * Define la cadena de filtros de seguridad principal para el microservicio.
     * <p>
     * Configura la seguridad para que todas las peticiones a {@code /api/**}
     * requieran autenticación y utilicen Autenticación Básica (HTTP Basic).
     * La gestión de sesiones se establece como stateless, ya que este servicio
     * no necesita mantener sesiones de usuario.
     *
     * @param http El objeto {@link HttpSecurity} para configurar la seguridad web.
     * @return La cadena de filtros de seguridad configurada.
     * @throws Exception si ocurre un error durante la configuración.
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http.csrf((csrf) -> csrf.disable()) // Deshabilitar CSRF
                .authorizeHttpRequests(authRequest ->
                        authRequest.requestMatchers("/api/**").authenticated() // Proteger todos los endpoints de la API
                                .anyRequest().permitAll() // Permitir otras rutas (ej. Actuator, si se añade)
                )
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .httpBasic(Customizer.withDefaults())
                .build();
    }
}