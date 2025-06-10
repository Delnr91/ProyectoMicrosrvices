package com.dani.spring_boot_microservice_3_api_gateway.security.jwt;

import com.dani.spring_boot_microservice_3_api_gateway.model.User;
import com.dani.spring_boot_microservice_3_api_gateway.security.UserPrincipal;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;

/**
 * Interfaz que define el contrato para la generación, validación y gestión
 * de JSON Web Tokens (JWT) dentro del sistema.
 * <p>
 * Esta abstracción permite desacoplar la lógica de JWT del resto de la aplicación,
 * facilitando cambios en la implementación o en la librería de JWT si fuera necesario.
 * Las implementaciones de esta interfaz son responsables de:
 * <ul>
 * <li>Generar tokens JWT para usuarios autenticados.</li>
 * <li>Validar tokens JWT recibidos en las peticiones HTTP.</li>
 * <li>Extraer la información de autenticación ({@link Authentication}) de un token válido.</li>
 * </ul>
 *
 * @see JwtProviderImpl Implementación por defecto de esta interfaz.
 * @see UserPrincipal Objeto que representa al usuario autenticado, a menudo usado para generar tokens.
 * @see User Entidad del dominio que también puede ser usada para generar tokens.
 * @author Daniel Núñez Rojas (danidev fullstack software) // O tu nombre
 * @version 1.0
 * @since 2025-05-04 // Fecha de creación o última modificación significativa
 */
public interface JwtProvider {

    /**
     * Genera un token JWT a partir de un objeto {@link UserPrincipal} que representa
     * al usuario ya autenticado por Spring Security.
     * El token incluirá claims esenciales como el nombre de usuario (subject),
     * los roles/autoridades y el ID del usuario.
     *
     * @param auth El objeto {@link UserPrincipal} del usuario autenticado. No debe ser nulo.
     * @return Una cadena de texto que representa el token JWT generado.
     */
    String generateToken(UserPrincipal auth);

    /**
     * Genera un token JWT directamente a partir de un objeto {@link User} del dominio.
     * Este método es útil, por ejemplo, justo después de que un nuevo usuario se registra
     * y se desea proporcionarle un token inmediatamente.
     * El token incluirá claims como el nombre de usuario, el rol y el ID del usuario.
     *
     * @param user El objeto {@link User} para el cual se generará el token. No debe ser nulo y se espera que tenga ID y rol asignados.
     * @return Una cadena de texto que representa el token JWT generado.
     */
    String generateToken(User user);

    /**
     * Intenta extraer un token JWT de una {@link HttpServletRequest} entrante,
     * validarlo y, si es válido, construir un objeto {@link Authentication}
     * que Spring Security pueda utilizar para establecer el contexto de seguridad.
     * <p>
     * La extracción del token típicamente se realiza desde el header "Authorization" (Bearer token).
     * La validación incluye verificar la firma del token y su fecha de expiración.
     *
     * @param request La petición HTTP entrante de la cual se intentará extraer el token.
     * @return Un objeto {@link Authentication} poblado con los detalles del usuario
     * (extraídos del token) si el token es válido y está presente.
     * Devuelve {@code null} si no hay token, si el token es inválido,
     * o si no se puede construir un objeto de autenticación a partir de él.
     */
    Authentication getAuthentication(HttpServletRequest request);

    /**
     * Verifica si el token JWT presente en la {@link HttpServletRequest} es válido.
     * <p>
     * Un token se considera válido si:
     * <ul>
     * <li>Está presente en la petición (normalmente en el header "Authorization").</li>
     * <li>Su firma es correcta (verificada con la clave secreta).</li>
     * <li>No ha expirado.</li>
     * </ul>
     *
     * @param request La petición HTTP entrante que puede contener el token.
     * @return {@code true} si se encuentra un token JWT válido en la petición,
     * {@code false} en caso contrario.
     */
    boolean istTokenValid(HttpServletRequest request); // Nota: typo "istTokenValid" en lugar de "isTokenValid"
}