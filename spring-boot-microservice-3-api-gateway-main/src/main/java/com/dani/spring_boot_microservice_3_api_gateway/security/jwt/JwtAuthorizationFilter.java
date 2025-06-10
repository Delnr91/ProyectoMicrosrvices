package com.dani.spring_boot_microservice_3_api_gateway.security.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;


import java.io.IOException;

/**
 * Filtro de Spring Security que se ejecuta una vez por cada petición HTTP.
 * Su propósito principal es interceptar las peticiones entrantes, extraer y validar
 * el token JWT (JSON Web Token) presente (generalmente en el header "Authorization").
 * <p>
 * Si se encuentra un token JWT válido:
 * <ol>
 * <li>Se construye un objeto {@link Authentication} a partir de la información contenida en el token (como el usuario y sus roles/autoridades).</li>
 * <li>Este objeto {@link Authentication} se establece en el {@link SecurityContextHolder},
 * lo que efectivamente autentica al usuario para la duración de la petición actual.</li>
 * </ol>
 * Si no hay token o el token es inválido, el filtro simplemente pasa la petición
 * al siguiente filtro en la cadena sin establecer ninguna autenticación. Otros mecanismos
 * de Spring Security (como {@code ExceptionTranslationFilter} o reglas de autorización)
 * se encargarán de manejar el acceso a recursos protegidos.
 * <p>
 * Este filtro se registra en la cadena de filtros de Spring Security antes que
 * {@link org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter},
 * como se configura en {@link com.dani.spring_boot_microservice_3_api_gateway.security.SecurityConfig}.
 *
 * @see JwtProvider Utilizado para validar el token y obtener el objeto Authentication.
 * @see OncePerRequestFilter Clase base de Spring que asegura que el filtro se ejecuta solo una vez por petición.
 * @see SecurityContextHolder Donde se almacena la información de autenticación.
 * @author Daniel Núñez Rojas (danidev fullstack software) // O tu nombre
 * @version 1.0
 * @since 2025-05-04 // Fecha de creación o última modificación significativa
 */
public class JwtAuthorizationFilter extends OncePerRequestFilter {

    /**
     * Proveedor de JWT, inyectado por Spring.
     * Se utiliza para la lógica de validación del token y la extracción de la autenticación.
     * La anotación {@link Autowired} aquí asume que este filtro es gestionado como un bean
     * y que JwtProvider también es un bean disponible para inyección.
     * Alternativamente, si este filtro se crea con 'new' en SecurityConfig, JwtProvider
     * se pasaría por constructor.
     */
    @Autowired
    private JwtProvider jwtProvider;

    /**
     * Lógica principal del filtro que se ejecuta para cada petición HTTP.
     * <p>
     * Intenta obtener un objeto {@link Authentication} a partir del token JWT
     * presente en la {@link HttpServletRequest}. Si el token es válido y se obtiene
     * una autenticación, esta se establece en el {@link SecurityContextHolder}.
     * Luego, la petición continúa hacia el siguiente filtro en la cadena.
     *
     * @param request La petición HTTP entrante.
     * @param response La respuesta HTTP.
     * @param filterChain La cadena de filtros para invocar al siguiente filtro.
     * @throws ServletException Si ocurre un error específico del servlet durante el procesamiento.
     * @throws IOException Si ocurre un error de entrada/salida.
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        Authentication authentication = jwtProvider.getAuthentication(request);

        // Si se pudo obtener un objeto Authentication (lo que implica que el token era válido y parseable)
        // Y si (como una doble verificación opcional) el token sigue siendo considerado válido en este punto
        if (authentication != null && jwtProvider.istTokenValid(request)) { // Manteniendo el typo "istTokenValid"
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        filterChain.doFilter(request, response);
    }
}