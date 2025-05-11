package com.dani.spring_boot_microservice_3_api_gateway.security;

import com.dani.spring_boot_microservice_3_api_gateway.model.Role;
import com.dani.spring_boot_microservice_3_api_gateway.security.jwt.JwtAuthorizationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer; // Import para Customizer
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configuración principal de Spring Security para el API Gateway.
 * Define el AuthenticationManager, la cadena de filtros de seguridad,
 * las reglas de autorización para los diferentes endpoints (incluyendo UI y API) y la configuración CORS.
 * Integra el filtro de autorización JWT y el login basado en formularios.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Expone el AuthenticationManager como un Bean.
     * Necesario para el proceso de autenticación manual en AuthenticationService
     * y también utilizado internamente por Spring Security para formLogin.
     * @param authenticationConfiguration La configuración de autenticación de Spring.
     * @return El AuthenticationManager configurado.
     * @throws Exception Si ocurre un error al obtener el AuthenticationManager.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    /**
     * Define la cadena de filtros de seguridad principal para la aplicación.
     * Configura CORS, CSRF, gestión de sesión, reglas de autorización,
     * el filtro JWT y el login basado en formularios.
     * @param http El objeto HttpSecurity para configurar la seguridad web.
     * @return La cadena de filtros de seguridad (SecurityFilterChain) construida.
     * @throws Exception Si ocurre un error durante la configuración.
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        // Configura el AuthenticationManagerBuilder para usar el CustomUserDetailsService y PasswordEncoder
        AuthenticationManagerBuilder authBuilder = http.getSharedObject(AuthenticationManagerBuilder.class);
        authBuilder.userDetailsService(customUserDetailsService).passwordEncoder(passwordEncoder);

        http
                // Configuración CORS: Usa la configuración del bean corsConfigurer
                .cors(Customizer.withDefaults())

                // Deshabilitar CSRF (común para APIs y si se usa JWT)
                .csrf(AbstractHttpConfigurer::disable)

                // Gestión de Sesión: Stateless, ya que primariamente usaremos JWT para APIs.
                // FormLogin puede crear una sesión temporalmente si es necesario para la UI.
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED) // Crea una sesión solo si es necesario (ej. para formLogin)
                        // .sessionCreationPolicy(SessionCreationPolicy.STATELESS) // Alternativa: sin sesiones, solo JWT.
                        // .sessionManagement(sessionManagementConfigurer -> sessionManagementConfigurer.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                )

                // Reglas de Autorización para las peticiones HTTP
                .authorizeHttpRequests(authorize -> authorize
                        // Endpoints públicos: página de login, futura página de registro UI, y endpoints de autenticación API.
                        .requestMatchers("/login", "/api/authentication/sign-up-page").permitAll()
                        .requestMatchers("/api/authentication/sign-in", "/api/authentication/sign-up").permitAll()

                        // Endpoints de la UI (ej. /ui/dashboard) - requieren autenticación.
                        .requestMatchers("/ui/**").authenticated()
                        // (Opcional: Si tienes recursos estáticos para la UI como CSS, JS fuera de /ui/** y necesitan ser públicos)
                        // .requestMatchers("/css/**", "/js/**", "/images/**").permitAll()

                        // Endpoints API Gateway (proxy a otros servicios)
                        // Permitir GET a /gateway/inmueble para todos (ej. listar inmuebles públicamente)
                        .requestMatchers(HttpMethod.GET, "/gateway/inmueble").permitAll()
                        // Otras operaciones sobre /gateway/inmueble/** requieren rol ADMIN.
                        .requestMatchers("/gateway/inmueble/**").hasRole(Role.ADMIN.name())
                        // Todas las demás peticiones (endpoints de API no cubiertos antes) requieren autenticación.
                        // Es importante que esta regla más general vaya después de las más específicas.
                        .anyRequest().authenticated()
                )

                // Configuración del Login Basado en Formularios (para la UI)
                .formLogin(formLogin -> formLogin
                        .loginPage("/login") // Especifica la URL de tu página de login personalizada.
                        .loginProcessingUrl("/api/authentication/sign-in") // Endpoint que procesa las credenciales del formulario.
                        // Spring Security lo maneja. Coincide con el th:action del form.
                        .defaultSuccessUrl("/ui/dashboard", true) // Redirige a esta URL si el login es exitoso.
                        // El 'true' fuerza esta redirección.
                        .failureUrl("/login?error=true") // Redirige aquí si el login falla (ej. credenciales incorrectas).
                        // El parámetro 'error' puede ser usado en la plantilla Thymeleaf.
                        .permitAll() // Permite a todos acceder a la página de login y al endpoint de procesamiento.
                )

                // Configuración de Logout (para la UI)
                .logout(logout -> logout
                        .logoutUrl("/logout") // URL para procesar la petición de cierre de sesión.
                        .logoutSuccessUrl("/login?logout=true") // Redirige aquí después de un logout exitoso.
                        // El parámetro 'logout' puede ser usado en Thymeleaf.
                        .invalidateHttpSession(true) // Invalida la sesión HTTP.
                        .deleteCookies("JSESSIONID") // Elimina cookies de sesión (si se usan).
                        .permitAll() // Permite a todos acceder a la URL de logout.
                );

        // Añadir el filtro de autorización JWT.
        // Este filtro se ejecutará antes del UsernamePasswordAuthenticationFilter.
        // Verificará la presencia de un token JWT en las peticiones (especialmente para APIs).
        // Si formLogin tiene éxito, el contexto de seguridad ya estará poblado para la sesión/petición UI.
        // El JWT es clave para las llamadas subsecuentes a la API que sean stateless.
        http.addFilterBefore(jwtAuthorizationFilter(), UsernamePasswordAuthenticationFilter.class);

        // Construye y retorna la cadena de filtros de seguridad.
        return http.build();
    }

    /**
     * Crea una instancia del filtro de autorización JWT.
     * Este filtro es responsable de validar los tokens JWT en las cabeceras
     * de las peticiones entrantes y establecer la autenticación en el contexto de seguridad.
     * @return una instancia de JwtAuthorizationFilter.
     */
    @Bean
    public JwtAuthorizationFilter jwtAuthorizationFilter() {
        // Es importante que JwtAuthorizationFilter tenga su dependencia JwtProvider correctamente inyectada.
        // Esto sucede porque JwtAuthorizationFilter está marcado como un bean y JwtProvider también lo es.
        return new JwtAuthorizationFilter();
    }

    /**
     * Configura CORS (Cross-Origin Resource Sharing) globalmente para la aplicación.
     * Permite peticiones desde cualquier origen ("*") a todos los endpoints.
     * NOTA: En producción, es altamente recomendable restringir `allowedOrigins`
     * a los dominios específicos que deban tener acceso.
     * @return Un configurador WebMvcConfigurer con las reglas CORS definidas.
     */
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**") // Aplica a todos los paths de la aplicación.
                        .allowedOrigins("*") // Permite peticiones de cualquier origen.
                // Opcionalmente, puedes configurar allowedMethods, allowedHeaders, etc.
                // .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                // .allowedHeaders("*")
                // .allowCredentials(true)
                ;
            }
        };
    }
}