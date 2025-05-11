package com.dani.spring_boot_microservice_2_compra.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer; // Importar para httpBasic(Customizer.withDefaults())
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer; // Importar para csrf(AbstractHttpConfigurer::disable)
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder; // Importar PasswordEncoder
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configuración de Seguridad para el microservicio de Compras.
 * Habilita la seguridad web y define las reglas de autenticación (en memoria) y autorización.
 * Utiliza Basic Auth para proteger los endpoints.
 */
@Configuration // Marca esta clase como contenedora de configuraciones de beans.
@EnableWebSecurity // Habilita la seguridad web de Spring Security.
public class SecurityConfig {

    // Inyecta credenciales desde application.properties para el usuario en memoria.
    @Value("${service.security.secure-key-username}")
    private String SECURE_KEY_USERNAME;

    @Value("${service.security.secure-key-password}")
    private String SECURE_KEY_PASSWORD;

    /**
     * Define el bean para el codificador de contraseñas.
     * Es importante usar un codificador fuerte como BCrypt.
     * @return Una instancia de PasswordEncoder (BCryptPasswordEncoder).
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Configura la cadena principal de filtros de seguridad HTTP.
     * Define qué peticiones requieren autenticación/autorización y cómo se realiza.
     *
     * @param http El objeto HttpSecurity para configurar la seguridad.
     * @param passwordEncoder El codificador de contraseñas inyectado.
     * @return La cadena de filtros de seguridad configurada (SecurityFilterChain).
     * @throws Exception Si ocurre un error durante la configuración.
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, PasswordEncoder passwordEncoder) throws Exception {
        // Obtiene el constructor del AuthenticationManager para definir usuarios.
        AuthenticationManagerBuilder authenticationManagerBuilder = http.getSharedObject(
                AuthenticationManagerBuilder.class);

        // Configura un usuario en memoria para la autenticación básica entre servicios.
        authenticationManagerBuilder.inMemoryAuthentication()
                .withUser(SECURE_KEY_USERNAME)
                .password(passwordEncoder.encode(SECURE_KEY_PASSWORD)) // Codifica la contraseña
                .authorities(AuthorityUtils.commaSeparatedStringToAuthorityList("ROLE_ADMIN")) // Asigna rol
                .and()
                .passwordEncoder(passwordEncoder); // Asocia el codificador

        // Configuración de las reglas de autorización y filtros.
        // NOTA: Se usa securityMatcher en lugar del antiguo antMatcher.
        // NOTA: Se usa authorizeHttpRequests en lugar del antiguo authorizeRequests.
        // NOTA: Se usa la sintaxis lambda para configurar csrf y httpBasic.
        http
                .securityMatcher("/**") // Aplica esta configuración a todas las rutas ("/**").
                .authorizeHttpRequests(authorize -> authorize
                        .anyRequest().hasRole("ADMIN") // Requiere que cualquier petición tenga el rol ADMIN.
                )
                .csrf(AbstractHttpConfigurer::disable) // Deshabilita la protección CSRF (común para APIs stateless).
                .httpBasic(Customizer.withDefaults()); // Habilita la autenticación HTTP Basic.

        // Construye y devuelve la cadena de filtros.
        return http.build();
    }

    /**
     * Configura CORS (Cross-Origin Resource Sharing) para permitir peticiones
     * desde cualquier origen a todos los endpoints de esta API.
     *
     * @return Un configurador WebMvcConfigurer con las reglas CORS.
     */
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**") // Aplica a todos los paths.
                        .allowedOrigins("*"); // Permite cualquier origen.
                // NOTA: En producción, restringir allowedOrigins a los dominios necesarios.
            }
        };
    }
}