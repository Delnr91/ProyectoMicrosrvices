package com.dani.spring_boot_microservice_3_api_gateway.service;

import com.dani.spring_boot_microservice_3_api_gateway.model.Role;
import com.dani.spring_boot_microservice_3_api_gateway.model.User;
import java.util.List;
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

    // --- NUEVOS MÉTODOS PARA GESTIÓN DE ADMIN ---
    List<User> findAllUsers();

    Optional<User> findUserById(Long id);

    /**
     * Actualiza los datos de un usuario, llamado por un administrador.
     * Incluye lógica para el límite de admins y protección del admin principal.
     * @param userId El ID del usuario a actualizar.
     * @param userUpdateRequest Objeto User con los datos a actualizar (ej. nombre, nuevo rol).
     * @param currentAdminUsername El username del administrador que realiza la operación.
     * @return El usuario actualizado.
     * @throws RuntimeException Si se violan las reglas de negocio (límite de admin, intento de modificar admin principal).
     */
    User updateUserByAdmin(Long userId, User userUpdateRequest, String currentAdminUsername);

    /**
     * Elimina un usuario, llamado por un administrador.
     * Incluye protección para no eliminar al admin principal.
     * @param userIdToDelete El ID del usuario a eliminar.
     * @param currentAdminUsername El username del administrador que realiza la operación.
     * @throws RuntimeException Si se intenta eliminar al admin principal o el usuario no existe.
     */
    void deleteUserByAdmin(Long userIdToDelete, String currentAdminUsername);
}