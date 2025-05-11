package com.dani.spring_boot_microservice_3_api_gateway.utils;

import jakarta.servlet.http.HttpServletRequest; // Asegúrate que es jakarta
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.util.StringUtils;

/**
 * Clase de utilidad con métodos estáticos relacionados con la seguridad,
 * como manejo de roles y extracción de tokens JWT de las peticiones.
 */
public class SecurityUtils {

    /** Prefijo estándar para roles en Spring Security (ej. "ROLE_ADMIN"). */
    public static final String ROLE_PREFIX = "ROLE_";
    /** Nombre estándar del header HTTP para la autorización. */
    public static final String AUTH_HEADER = "authorization";
    /** Tipo de token estándar para JWT en el header Authorization. */
    public static final String AUTH_TOKEN_TYPE = "Bearer";
    /** Prefijo completo esperado en el header Authorization para un token Bearer. */
    public static final String AUTH_TOKEN_PREFIX = AUTH_TOKEN_TYPE + " "; // Incluye el espacio

    /**
     * Convierte un String de rol (ej. "ADMIN") a un objeto SimpleGrantedAuthority
     * requerido por Spring Security, añadiendo el prefijo "ROLE_" si no lo tiene.
     *
     * @param role El nombre del rol como String.
     * @return El objeto SimpleGrantedAuthority correspondiente (ej. "ROLE_ADMIN").
     */
    public static SimpleGrantedAuthority convertToAuthority(String role) {
        // Asegura que el rol tenga el prefijo ROLE_
        String formattedRole = role.startsWith(ROLE_PREFIX) ? role : ROLE_PREFIX + role;
        return new SimpleGrantedAuthority(formattedRole);
    }

    /**
     * Extrae el token JWT puro del header 'Authorization' de una petición HTTP.
     * Espera el formato "Bearer {token}".
     *
     * @param request La petición HTTP entrante.
     * @return El token JWT como String si se encuentra y tiene el formato correcto, o null si no.
     */
    public static String extractAuthTokenFromRequest(HttpServletRequest request) {
        // Obtiene el valor del header "Authorization"
        String bearerToken = request.getHeader(AUTH_HEADER);

        // Verifica que el header exista, no esté vacío y comience con "Bearer "
        if (StringUtils.hasLength(bearerToken) && bearerToken.startsWith(AUTH_TOKEN_PREFIX)) {
            // Devuelve el token quitando el prefijo "Bearer " (7 caracteres)
            return bearerToken.substring(7);
        }
        // Si no cumple las condiciones, devuelve null
        return null;
    }
}