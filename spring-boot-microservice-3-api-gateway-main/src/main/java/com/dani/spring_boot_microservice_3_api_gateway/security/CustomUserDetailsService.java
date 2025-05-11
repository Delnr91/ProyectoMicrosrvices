package com.dani.spring_boot_microservice_3_api_gateway.security;

import com.dani.spring_boot_microservice_3_api_gateway.model.User;
import com.dani.spring_boot_microservice_3_api_gateway.service.UserService;
import com.dani.spring_boot_microservice_3_api_gateway.utils.SecurityUtils;
import lombok.RequiredArgsConstructor; // Importar
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Set;

/**
 * Implementación personalizada de UserDetailsService de Spring Security.
 * Se encarga de cargar los detalles de un usuario (UserDetails) a partir de su
 * nombre de usuario, consultando la base de datos a través de UserService.
 */
@Service
@RequiredArgsConstructor // Lombok para inyección por constructor
public class CustomUserDetailsService implements UserDetailsService {

    // Inyección por constructor
    private final UserService userService;

    /**
     * Carga un usuario por su nombre de usuario.
     * Es invocado por Spring Security durante el proceso de autenticación.
     *
     * @param username El nombre de usuario a buscar.
     * @return Un objeto UserDetails (UserPrincipal) si el usuario es encontrado.
     * @throws UsernameNotFoundException Si el usuario no existe en la base de datos.
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Busca el usuario en la BD usando el servicio
        User user = userService.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("El usuario no fue encontrado: " + username));

        // Convierte el rol del usuario a un conjunto de GrantedAuthority
        Set<GrantedAuthority> authorities = Set.of(SecurityUtils.convertToAuthority(user.getRole().name()));

        // Construye y devuelve el objeto UserPrincipal con los detalles necesarios para Spring Security
        return UserPrincipal.builder()
                .user(user) // Guarda la referencia al User completo (útil en otros sitios)
                .id(user.getId())
                .username(user.getUsername()) // El username buscado
                .password(user.getPassword()) // La contraseña hasheada de la BD
                .authorities(authorities) // Los roles/permisos
                .build();
    }
}