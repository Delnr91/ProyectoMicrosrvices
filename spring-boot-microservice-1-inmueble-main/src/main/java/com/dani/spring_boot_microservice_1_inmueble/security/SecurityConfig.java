package com.dani.spring_boot_microservice_1_inmueble.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer; // Importado para httpBasic(Customizer.withDefaults())
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer; // Importado para csrf(AbstractHttpConfigurer::disable)
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder; // Importar PasswordEncoder
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configuración de Seguridad para el microservicio de Inmuebles.
 * Habilita la seguridad web y define las reglas de autenticación y autorización.
 * Utiliza autenticación en memoria y Basic Auth para proteger los endpoints.
 */
@Configuration // Indica que esta clase contiene configuraciones de beans de Spring.
@EnableWebSecurity // Habilita la integración de Spring Security con Spring MVC.
public class SecurityConfig {

    // Inyecta valores desde application.properties para las credenciales de seguridad.
    @Value("${service.security.secure-key-username}")
    private String SECURE_KEY_USERNAME;
    @Value("${service.security.secure-key-password}")
    private String SECURE_KEY_PASSWORD;
    @Value("${service.security.secure-key-username-2}")
    private String SECURE_KEY_USERNAME_2;
    @Value("${service.security.secure-key-password-2}")
    private String SECURE_KEY_PASSWORD_2;

    /**
     * Bean que define el codificador de contraseñas a utilizar.
     * BCrypt es el estándar recomendado actualmente.
     *
     * @return Una instancia de BCryptPasswordEncoder.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Configura la cadena de filtros de seguridad principal.
     * Define cómo se manejan la autenticación y la autorización para las peticiones HTTP.
     *
     * @param http El objeto HttpSecurity para configurar la seguridad web.
     * @param passwordEncoder El codificador de contraseñas inyectado.
     * @return La cadena de filtros de seguridad configurada.
     * @throws Exception Si ocurre un error durante la configuración.
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, PasswordEncoder passwordEncoder) throws Exception {
        // Obtiene el constructor del AuthenticationManager para configurar usuarios en memoria.
        AuthenticationManagerBuilder authenticationManagerBuilder = http.getSharedObject(
                AuthenticationManagerBuilder.class);

        // Configura usuarios en memoria con roles y contraseñas codificadas.
        authenticationManagerBuilder.inMemoryAuthentication()
                .withUser(SECURE_KEY_USERNAME)
                .password(passwordEncoder.encode(SECURE_KEY_PASSWORD)) // Usa el PasswordEncoder del bean
                .authorities(AuthorityUtils.commaSeparatedStringToAuthorityList("ROLE_ADMIN"))
                .and()
                .withUser(SECURE_KEY_USERNAME_2)
                .password(passwordEncoder.encode(SECURE_KEY_PASSWORD_2)) // Usa el PasswordEncoder del bean
                .authorities(AuthorityUtils.commaSeparatedStringToAuthorityList("ROLE_DEV"))
                .and()
                // El passwordEncoder ya está asociado a través del bean, no es necesario aquí explícitamente
                // si se configura globalmente como hicimos, pero se puede dejar por claridad.
                .passwordEncoder(passwordEncoder);


        // Configuración de las reglas de autorización HTTP.
        // NOTA: Se usa securityMatcher en lugar del antiguo antMatcher a nivel de HttpSecurity.
        // NOTA: Se usa authorizeHttpRequests en lugar del antiguo authorizeRequests.
        // NOTA: Se usa la expresión lambda para configurar CSRF y HTTP Basic.
        http
                .securityMatcher("/**") // Aplica esta configuración a todas las rutas.
                .authorizeHttpRequests(authorize -> authorize
                        .anyRequest().hasRole("ADMIN") // Requiere rol ADMIN para cualquier petición.
                )
                .csrf(AbstractHttpConfigurer::disable) // Deshabilita CSRF (común en APIs stateless).
                .httpBasic(Customizer.withDefaults()); // Habilita autenticación HTTP Basic con configuración por defecto.

        // Construye y retorna la cadena de filtros de seguridad.
        return http.build();
    }

    /**
     * Configura las reglas de CORS (Cross-Origin Resource Sharing) para la aplicación.
     * Permite peticiones desde cualquier origen ("*").
     *
     * @return Un configurador WebMvcConfigurer con las reglas CORS.
     */
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                // Permite el acceso desde cualquier origen a todos los endpoints ("/**").
                registry.addMapping("/**").allowedOrigins("*");
                // NOTA: En producción, es recomendable restringir allowedOrigins a dominios específicos.
            }
        };
    }
}