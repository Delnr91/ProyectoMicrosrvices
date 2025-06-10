package com.dani.spring_boot_microservice_3_api_gateway.service;

import com.dani.spring_boot_microservice_3_api_gateway.model.User;

/**
 * Interfaz que define el contrato para el servicio de autenticación de usuarios.
 * <p>
 * Es responsable de la lógica de negocio relacionada con el proceso de inicio de sesión (sign-in),
 * validando las credenciales del usuario y generando un token JWT si la autenticación es exitosa.
 * Esta interfaz abstrae la implementación específica del proceso de autenticación.
 *
 * @see AuthenticationServiceImpl Implementación por defecto de esta interfaz.
 * @see User Objeto que contiene las credenciales para la solicitud de inicio de sesión y
 * también el objeto devuelto con el token tras una autenticación exitosa.
 * @author Daniel Núñez Rojas (danidev fullstack software) // O tu nombre
 * @version 1.0
 * @since 2025-05-04 // Fecha de creación o última modificación significativa
 */
public interface AuthenticationService {

    /**
     * Realiza el proceso de inicio de sesión (sign-in) para un usuario.
     * <p>
     * Este método toma las credenciales del usuario (normalmente nombre de usuario y contraseña)
     * contenidas en el objeto {@code signInRequest}. Utiliza el {@code AuthenticationManager}
     * de Spring Security para validar estas credenciales.
     * <p>
     * Si la autenticación es exitosa:
     * <ol>
     * <li>Se obtiene el {@link com.dani.spring_boot_microservice_3_api_gateway.security.UserPrincipal} del usuario autenticado.</li>
     * <li>Se genera un token JWT para este usuario utilizando el {@link com.dani.spring_boot_microservice_3_api_gateway.security.jwt.JwtProvider}.</li>
     * <li>El token JWT generado se asigna al campo transitorio {@code token} del objeto {@link User}
     * (que se obtiene a partir del {@code UserPrincipal}).</li>
     * </ol>
     * El objeto {@link User} resultante, con el token incluido, se devuelve al llamador.
     *
     * @param signInRequest Un objeto {@link User} que contiene el nombre de usuario y la contraseña
     * proporcionados por el usuario para el intento de inicio de sesión.
     * @return El objeto {@link User} correspondiente al usuario autenticado, con el campo
     * transitorio {@code token} poblado con un nuevo token JWT.
     * @throws org.springframework.security.core.AuthenticationException Si las credenciales son inválidas
     * o si ocurre cualquier otro error durante el proceso de autenticación gestionado
     * por el {@code AuthenticationManager} de Spring Security.
     */
    User signInAndReturnJWT(User signInRequest);
}