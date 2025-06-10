package com.dani.spring_boot_microservice_3_api_gateway.request;

import com.dani.spring_boot_microservice_3_api_gateway.security.UserPrincipal;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;


import java.util.stream.Collectors;

/**
 * Interceptor de Feign diseñado para propagar la información del usuario autenticado
 * (ID y roles) a los microservicios downstream.
 * <p>
 * Este interceptor se ejecuta antes de cada petición Feign y añade dos cabeceras HTTP personalizadas:
 * <ul>
 * <li>{@value #USER_ID_HEADER}: Contiene el ID del usuario autenticado.</li>
 * <li>{@value #USER_ROLES_HEADER}: Contiene una lista de roles/autoridades del usuario,
 * separados por comas.</li>
 * </ul>
 * Estas cabeceras permiten a los servicios destino realizar operaciones de auditoría,
 * aplicar lógica de negocio basada en el usuario o implementar permisos granulares.
 * <p>
 * La información del usuario se obtiene del {@link SecurityContextHolder} de Spring Security.
 * Si no hay un usuario autenticado o el principal no es una instancia de {@link UserPrincipal},
 * las cabeceras no se añaden, y se registra un mensaje de advertencia o depuración.
 *
 * @see RequestInterceptor Interfaz de Feign que esta clase implementa.
 * @see UserPrincipal Clase que encapsula los detalles del usuario autenticado.
 * @see PropagateUserFeignConfiguration Donde se instancia este interceptor como un bean.
 * @author Daniel Núñez Rojas (danidev fullstack software)
 * @version 1.1
 * @since 2025-05-13 (Actualizado con logging y JavaDoc)
 */
@Slf4j // Lombok para logging
public class UserContextRequestInterceptor implements RequestInterceptor {

    /**
     * Nombre de la cabecera HTTP para propagar el ID del usuario.
     * Valor: {@value}.
     */
    private static final String USER_ID_HEADER = "X-User-ID";

    /**
     * Nombre de la cabecera HTTP para propagar los roles del usuario.
     * Valor: {@value}.
     */
    private static final String USER_ROLES_HEADER = "X-User-Roles";

    /**
     * Método llamado por Feign antes de enviar una petición.
     * <p>
     * Intenta obtener el {@link UserPrincipal} del usuario autenticado actual desde el
     * {@link SecurityContextHolder}. Si se encuentra un {@code UserPrincipal} válido con ID y autoridades:
     * <ul>
     * <li>Añade el ID del usuario a la cabecera {@value #USER_ID_HEADER}.</li>
     * <li>Añade los roles/autoridades del usuario (separados por comas) a la cabecera {@value #USER_ROLES_HEADER}.</li>
     * </ul>
     * Registra información de depuración sobre las cabeceras que se propagan o advertencias si
     * la información del usuario no está disponible.
     *
     * @param template El objeto {@link RequestTemplate} que representa la petición HTTP saliente.
     * Este template se modifica para añadir las nuevas cabeceras.
     */
    @Override
    public void apply(RequestTemplate template) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated() && authentication.getPrincipal() instanceof UserPrincipal) {
            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
            log.debug(">>> UserContextInterceptor: Propagando contexto para UserID: {}, Username: {}, Authorities: {}",
                    userPrincipal.getId(), userPrincipal.getUsername(), userPrincipal.getAuthorities());

            if (userPrincipal.getId() != null) {
                log.debug("Añadiendo cabecera {}: {}", USER_ID_HEADER, userPrincipal.getId());
                template.header(USER_ID_HEADER, String.valueOf(userPrincipal.getId()));
            } else {
                log.warn("UserPrincipal autenticado no tiene ID. No se añadirá cabecera {}.", USER_ID_HEADER);
            }

            if (userPrincipal.getAuthorities() != null && !userPrincipal.getAuthorities().isEmpty()) {
                String roles = userPrincipal.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .collect(Collectors.joining(","));
                log.debug("Añadiendo cabecera {}: {}", USER_ROLES_HEADER, roles);
                template.header(USER_ROLES_HEADER, roles); //
            } else {
                log.warn("UserPrincipal autenticado no tiene roles/autoridades. No se añadirá cabecera {}.", USER_ROLES_HEADER);
            }
        } else {
            log.debug("No hay UserPrincipal autenticado en SecurityContextHolder o no es del tipo esperado. No se propagarán cabeceras de usuario.");
        }
    }
}