package com.dani.spring_boot_microservice_3_api_gateway.request;

import com.dani.spring_boot_microservice_3_api_gateway.security.UserPrincipal;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component; // Marcado como Component para gestión de Spring

import java.util.stream.Collectors;

/**
 * Interceptor de Feign para añadir cabeceras X-User-ID y X-User-Roles.
 * Estas cabeceras permiten a los microservicios destino conocer la identidad y roles
 * del usuario que originó la petición en el API Gateway.
 *
 * @author Daniel Núñez Rojas (danidev fullstack software)
 */

@Slf4j
public class UserContextRequestInterceptor implements RequestInterceptor {

    private static final String USER_ID_HEADER = "X-User-ID";
    private static final String USER_ROLES_HEADER = "X-User-Roles";

    @Override
    public void apply(RequestTemplate template) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated() && authentication.getPrincipal() instanceof UserPrincipal) {
            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
            log.info(">>> Interceptor: UserPrincipal ID: {}, Username: {}, Authorities: {}",
                    userPrincipal.getId(), userPrincipal.getUsername(), userPrincipal.getAuthorities()); // NUEVO LOG

            if (userPrincipal.getId() != null) {
                log.debug("Propagando cabecera {}: {}", USER_ID_HEADER, userPrincipal.getId());
                template.header(USER_ID_HEADER, String.valueOf(userPrincipal.getId()));
            } else {
                log.warn("UserPrincipal autenticado no tiene ID. No se añadirá cabecera {}.", USER_ID_HEADER);
            }

            if (userPrincipal.getAuthorities() != null && !userPrincipal.getAuthorities().isEmpty()) {
                String roles = userPrincipal.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .collect(Collectors.joining(","));
                log.debug("Propagando cabecera {}: {}", USER_ROLES_HEADER, roles);
                template.header(USER_ROLES_HEADER, roles);
            } else {
                log.warn("UserPrincipal autenticado no tiene roles. No se añadirá cabecera {}.", USER_ROLES_HEADER);
            }
        } else {
            log.debug("No hay UserPrincipal autenticado en SecurityContextHolder o no es del tipo esperado. No se propagarán cabeceras de usuario.");
        }
    }
}