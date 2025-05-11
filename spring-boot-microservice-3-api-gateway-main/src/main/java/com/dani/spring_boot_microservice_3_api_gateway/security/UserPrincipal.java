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
 * Implementación de la interfaz UserDetails de Spring Security.
 * Representa al usuario principal autenticado dentro del contexto de seguridad.
 * Contiene la información esencial del usuario (ID, username, authorities)
 * y opcionalmente una referencia al objeto User completo.
 * Los campos marcados como 'transient' no se serializan si fuera necesario.
 */
@Getter // Lombok: Genera getters para todos los campos.
@NoArgsConstructor // Lombok: Genera constructor sin argumentos.
@AllArgsConstructor // Lombok: Genera constructor con todos los argumentos.
@Builder // Lombok: Habilita el patrón Builder para crear instancias.
public class UserPrincipal implements UserDetails {

    /** Identificador único del usuario. */
    private Long id;
    /** Nombre de usuario. */
    private String username;
    /** Contraseña (transient para no exponerla innecesariamente). */
    transient private String password; // transient: no se serializa
    /** Referencia opcional al objeto User completo (transient). */
    transient private User user; // transient: no se serializa
    /** Conjunto de autoridades (roles/permisos) asignadas al usuario. */
    private Set<GrantedAuthority> authorities;

    // --- Implementación de métodos de UserDetails ---

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    /** Indica si la cuenta del usuario no ha expirado. Siempre true aquí. */
    @Override
    public boolean isAccountNonExpired() {
        return true; // Simplificado: asumir que no expira
    }

    /** Indica si la cuenta no está bloqueada. Siempre true aquí. */
    @Override
    public boolean isAccountNonLocked() {
        return true; // Simplificado: asumir que no se bloquea
    }

    /** Indica si las credenciales (contraseña) no han expirado. Siempre true aquí. */
    @Override
    public boolean isCredentialsNonExpired() {
        return true; // Simplificado: asumir que no expira
    }

    /** Indica si el usuario está habilitado. Siempre true aquí. */
    @Override
    public boolean isEnabled() {
        return true; // Simplificado: asumir que está habilitado
    }
}