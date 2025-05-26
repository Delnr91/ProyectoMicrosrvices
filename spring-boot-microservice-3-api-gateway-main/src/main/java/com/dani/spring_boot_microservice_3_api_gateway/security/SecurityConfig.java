package com.dani.spring_boot_microservice_3_api_gateway.security;

import com.dani.spring_boot_microservice_3_api_gateway.model.Role;
import com.dani.spring_boot_microservice_3_api_gateway.security.jwt.JwtAuthorizationFilter;
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
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        AuthenticationManagerBuilder authBuilder = http.getSharedObject(AuthenticationManagerBuilder.class);
        authBuilder.userDetailsService(customUserDetailsService).passwordEncoder(passwordEncoder);

        http
                .cors(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                )
                .authorizeHttpRequests(authorize -> authorize
                        // --- Endpoints Públicos ---
                        .requestMatchers("/", "/index", "/home").permitAll() // Página de inicio
                        .requestMatchers("/css/**", "/js/**", "/images/**").permitAll() // Recursos estáticos
                        .requestMatchers("/login", "/ui/authentication/sign-up", "/ui/authentication/perform-sign-up").permitAll() // UI de Login/Registro
                        .requestMatchers("/api/authentication/sign-in", "/api/authentication/sign-up").permitAll() // API de Autenticación

                        // --- Catálogo Público (UI) ---
                        .requestMatchers("/ui/catalogo", "/ui/catalogo/detalle/**").permitAll()

                        // --- UI Autenticada ---
                        // Cualquier otra cosa bajo /ui/ (dashboard, gestión de inmuebles del usuario, etc.) requiere autenticación.
                        .requestMatchers("/ui/dashboard", "/ui/inmuebles/**", "/ui/mis-compras/**").authenticated()
                        // La sección de admin UI ya está protegida por @PreAuthorize("hasRole('ADMIN')") a nivel de controlador,
                        // pero una capa adicional aquí es buena práctica.
                        .requestMatchers("/ui/admin/**").hasRole(Role.ADMIN.name())

                        // --- Endpoints del Gateway que actúan como Proxy (Prefijo /gateway/) ---
                        // Estos son los endpoints que el API Gateway expone y que internamente llaman a otros microservicios.
                        // La seguridad aquí es para el acceso DIRECTO a estos endpoints del Gateway.
                        // Las llamadas Feign desde los UIControllers a los ServiceRequest no pasan directamente por aquí,
                        // sino que usan su propia config de seguridad (Basic Auth + propagación X-User-ID).

                        // InmuebleService via Gateway:
                        // GET público para listar todos los inmuebles (usado por el catálogo público en el ServiceRequest)
                        // o si se quiere un API pública para ello.
                        .requestMatchers(HttpMethod.GET, "/gateway/inmueble").permitAll()
                        // Operaciones de escritura/modificación de inmuebles vía API del Gateway (si existen) usualmente protegidas.
                        // Ejemplo: Si el InmuebleController del Gateway tuviera un POST para crear:
                        .requestMatchers(HttpMethod.POST, "/gateway/inmueble").hasRole(Role.ADMIN.name()) // Solo ADMIN puede crear inmuebles vía API del gateway
                        .requestMatchers(HttpMethod.PUT, "/gateway/inmueble/**").hasRole(Role.ADMIN.name()) // Solo ADMIN puede actualizar
                        .requestMatchers(HttpMethod.DELETE, "/gateway/inmueble/**").hasRole(Role.ADMIN.name()) // Solo ADMIN puede borrar

                        // CompraService via Gateway:
                        // El endpoint POST /gateway/compra es llamado por el formulario de la UI de detalle del inmueble.
                        // Este SÍ necesita estar autenticado porque un usuario debe estar logueado para comprar.
                        .requestMatchers(HttpMethod.POST, "/gateway/compra").authenticated()
                        // El endpoint GET /gateway/compra/api/mis-compras es para que un usuario autenticado vea sus compras vía API.
                        .requestMatchers(HttpMethod.GET, "/gateway/compra/api/mis-compras").authenticated()
                        // Si hubiera un endpoint para que un admin vea todas las compras del sistema vía API del Gateway:
                        // .requestMatchers(HttpMethod.GET, "/gateway/compra/admin/all").hasRole(Role.ADMIN.name())


                        // --- API de Gestión de Usuarios (Prefijo /api/user/) ---
                        // Estos son los endpoints del UserController del API Gateway.
                        .requestMatchers(HttpMethod.PUT, "/api/user/change/**").authenticated() // Cambiar propio rol
                        .requestMatchers(HttpMethod.GET, "/api/user").authenticated()          // Obtener datos del usuario actual
                        .requestMatchers("/api/user/admin/**").hasRole(Role.ADMIN.name())   // Endpoints de admin para gestión de usuarios

                        // --- Regla General ---
                        // Cualquier otra petición no especificada anteriormente requiere autenticación.
                        .anyRequest().authenticated()
                )
                .formLogin(formLogin -> formLogin
                        .loginPage("/login")
                        .loginProcessingUrl("/api/authentication/sign-in")
                        .defaultSuccessUrl("/ui/dashboard", true)
                        .failureUrl("/login?error=true")
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/?logout=true") // Redirige a la página de inicio pública
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll()
                );

        http.addFilterBefore(jwtAuthorizationFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public JwtAuthorizationFilter jwtAuthorizationFilter() {
        return new JwtAuthorizationFilter();
    }

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedOrigins("*")
                // .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // Puedes ser más específico si lo deseas
                // .allowedHeaders("*")
                // .allowCredentials(true)
                ;
            }
        };
    }
}