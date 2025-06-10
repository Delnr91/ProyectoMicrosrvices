package com.dani.spring_boot_microservice_3_api_gateway.utils;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.util.StringUtils;

/**
 * Clase de utilidad con métodos estáticos relacionados con la seguridad,
 * principalmente para el manejo de roles de Spring Security y la extracción
 * de tokens JWT de las cabeceras de las peticiones HTTP.
 * <p>
 * Esta clase no está pensada para ser instanciada; todos sus métodos son estáticos.
 *
 * @author Daniel Núñez Rojas (danidev fullstack software)
 * @version 1.0
 * @since 2025-05-04 // Fecha de creación o última modificación significativa
 */
public final class SecurityUtils { // Marcada como "final" ya que solo tiene métodos estáticos

    /**
     * Prefijo estándar que Spring Security espera para los nombres de roles
     * al crear objetos {@link org.springframework.security.core.GrantedAuthority}.
     * Por ejemplo, un rol "ADMIN" se convierte en "ROLE_ADMIN".
     */
    public static final String ROLE_PREFIX = "ROLE_";

    /**
     * Nombre estándar del header HTTP utilizado para transportar información de autorización,
     * comúnmente el token JWT. El valor es "authorization".
     */
    public static final String AUTH_HEADER = "authorization";

    /**
     * Tipo de token estándar para JWT, utilizado como prefijo en el valor del header "Authorization".
     * El valor es "Bearer".
     */
    public static final String AUTH_TOKEN_TYPE = "Bearer";

    /**
     * Prefijo completo (incluyendo el espacio) esperado en el valor del header "Authorization"
     * antes del token JWT. El valor es "Bearer ".
     */
    public static final String AUTH_TOKEN_PREFIX = AUTH_TOKEN_TYPE + " ";

    /**
     * Constructor privado para prevenir la instanciación de esta clase de utilidad.
     */
    private SecurityUtils() {
        throw new IllegalStateException("Clase de utilidad no instanciable.");
    }

    /**
     * Convierte una cadena de texto que representa un rol (ej. "ADMIN", "USER")
     * en un objeto {@link SimpleGrantedAuthority} que Spring Security puede utilizar.
     * <p>
     * Si la cadena del rol no comienza ya con el prefijo {@link #ROLE_PREFIX},
     * este método lo añade automáticamente.
     *
     * @param role El nombre del rol como {@link String}. No debe ser nulo ni vacío.
     * @return El objeto {@link SimpleGrantedAuthority} correspondiente (ej. "ROLE_ADMIN").
     * @throws IllegalArgumentException si el rol proporcionado es nulo o vacío.
     */
    public static SimpleGrantedAuthority convertToAuthority(String role) {
        if (!StringUtils.hasText(role)) {
            throw new IllegalArgumentException("El rol no puede ser nulo ni vacío.");
        }
        String formattedRole = role.startsWith(ROLE_PREFIX) ? role : ROLE_PREFIX + role;
        return new SimpleGrantedAuthority(formattedRole);
    }

    /**
     * Extrae el token JWT puro (sin el prefijo "Bearer ") del header "Authorization"
     * de una petición HTTP {@link HttpServletRequest}.
     * <p>
     * Espera que el valor del header "Authorization" tenga el formato "Bearer &lt;token&gt;".
     * Si el header no está presente, está vacío, o no comienza con el prefijo
     * {@link #AUTH_TOKEN_PREFIX}, el método devuelve {@code null}.
     *
     * @param request La petición HTTP entrante de la cual se extraerá el token. No debe ser nula.
     * @return El token JWT como {@link String} si se encuentra y tiene el formato correcto;
     * {@code null} en caso contrario.
     */
    public static String extractAuthTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTH_HEADER);

        if (StringUtils.hasLength(bearerToken) && bearerToken.startsWith(AUTH_TOKEN_PREFIX)) {
            return bearerToken.substring(AUTH_TOKEN_PREFIX.length());
        }
        return null;
    }
}