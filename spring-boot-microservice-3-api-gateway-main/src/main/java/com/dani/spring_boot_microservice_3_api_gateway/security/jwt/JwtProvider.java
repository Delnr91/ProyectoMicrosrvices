package com.dani.spring_boot_microservice_3_api_gateway.security.jwt;

import com.dani.spring_boot_microservice_3_api_gateway.model.User;
import com.dani.spring_boot_microservice_3_api_gateway.security.UserPrincipal;
import jakarta.servlet.http.HttpServletRequest; // Asegúrate que es jakarta
import org.springframework.security.core.Authentication;

/**
 * Interfaz que define las operaciones para la gestión de JSON Web Tokens (JWT).
 * Abstrae la lógica de generación, validación y extracción de información de tokens.
 */
public interface JwtProvider {

    /**
     * Genera un token JWT basado en los detalles de un UserPrincipal autenticado.
     * @param auth El UserPrincipal autenticado.
     * @return El token JWT generado como String.
     */
    String generateToken(UserPrincipal auth);

    /**
     * Genera un token JWT directamente desde un objeto User.
     * @param user El objeto User para el cual generar el token.
     * @return El token JWT generado como String.
     */
    String generateToken(User user);

    /**
     * Intenta extraer y validar un token JWT de una HttpServletRequest y
     * construye un objeto Authentication para Spring Security si es válido.
     * @param request La petición HTTP entrante.
     * @return Un objeto Authentication si el token es válido, o null si no.
     */
    Authentication getAuthentication(HttpServletRequest request);

    /**
     * Verifica si hay un token JWT válido en la HttpServletRequest.
     * @param request La petición HTTP entrante.
     * @return true si el token es válido (existe y no ha expirado), false en caso contrario.
     */
    boolean istTokenValid(HttpServletRequest request);
}