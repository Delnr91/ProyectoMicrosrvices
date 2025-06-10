package com.dani.spring_boot_microservice_3_api_gateway.service;

import com.dani.spring_boot_microservice_3_api_gateway.model.User;
import com.dani.spring_boot_microservice_3_api_gateway.security.UserPrincipal;
import com.dani.spring_boot_microservice_3_api_gateway.security.jwt.JwtProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

/**
 * Implementación concreta de la interfaz {@link AuthenticationService}.
 * <p>
 * Esta clase maneja el proceso de inicio de sesión (sign-in). Utiliza el
 * {@link AuthenticationManager} de Spring Security para validar las credenciales
 * del usuario y, si la autenticación es exitosa, emplea un {@link JwtProvider}
 * para generar un token JWT que se devuelve junto con la información del usuario.
 *
 * @see AuthenticationService Interfaz que esta clase implementa.
 * @see AuthenticationManager Utilizado para el proceso de autenticación de Spring Security.
 * @see JwtProvider Utilizado para generar el token JWT.
 * @see UserPrincipal Objeto principal de Spring Security que se obtiene tras la autenticación.
 * @author Daniel Núñez Rojas (danidev fullstack software)
 * @version 1.0
 * @since 2025-05-04 // Fecha de creación o última modificación significativa
 */
@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {

    //Inyectado por spring (dependencias)
    private final AuthenticationManager authenticationManager;
    private final JwtProvider jwtProvider;

    /**
     * {@inheritDoc}
     * <p>
     * Esta implementación específica realiza los siguientes pasos:
     * <ol>
     * <li>Crea un objeto {@link UsernamePasswordAuthenticationToken} con el nombre de usuario y la contraseña
     * proporcionados en {@code signInRequest}.</li>
     * <li>Pasa este token al {@link AuthenticationManager#authenticate(Authentication)} para su validación.
     * Si las credenciales son incorrectas, el {@code AuthenticationManager} lanzará una
     * {@link org.springframework.security.core.AuthenticationException}.</li>
     * <li>Si la autenticación es exitosa, se obtiene el objeto {@link UserPrincipal}
     * del objeto {@link Authentication} devuelto por el {@code AuthenticationManager}.</li>
     * <li>Se utiliza el {@link JwtProvider#generateToken(UserPrincipal)} para crear un nuevo token JWT.</li>
     * <li>Se recupera la entidad {@link User} completa desde el {@code UserPrincipal}.</li>
     * <li>El token JWT generado se establece en el campo transitorio {@code token} de la entidad {@link User}.</li>
     * <li>Finalmente, se devuelve la entidad {@link User} con el token.</li>
     * </ol>
     */
    @Override
    public User signInAndReturnJWT(User signInRequest) {
        // Crea el objeto de autenticación para que Spring Security lo procese
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(signInRequest.getUsername(), signInRequest.getPassword())
        );

        // Si la autenticación es exitosa, authentication.getPrincipal() devuelve nuestro UserPrincipal
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

        // Genera el token JWT para este UserPrincipal
        String jwt = jwtProvider.generateToken(userPrincipal);

        // Obtiene la entidad User completa que está encapsulada dentro del UserPrincipal
        User sigInUser = userPrincipal.getUser();
        // Asigna el token JWT generado al campo transitorio 'token' del objeto User
        sigInUser.setToken(jwt);

        return sigInUser;
    }
}