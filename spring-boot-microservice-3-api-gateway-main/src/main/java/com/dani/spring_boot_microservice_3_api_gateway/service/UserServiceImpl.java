package com.dani.spring_boot_microservice_3_api_gateway.service;

import com.dani.spring_boot_microservice_3_api_gateway.model.Role;
import com.dani.spring_boot_microservice_3_api_gateway.model.User;
import com.dani.spring_boot_microservice_3_api_gateway.repository.UserRepository;
import com.dani.spring_boot_microservice_3_api_gateway.security.jwt.JwtProvider;
import lombok.RequiredArgsConstructor; // Importar
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // Importar

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Implementación del servicio para la gestión de usuarios.
 */
@Service
@RequiredArgsConstructor // Lombok para inyección por constructor
public class UserServiceImpl implements UserService {

    // Inyección por constructor (campos final)
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    // Nombre de usuario del administrador principal (desde application.properties)
    // Deberás añadir esta propiedad a tu application.properties del api-gateway
    @Value("${app.security.principal-admin-username}")
    private String PRINCIPAL_ADMIN_USERNAME;

    private static final int MAX_ADMIN_USERS = 3;
    /**
     * {@inheritDoc}
     * Codifica la contraseña, asigna rol USER y fecha de creación por defecto.
     * Genera y asigna un token JWT inicial al usuario creado.
     */
    @Override
    @Transactional // Operación de escritura
    public User saveUser(User user) {
        // Codifica la contraseña antes de guardarla
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        // Asigna rol por defecto
        user.setRole(Role.USER);
        // Asigna fecha de creación
        user.setFechaCreacion(LocalDateTime.now());

        // Guarda el usuario en la base de datos
        User userCreated = userRepository.save(user);

        // Genera un token JWT inicial para el usuario recién creado
        String jwt = jwtProvider.generateToken(userCreated);
        userCreated.setToken(jwt); // Asigna el token al campo transitorio

        return userCreated;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true) // Operación de solo lectura
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    /**
     * {@inheritDoc}
     */
    @Transactional // Operación de modificación (UPDATE)
    @Override
    public void changeRole(Role newRole, String username) {
        // Añadimos validación para el límite de admins si el usuario intenta auto-promoverse a ADMIN.
        if (newRole == Role.ADMIN) {
            User userToChange = userRepository.findByUsername(username)
                    .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + username));
            // Solo validar si está cambiando A admin y el usuario NO es ya admin.
            if (userToChange.getRole() != Role.ADMIN) {
                long adminCount = userRepository.countByRole(Role.ADMIN);
                if (adminCount >= MAX_ADMIN_USERS) {
                    throw new RuntimeException("No se puede añadir más administradores. Límite de " + MAX_ADMIN_USERS + " alcanzado.");
                }
            }
        }
        userRepository.updateUserRole(username, newRole);
    }

    /**
     * {@inheritDoc}
     * Busca un usuario y, si existe, genera y le asigna un nuevo token JWT.
     */
    @Override
    @Transactional(readOnly = true) // Solo lectura, aunque genera token (no persiste el token aquí)
    public User findByUserameReturnToken(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("El usuario no fue encontrado:" + username));
        // Si el usuario existe, genera un nuevo token JWT.
        String jwt = jwtProvider.generateToken(user);
        user.setToken(jwt); // Asigna al campo transitorio
        return user;
    }


    // --- IMPLEMENTACIÓN DE NUEVOS MÉTODOS ---
    @Override
    @Transactional(readOnly = true)
    public List<User> findAllUsers() {
        return userRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findUserById(Long id) {
        return userRepository.findById(id);
    }

    @Override
    @Transactional
    public User updateUserByAdmin(Long userId, User userUpdateRequest, String currentAdminUsername) {
        User userToUpdate = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado con ID: " + userId));

        // Regla: El admin principal no puede ser modificado por esta función
        if (userToUpdate.getUsername().equals(PRINCIPAL_ADMIN_USERNAME)) {
            // Permitir que el admin principal se modifique a sí mismo (por ejemplo, su nombre), pero no su rol por otro admin.
            // Si el currentAdminUsername es diferente al PRINCIPAL_ADMIN_USERNAME, entonces no puede modificar al PRINCIPAL_ADMIN_USERNAME.
            if (!currentAdminUsername.equals(PRINCIPAL_ADMIN_USERNAME)) {
                throw new RuntimeException("El administrador principal (" + PRINCIPAL_ADMIN_USERNAME + ") no puede ser modificado por otros administradores.");
            }
            // Si es el admin principal modificándose a sí mismo, no le permitimos cambiar su rol a USER.
            if (userUpdateRequest.getRole() == Role.USER && currentAdminUsername.equals(PRINCIPAL_ADMIN_USERNAME)) {
                throw new RuntimeException("El administrador principal no puede cambiar su propio rol a USER.");
            }
        }

        // Validar límite de administradores si se está intentando cambiar/asignar a ROLE_ADMIN
        // y el usuario no es ya un admin.
        if (userUpdateRequest.getRole() == Role.ADMIN && userToUpdate.getRole() != Role.ADMIN) {
            long adminCount = userRepository.countByRole(Role.ADMIN);
            if (adminCount >= MAX_ADMIN_USERS) {
                throw new RuntimeException("No se puede promover a más administradores. Límite de " + MAX_ADMIN_USERS + " alcanzado.");
            }
        }

        // Actualizar campos permitidos (ej. nombre, rol)
        // No actualizamos username ni contraseña aquí por simplicidad, podría ser una función separada.
        if (userUpdateRequest.getNombre() != null) {
            userToUpdate.setNombre(userUpdateRequest.getNombre());
        }
        if (userUpdateRequest.getRole() != null) {
            userToUpdate.setRole(userUpdateRequest.getRole());
        }
        // Si se quiere permitir cambiar la contraseña, se haría aquí, encriptándola.
        // if (userUpdateRequest.getPassword() != null && !userUpdateRequest.getPassword().isEmpty()) {
        //    userToUpdate.setPassword(passwordEncoder.encode(userUpdateRequest.getPassword()));
        // }

        return userRepository.save(userToUpdate);
    }

    @Override
    @Transactional
    public void deleteUserByAdmin(Long userIdToDelete, String currentAdminUsername) {
        User userToDelete = userRepository.findById(userIdToDelete)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado con ID: " + userIdToDelete));

        // Regla: El admin principal no puede ser eliminado
        if (userToDelete.getUsername().equals(PRINCIPAL_ADMIN_USERNAME)) {
            throw new RuntimeException("El administrador principal (" + PRINCIPAL_ADMIN_USERNAME + ") no puede ser eliminado.");
        }

        userRepository.delete(userToDelete);
    }
}