package com.dani.spring_boot_microservice_3_api_gateway.security;

import com.dani.spring_boot_microservice_3_api_gateway.model.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Set;

/**
 * Implementación de la interfaz {@link UserDetails} de Spring Security.
 * <p>
 * Esta clase representa al usuario principal (autenticado) dentro del contexto de seguridad de Spring.
 * Contiene la información esencial del usuario necesaria para los procesos de autenticación y autorización,
 * como el nombre de usuario, la contraseña (aunque a menudo se maneja con cuidado y no se expone directamente
 * después de la autenticación), las autoridades (roles/permisos), y el estado de la cuenta (habilitada,
 * no expirada, no bloqueada).
 * <p>
 * Es utilizada por {@link CustomUserDetailsService} para construir el objeto que Spring Security
 * manejará internamente. También se puede acceder a ella en los controladores mediante la anotación
 * {@code @AuthenticationPrincipal} para obtener detalles del usuario actualmente autenticado.
 * <p>
 * Los campos marcados como {@code transient} no se serializan si el objeto UserPrincipal
 * necesitara ser serializado (por ejemplo, en sesiones HTTP distribuidas, aunque con JWT
 * y sesiones stateless esto es menos común para UserDetails).
 *
 * @see UserDetails Interfaz de Spring Security que esta clase implementa.
 * @see User La entidad del dominio que puede estar asociada a este principal.
 * @see CustomUserDetailsService El servicio que crea instancias de esta clase.
 * @author Daniel Núñez Rojas (danidev fullstack software) // Asumiendo el autor original o el tuyo
 * @version 1.0
 * @since 2025-05-04 // Fecha de creación o última modificación significativa
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserPrincipal implements UserDetails {

    /**
     * El identificador único del usuario, obtenido de la entidad {@link User}.
     */
    private Long id;

    /**
     * El nombre de usuario utilizado para la autenticación.
     * Corresponde al campo {@code username} de la entidad {@link User}.
     */
    private String username;

    /**
     * La contraseña codificada del usuario.
     * Este campo es {@code transient} para evitar su serialización innecesaria
     * o exposición accidental después de la autenticación. Spring Security la utiliza
     * internamente durante el proceso de autenticación.
     */
    transient private String password;

    /**
     * Referencia opcional a la entidad completa del {@link User} del dominio.
     * Este campo es {@code transient} y puede ser útil para acceder a otros detalles del usuario
     * que no forman parte estándar de la interfaz {@code UserDetails}, sin necesidad de
     * volver a consultar la base de datos.
     */
    transient private User user;

    /**
     * El conjunto de autoridades (roles y/o permisos) concedidas al usuario.
     * Por ejemplo, "ROLE_USER", "ROLE_ADMIN".
     * No debe ser {@code null}.
     */
    private Set<GrantedAuthority> authorities;

    /**
     * Devuelve las autoridades concedidas al usuario. No puede ser {@code null}.
     * @return una colección de autoridades, nunca {@code null}
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    /**
     * Devuelve la contraseña utilizada para autenticar al usuario.
     * @return la contraseña del usuario
     */
    @Override
    public String getPassword() {
        return password;
    }

    /**
     * Devuelve el nombre de usuario utilizado para autenticar al usuario. No puede ser {@code null}.
     * @return el nombre de usuario, nunca {@code null}
     */
    @Override
    public String getUsername() {
        return username;
    }

    /**
     * Indica si la cuenta del usuario ha expirado. Una cuenta expirada no puede ser
     * autenticada.
     * <p>En esta implementación, las cuentas nunca expiran por defecto.</p>
     * @return {@code true} si la cuenta del usuario es válida (es decir, no ha expirado),
     * {@code false} si ya no es válida (ha expirado)
     */
    @Override
    public boolean isAccountNonExpired() {
        return true; // Por defecto, las cuentas no expiran
    }

    /**
     * Indica si el usuario está bloqueado o desbloqueado. Un usuario bloqueado no puede ser
     * autenticado.
     * <p>En esta implementación, los usuarios nunca están bloqueados por defecto.</p>
     * @return {@code true} si el usuario no está bloqueado, {@code false} en caso contrario
     */
    @Override
    public boolean isAccountNonLocked() {
        return true; // Por defecto, las cuentas no están bloqueadas
    }

    /**
     * Indica si las credenciales del usuario (contraseña) han expirado. Credenciales
     * expiradas impiden la autenticación.
     * <p>En esta implementación, las credenciales nunca expiran por defecto.</p>
     * @return {@code true} si las credenciales del usuario son válidas (es decir, no han expirado),
     * {@code false} si ya no son válidas (han expirado)
     */
    @Override
    public boolean isCredentialsNonExpired() {
        return true; // Por defecto, las credenciales no expiran
    }

    /**
     * Indica si el usuario está habilitado o deshabilitado. Un usuario deshabilitado no puede ser
     * autenticado.
     * <p>En esta implementación, los usuarios siempre están habilitados por defecto.</p>
     * @return {@code true} si el usuario está habilitado, {@code false} en caso contrario
     */
    @Override
    public boolean isEnabled() {
        return true; // Por defecto, los usuarios están habilitados
    }
}