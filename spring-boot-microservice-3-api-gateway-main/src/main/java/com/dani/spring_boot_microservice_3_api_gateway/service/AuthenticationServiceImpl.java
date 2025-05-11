package com.dani.spring_boot_microservice_3_api_gateway.service;

import com.dani.spring_boot_microservice_3_api_gateway.model.User;
import com.dani.spring_boot_microservice_3_api_gateway.security.UserPrincipal;
import com.dani.spring_boot_microservice_3_api_gateway.security.jwt.JwtProvider;
import lombok.RequiredArgsConstructor; // Importar
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

/**
 * Implementación del servicio de autenticación.
 * Maneja el proceso de inicio de sesión (sign-in) y la generación de JWT.
 */
@Service
@RequiredArgsConstructor // Lombok para inyección por constructor
public class AuthenticationServiceImpl implements AuthenticationService {

    // Inyección por constructor (campos final)
    private final AuthenticationManager authenticationManager;
    private final JwtProvider jwtProvider;

    /**
     * {@inheritDoc}
     * Autentica al usuario usando el AuthenticationManager y genera un token JWT si es exitoso.
     */
    @Override
    public User signInAndReturnJWT(User signInRequest) {
        // Crea el objeto de autenticación para Spring Security
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(signInRequest.getUsername(), signInRequest.getPassword())
        );

        // Obtiene los detalles del usuario autenticado (nuestro UserPrincipal)
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

        // Genera el token JWT para este usuario
        String jwt = jwtProvider.generateToken(userPrincipal);

        // Obtiene el objeto User completo desde UserPrincipal
        User sigInUser = userPrincipal.getUser();
        // Asigna el token generado al objeto User (campo transitorio) antes de devolverlo
        sigInUser.setToken(jwt);

        return sigInUser;
    }
}