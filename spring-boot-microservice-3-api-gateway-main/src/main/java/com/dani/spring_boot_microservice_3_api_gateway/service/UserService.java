package com.dani.spring_boot_microservice_3_api_gateway.service;

import com.dani.spring_boot_microservice_3_api_gateway.model.Role;
import com.dani.spring_boot_microservice_3_api_gateway.model.User;
import java.util.Optional;

/**
 * Define el contrato para las operaciones de negocio relacionadas con los usuarios.
 */
public interface UserService {

    /**
     * Guarda un nuevo usuario en el sistema.
     * Normalmente asigna valores por defecto (rol, fecha creación) y codifica la contraseña.
     * Puede devolver el usuario con un token JWT inicial.
     * @param user El usuario a guardar (sin ID, con contraseña en texto plano inicialmente).
     * @return El usuario guardado (con ID, contraseña codificada, token, etc.).
     */
    User saveUser(User user);

    /**
     * Busca un usuario por su nombre de usuario.
     * @param username El nombre de usuario a buscar.
     * @return Un Optional<User> que contiene al usuario si se encuentra.
     */
    Optional<User> findByUsername(String username);

    /**
     * Cambia el rol de un usuario existente, identificado por su nombre de usuario.
     * @param newRole El nuevo rol a asignar.
     * @param username El nombre de usuario del usuario a modificar.
     */
    void changeRole(Role newRole, String username);

    /**
     * Busca un usuario por su nombre de usuario y, si existe, genera un nuevo token JWT
     * y lo asigna al campo transitorio 'token' del objeto User devuelto.
     * @param username El nombre de usuario a buscar.
     * @return El objeto User encontrado, con un token JWT fresco asignado.
     * @throws org.springframework.security.core.userdetails.UsernameNotFoundException si el usuario no existe.
     */
    User findByUserameReturnToken(String username);
}