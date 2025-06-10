package com.dani.spring_boot_microservice_3_api_gateway.security;

import com.dani.spring_boot_microservice_3_api_gateway.model.User;
import com.dani.spring_boot_microservice_3_api_gateway.service.UserService;
import com.dani.spring_boot_microservice_3_api_gateway.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Set;

/**
 * Implementación personalizada de la interfaz {@link UserDetailsService} de Spring Security.
 * <p>
 * Esta clase es responsable de cargar los detalles de un usuario (específicamente, un objeto
 * {@link UserDetails}) a partir de su nombre de usuario. Interactúa con {@link UserService}
 * para obtener la información del usuario desde la base de datos (o cualquier otro almacén de datos).
 * <p>
 * Spring Security utiliza esta implementación durante el proceso de autenticación
 * (por ejemplo, al procesar un formulario de inicio de sesión o al validar un token JWT)
 * para verificar las credenciales del usuario y obtener sus roles/autoridades.
 *
 * @see UserDetailsService Interfaz de Spring Security que esta clase implementa.
 * @see UserService Servicio utilizado para acceder a los datos del usuario.
 * @see UserPrincipal Implementación de {@link UserDetails} que representa al usuario autenticado.
 * @author Daniel Núñez Rojas (danidev fullstack software)
 * @version 1.0
 * @since 2025-05-04 (Fecha de creación inicial o última modificación significativa)
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserService userService;

    /**
     * Carga un usuario por su nombre de usuario (username).
     * <p>
     * Este método es invocado por el AuthenticationManager de Spring Security
     * cuando un usuario intenta autenticarse. Busca al usuario a través del {@link UserService}.
     * Si el usuario es encontrado, convierte su información (incluyendo su rol) en un objeto
     * {@link UserPrincipal} (que es una implementación de {@link UserDetails}).
     * Si el usuario no se encuentra, lanza una {@link UsernameNotFoundException}.
     *
     * @param username El nombre de usuario (único) del usuario que se intenta autenticar.
     * @return Un objeto {@link UserDetails} (específicamente {@link UserPrincipal}) que contiene
     * la información esencial del usuario (ID, username, contraseña codificada, autoridades).
     * @throws UsernameNotFoundException Si no se encuentra ningún usuario con el nombre de usuario proporcionado.
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Busca el usuario en la base de datos utilizando el UserService.
        User user = userService.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("El usuario no fue encontrado: " + username));

        // Convierte el rol del usuario (almacenado en la entidad User) a un conjunto de GrantedAuthority.
        // SecurityUtils.convertToAuthority se encarga de añadir el prefijo "ROLE_" si es necesario.
        Set<GrantedAuthority> authorities = Set.of(SecurityUtils.convertToAuthority(user.getRole().name()));

        // Construye y devuelve el objeto UserPrincipal.
        // Este objeto será utilizado por Spring Security para completar el proceso de autenticación
        // y para las comprobaciones de autorización.
        return UserPrincipal.builder()
                .user(user) // Almacena la entidad User completa para referencia, si es necesario en otros puntos.
                .id(user.getId())
                .username(user.getUsername())
                .password(user.getPassword()) // La contraseña ya debe estar codificada en la base de datos.
                .authorities(authorities)
                .build();
    }
}