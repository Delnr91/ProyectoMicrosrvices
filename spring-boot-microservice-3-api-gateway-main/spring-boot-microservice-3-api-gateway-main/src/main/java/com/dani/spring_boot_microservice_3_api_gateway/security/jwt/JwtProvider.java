package com.dani.spring_boot_microservice_3_api_gateway.security.jwt;

import com.dani.spring_boot_microservice_3_api_gateway.model.User;
import com.dani.spring_boot_microservice_3_api_gateway.security.UserPrincipal;
import org.springframework.security.core.Authentication;

import javax.servlet.http.HttpServletRequest;

public interface JwtProvider {

    //para crear token basado en la data del usario
    String generateToken(UserPrincipal auth);

    //metodo para generar un token por parametro user
    String generateToken(User user);

    //metodo publico para obtener la autentificacion
    Authentication getAuthentication(HttpServletRequest request);

    //Metodo para saber si el token es valido
    boolean istTokenValid(HttpServletRequest request);
}
