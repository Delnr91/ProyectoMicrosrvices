package com.dani.spring_boot_microservice_3_api_gateway.security.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter; // Asegúrate que este import es el correcto

import java.io.IOException;

/**
 * Filtro de Spring Security que intercepta todas las peticiones una vez (`OncePerRequestFilter`)
 * para validar el token JWT presente en el header 'Authorization'.
 * Si el token es válido, establece el objeto Authentication en el SecurityContextHolder.
 */
public class JwtAuthorizationFilter extends OncePerRequestFilter {

    // Inyecta el proveedor de JWT para validar y obtener la autenticación.
    // NOTA: Considerar inyección por constructor si se convierte en @Component o se crea en SecurityConfig.
    @Autowired
    private JwtProvider jwtProvider;

    /**
     * Lógica principal del filtro. Se ejecuta para cada petición.
     * Extrae el token, lo valida y establece la autenticación en el contexto de seguridad.
     *
     * @param request     La petición HTTP entrante.
     * @param response    La respuesta HTTP.
     * @param filterChain La cadena de filtros para pasar la petición al siguiente filtro.
     * @throws ServletException Si ocurre un error relacionado con Servlets.
     * @throws IOException      Si ocurre un error de entrada/salida.
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // Intenta obtener la autenticación a partir del token en la petición.
        Authentication authentication = jwtProvider.getAuthentication(request);

        // Si la autenticación no es nula (token válido) y el token es válido (redundante pero seguro)...
        if (authentication != null && jwtProvider.istTokenValid(request)) {
            // Establece la autenticación en el contexto de seguridad para esta petición.
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        // Continúa con el siguiente filtro en la cadena.
        filterChain.doFilter(request, response);
    }
}