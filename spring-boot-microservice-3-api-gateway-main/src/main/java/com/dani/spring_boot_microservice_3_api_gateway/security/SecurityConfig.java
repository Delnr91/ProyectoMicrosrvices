package com.dani.spring_boot_microservice_3_api_gateway.security;

import com.dani.spring_boot_microservice_3_api_gateway.model.Role;
import com.dani.spring_boot_microservice_3_api_gateway.security.jwt.JwtAuthorizationFilter;
import com.dani.spring_boot_microservice_3_api_gateway.security.jwt.JwtProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;


/**
 * Clase de configuración principal para Spring Security en el API Gateway.
 * <p>
 * Habilita la seguridad web ({@link EnableWebSecurity}) y la seguridad a nivel de método
 * ({@link EnableMethodSecurity}) para un control de acceso granular.
 * Define la cadena de filtros de seguridad, el gestor de autenticación,
 * el codificador de contraseñas, y las políticas de CORS.
 * <p>
 * Configura:
 * <ul>
 * <li>Autenticación basada en {@link CustomUserDetailsService} y {@link PasswordEncoder}.</li>
 * <li>Autorización para diferentes rutas HTTP, distinguiendo entre rutas públicas,
 * rutas que requieren autenticación y rutas que requieren roles específicos (ej. ADMIN).</li>
 * <li>Gestión de sesiones (actualmente {@link SessionCreationPolicy#IF_REQUIRED}).</li>
 * <li>Integración de un filtro personalizado {@link JwtAuthorizationFilter} para procesar tokens JWT.</li>
 * <li>Configuración para el formulario de inicio de sesión (login) y el proceso de cierre de sesión (logout).</li>
 * <li>Política global de CORS.</li>
 * </ul>
 *
 * @see EnableWebSecurity
 * @see EnableMethodSecurity
 * @see CustomUserDetailsService
 * @see JwtAuthorizationFilter
 * @author Daniel Núñez Rojas (danidev fullstack software)
 * @version 1.1
 * @since 2025-05-13 (Refactorización para UI y seguridad JWT)
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    /**
     * Servicio personalizado para cargar detalles del usuario desde la base de datos.
     * Utilizado por el {@link AuthenticationManagerBuilder} para la autenticación.
     */
    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    /**
     * Codificador de contraseñas utilizado para verificar las contraseñas durante la autenticación
     * y para codificar nuevas contraseñas antes de almacenarlas.
     * El bean para este PasswordEncoder se define en {@link com.dani.spring_boot_microservice_3_api_gateway.SpringBootMicroservice3ApiGatewayApplication}.
     */
    @Autowired
    private PasswordEncoder passwordEncoder;

    // Rutas públicas generales de la aplicación (UI y recursos estáticos)
    private static final String[] PUBLIC_UI_PATHS = {
            "/", "/index", "/home",
            "/css/**", "/js/**", "/images/**",
            "/login", "/ui/authentication/sign-up", "/ui/authentication/perform-sign-up",
            "/ui/catalogo", "/ui/catalogo/detalle/**"
    };

    // Rutas públicas de la API (autenticación y endpoints públicos del gateway)
    private static final String[] PUBLIC_API_PATHS = {
            "/api/authentication/sign-in",
            "/api/authentication/sign-up"
    };

    /**
     * Expone el {@link AuthenticationManager} como un bean para que pueda ser utilizado
     * en otras partes de la aplicación (por ejemplo, en el servicio de autenticación
     * para procesar el inicio de sesión).
     *
     * @param authenticationConfiguration La configuración de autenticación de Spring.
     * @return El {@link AuthenticationManager} configurado.
     * @throws Exception Si ocurre un error al obtener el AuthenticationManager.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    /**
     * Define la cadena de filtros de seguridad principal ({@link SecurityFilterChain}) que
     * se aplica a todas las peticiones HTTP que llegan a la aplicación.
     * <p>
     * Configura aspectos como:
     * <ul>
     * <li>Política CORS.</li>
     * <li>Desactivación de CSRF (común para APIs que usan tokens).</li>
     * <li>Gestión de sesiones ({@link SessionCreationPolicy#IF_REQUIRED} para permitir tanto UI basada en sesión como APIs JWT).</li>
     * <li>Reglas de autorización para rutas específicas ({@code authorizeHttpRequests}).</li>
     * <li>Configuración del formulario de login ({@code formLogin}).</li>
     * <li>Configuración del logout ({@code logout}).</li>
     * <li>Inclusión del filtro personalizado {@link JwtAuthorizationFilter} en la cadena.</li>
     * </ul>
     * Las reglas de autorización se definen de más específicas (permitAll para rutas públicas)
     * a más generales (anyRequest().authenticated()).
     *
     * @param http El objeto {@link HttpSecurity} para configurar la seguridad web.
     * @return La cadena de filtros de seguridad configurada.
     * @throws Exception Si ocurre un error durante la configuración.
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        // Configura el AuthenticationManagerBuilder para usar el CustomUserDetailsService y PasswordEncoder
        AuthenticationManagerBuilder authBuilder = http.getSharedObject(AuthenticationManagerBuilder.class);
        authBuilder.userDetailsService(customUserDetailsService).passwordEncoder(passwordEncoder);

        http
                .cors(Customizer.withDefaults()) // Aplica la configuración CORS definida en el bean corsConfigurer
                .csrf(AbstractHttpConfigurer::disable) // Deshabilita CSRF, adecuado para APIs stateless o cuando se usan tokens
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED) // Permite sesiones para la UI, pero las APIs pueden ser stateless con JWT
                )
                .authorizeHttpRequests(authorize -> authorize
                        // --- Rutas Públicas (permitAll DEBEN IR PRIMERO) ---
                        .requestMatchers(PUBLIC_UI_PATHS).permitAll()
                        .requestMatchers(PUBLIC_API_PATHS).permitAll()
                        .requestMatchers(HttpMethod.GET, "/gateway/inmueble").permitAll() // Listar inmuebles públicamente

                        // --- Rutas que requieren autenticación general ---
                        .requestMatchers(HttpMethod.POST, "/gateway/inmueble").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/gateway/inmueble/**").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/gateway/inmueble/**").authenticated()
                        .requestMatchers(HttpMethod.POST, "/gateway/compra").authenticated()
                        .requestMatchers(HttpMethod.GET, "/gateway/compra/api/mis-compras").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/api/user/change/**").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/user").authenticated()

                        // --- Rutas de UI que requieren autenticación ---
                        .requestMatchers("/ui/dashboard").authenticated()
                        .requestMatchers("/ui/inmuebles/**").authenticated()
                        .requestMatchers("/ui/mis-compras/**").authenticated()

                        // --- Rutas que requieren rol ADMIN ---
                        .requestMatchers("/ui/admin/**").hasRole(Role.ADMIN.name())
                        .requestMatchers("/api/user/admin/**").hasRole(Role.ADMIN.name())

                        // --- Regla General (Cualquier otra petición requiere autenticación) ---
                        .anyRequest().authenticated()
                )
                .formLogin(formLogin -> formLogin // Configuración para el login basado en formulario
                        .loginPage("/login")                               // URL de la página de login personalizada
                        .loginProcessingUrl("/api/authentication/sign-in") // URL donde Spring Security procesa las credenciales del formulario (POST)
                        .defaultSuccessUrl("/ui/dashboard", true)       // URL a la que redirigir tras un login exitoso
                        .failureUrl("/login?error=true")                   // URL a la que redirigir si el login falla
                        .permitAll()                                       // Permite el acceso a estas URLs de login
                )
                .logout(logout -> logout // Configuración para el logout
                        .logoutRequestMatcher(new AntPathRequestMatcher("/logout", "GET")) // URL para activar el logout (puede ser POST también)
                        .logoutSuccessUrl("/login?logout=true")            // URL a la que redirigir tras un logout exitoso
                        .invalidateHttpSession(true)                       // Invalida la sesión HTTP
                        .deleteCookies("JSESSIONID", "remember-me")        // Elimina cookies específicas
                        .clearAuthentication(true)                       // Limpia la información de autenticación
                        .permitAll()                                       // Permite el acceso a la URL de logout
                )
                // Añade el filtro JwtAuthorizationFilter ANTES del UsernamePasswordAuthenticationFilter estándar.
                // Esto asegura que si hay un token JWT válido, la autenticación se establezca a partir de él.
                .addFilterBefore(jwtAuthorizationFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Define un bean para el {@link JwtAuthorizationFilter}.
     * Este filtro se encarga de procesar los tokens JWT en las peticiones entrantes.
     * Se crea como un bean para que Spring pueda inyectar dependencias en él (como {@link JwtProvider}).
     *
     * @return una instancia de {@link JwtAuthorizationFilter}.
     */
    @Bean
    public JwtAuthorizationFilter jwtAuthorizationFilter() {
        return new JwtAuthorizationFilter();
    }

    /**
     * Configura las reglas globales de CORS (Cross-Origin Resource Sharing) para la aplicación.
     * Permite peticiones desde cualquier origen ("*") a todos los endpoints ("/**").
     * <p>
     * **Nota de Seguridad:** En entornos de producción, es altamente recomendable restringir
     * {@code allowedOrigins} a los dominios específicos que deben tener permiso para
     * interactuar con la API, en lugar de usar "*".
     *
     * @return Un {@link WebMvcConfigurer} con las reglas CORS definidas.
     */
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**") // Aplica a todas las rutas
                        .allowedOrigins("*"); // Permite peticiones desde cualquier origen
            }
        };
    }
}