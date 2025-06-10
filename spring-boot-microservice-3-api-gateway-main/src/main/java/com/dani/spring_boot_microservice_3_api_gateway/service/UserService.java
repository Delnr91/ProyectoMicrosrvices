package com.dani.spring_boot_microservice_3_api_gateway.service;

import com.dani.spring_boot_microservice_3_api_gateway.model.Role;
import com.dani.spring_boot_microservice_3_api_gateway.model.User;
import java.util.List;
import java.util.Optional;

/**
 * Interfaz que define el contrato para las operaciones de negocio
 * relacionadas con los usuarios del sistema.
 * Abstrae la lógica de negocio de la capa de acceso a datos y controladores.
 *
 * @author Daniel Núñez Rojas (danidev fullstack software)
 * @version 1.1
 * @since 2025-06-02
 */
public interface UserService {

    /**
     * Guarda un nuevo usuario en el sistema o actualiza uno existente.
     * Para nuevos usuarios, normalmente asigna valores por defecto (ej. rol USER, fecha de creación)
     * y codifica la contraseña. Puede devolver el usuario con un token JWT inicial.
     *
     * @param user El objeto {@link User} a guardar. Si es nuevo, no debe tener ID.
     * La contraseña se espera en texto plano para ser codificada.
     * @return El objeto {@link User} guardado (con ID, contraseña codificada, token JWT, etc.).
     */
    User saveUser(User user);

    /**
     * Busca un usuario por su nombre de usuario (username).
     *
     * @param username El nombre de usuario a buscar. Debe ser único.
     * @return Un {@link Optional} que contiene el {@link User} si se encuentra,
     * o un {@link Optional} vacío si no existe un usuario con ese username.
     */
    Optional<User> findByUsername(String username);

    /**
     * Cambia el rol de un usuario existente, identificado por su nombre de usuario.
     * Implementa lógica para límites en la cantidad de ciertos roles (ej. ADMIN).
     *
     * @param newRole El nuevo {@link Role} a asignar al usuario.
     * @param username El nombre de usuario del usuario a modificar.
     * @throws org.springframework.security.core.userdetails.UsernameNotFoundException si el usuario no se encuentra.
     * @throws RuntimeException si se intenta exceder el límite de roles ADMIN o si se intenta degradar al admin principal.
     */
    void changeRole(Role newRole, String username);

    /**
     * Busca un usuario por su nombre de usuario y, si existe, genera un nuevo token JWT
     * y lo asigna al campo transitorio 'token' del objeto {@link User} devuelto.
     * Este método es útil después de una autenticación exitosa para proveer un token fresco.
     *
     * @param username El nombre de usuario a buscar.
     * @return El objeto {@link User} encontrado, con un token JWT fresco asignado al campo {@code token}.
     * @throws org.springframework.security.core.userdetails.UsernameNotFoundException si el usuario no existe.
     */
    User findByUserameReturnToken(String username);

    /**
     * Recupera una lista de todos los usuarios registrados en el sistema.
     * Destinado principalmente para uso administrativo.
     *
     * @return Una {@link List} de objetos {@link User}; puede estar vacía si no hay usuarios.
     */
    List<User> findAllUsers();

    /**
     * Busca un usuario por su ID.
     * Destinado principalmente para uso administrativo o cuando se conoce el ID.
     *
     * @param id El ID del usuario a buscar.
     * @return Un {@link Optional} que contiene el {@link User} si se encuentra,
     * o un {@link Optional} vacío si no.
     */
    Optional<User> findUserById(Long id);

    /**
     * Actualiza los datos de un usuario (ej. nombre, rol), invocado por un administrador.
     * No permite modificar el username ni la contraseña directamente por esta vía.
     * Contiene lógica para proteger al administrador principal y para el límite de administradores.
     *
     * @param userId El ID del usuario a actualizar.
     * @param userUpdateRequest Objeto {@link User} con los campos (ej. nombre, rol) que se desean actualizar.
     * @param currentAdminUsername El username del administrador que realiza la operación, para validaciones.
     * @return El objeto {@link User} actualizado.
     * @throws org.springframework.security.core.userdetails.UsernameNotFoundException si el usuario a actualizar no se encuentra.
     * @throws RuntimeException si se intentan realizar operaciones no permitidas (ej. modificar admin principal, exceder límite de admins).
     */
    User updateUserByAdmin(Long userId, User userUpdateRequest, String currentAdminUsername);

    /**
     * Elimina un usuario del sistema, invocado por un administrador.
     * Contiene lógica para proteger contra la eliminación del administrador principal.
     *
     * @param userIdToDelete El ID del usuario a eliminar.
     * @param currentAdminUsername El username del administrador que realiza la operación.
     * @throws org.springframework.security.core.userdetails.UsernameNotFoundException si el usuario a eliminar no se encuentra.
     * @throws RuntimeException si se intenta eliminar al administrador principal.
     */
    void deleteUserByAdmin(Long userIdToDelete, String currentAdminUsername);
}