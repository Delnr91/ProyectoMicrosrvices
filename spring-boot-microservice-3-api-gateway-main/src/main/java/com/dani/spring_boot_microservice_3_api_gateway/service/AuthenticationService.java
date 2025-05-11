package com.dani.spring_boot_microservice_3_api_gateway.service;

import com.dani.spring_boot_microservice_3_api_gateway.model.User;

/**
 * Define el contrato para el servicio de autenticación.
 */
public interface AuthenticationService {

    /**
     * Realiza el proceso de inicio de sesión (sign-in).
     * Valida las credenciales del usuario y, si son correctas,
     * devuelve el objeto User con un token JWT generado y asignado.
     *
     * @param signInRequest Objeto User que contiene el username y password para autenticar.
     * @return El objeto User autenticado, incluyendo el token JWT en el campo transitorio 'token'.
     * @throws org.springframework.security.core.AuthenticationException Si la autenticación falla.
     */
    User signInAndReturnJWT(User signInRequest);
}