package com.dani.spring_boot_microservice_3_api_gateway.service;

import com.dani.spring_boot_microservice_3_api_gateway.model.Role;
import com.dani.spring_boot_microservice_3_api_gateway.model.User;
import com.dani.spring_boot_microservice_3_api_gateway.repository.UserRepository;
import com.dani.spring_boot_microservice_3_api_gateway.security.jwt.JwtProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Implementación concreta de la interfaz {@link UserService}.
 * Contiene la lógica de negocio para la gestión de usuarios,
 * incluyendo la creación, búsqueda, modificación de roles y eliminación.
 * Interactúa con {@link UserRepository} para la persistencia de datos,
 * {@link PasswordEncoder} para la codificación de contraseñas,
 * y {@link JwtProvider} para la generación de tokens JWT.
 *
 * @author Daniel Núñez Rojas (danidev fullstack software)
 * @version 1.3
 * @since 2025-06-05 (JavaDoc completo para todos los métodos)
 */
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    @Value("${app.security.principal-admin-username}")
    private String PRINCIPAL_ADMIN_USERNAME;

    private static final int MAX_ADMIN_USERS = 3;

    /**
     * {@inheritDoc}
     * <p>
     * En esta implementación, al guardar un nuevo usuario:
     * <ul>
     * <li>La contraseña proporcionada se codifica usando {@link PasswordEncoder}.</li>
     * <li>Se asigna el rol {@link Role#USER} por defecto.</li>
     * <li>Se establece la {@code fechaCreacion} a la fecha y hora actuales.</li>
     * <li>Después de guardar en el repositorio, se genera un token JWT usando {@link JwtProvider}
     * y se asigna al campo transitorio {@code token} del usuario devuelto.</li>
     * </ul>
     * No se realizan validaciones de existencia previa del username aquí; se asume que
     * dicha validación (si es necesaria) se realiza antes de llamar a este método.
     */
    @Override
    @Transactional
    public User saveUser(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole(Role.USER);
        user.setFechaCreacion(LocalDateTime.now());

        User userCreated = userRepository.save(user);

        String jwt = jwtProvider.generateToken(userCreated);
        userCreated.setToken(jwt);

        return userCreated;
    }

    /**
     * {@inheritDoc}
     * Esta implementación simplemente delega la llamada al método {@code findByUsername}
     * del {@link UserRepository}.
     */
    @Override
    @Transactional(readOnly = true)
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Antes de actualizar el rol, esta implementación verifica:
     * <ul>
     * <li>Si el usuario existe.</li>
     * <li>Si se intenta promover a {@link Role#ADMIN} y el usuario aún no es ADMIN,
     * se comprueba que no se exceda el límite {@code MAX_ADMIN_USERS}.</li>
     * <li>(Implícito en la lógica de negocio) El administrador principal no puede ser degradado por esta vía si se añadieran más chequeos.</li>
     * </ul>
     */
    @Transactional
    @Override
    public void changeRole(Role newRole, String username) {
        User userToChange = userRepository.findByUsername(username).orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + username));

        if (newRole == Role.ADMIN) {
            if (userToChange.getRole() != Role.ADMIN) { // Solo contar si estamos promoviendo a un no-admin
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
     * <p>
     * Esta implementación primero busca al usuario por su nombre de usuario.
     * Si se encuentra, se genera un token JWT usando {@link JwtProvider} y se asigna
     * al campo transitorio {@code token} del objeto {@link User} antes de devolverlo.
     */
    @Override
    @Transactional(readOnly = true)
    public User findByUserameReturnToken(String username) { // Nota: Hay un typo en el nombre del método ("findByUserame")
        User user = userRepository.findByUsername(username).orElseThrow(() -> new UsernameNotFoundException("El usuario no fue encontrado:" + username));

        String jwt = jwtProvider.generateToken(user);
        user.setToken(jwt);
        return user;
    }

    /**
     * {@inheritDoc}
     * Esta implementación simplemente delega la llamada al método {@code findAll}
     * del {@link UserRepository}.
     */
    @Override
    @Transactional(readOnly = true)
    public List<User> findAllUsers() {
        return userRepository.findAll();
    }

    /**
     * {@inheritDoc}
     * Esta implementación simplemente delega la llamada al método {@code findById}
     * del {@link UserRepository}.
     */
    @Override
    @Transactional(readOnly = true)
    public Optional<User> findUserById(Long id) {
        return userRepository.findById(id);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Lógica específica de esta implementación:
     * <ul>
     * <li>Verifica si el usuario a actualizar es el administrador principal (definido por {@code PRINCIPAL_ADMIN_USERNAME}).
     * Si es así y el actualizador no es él mismo, se previene la modificación.
     * Si es él mismo, no se permite cambiar su rol a {@link Role#USER}.</li>
     * <li>Si se intenta cambiar el rol a {@link Role#ADMIN} para un usuario que no es ADMIN,
     * se verifica que no se exceda el límite {@code MAX_ADMIN_USERS}.</li>
     * <li>Solo se actualizan el nombre y el rol. Otros campos como username o contraseña
     * no se modifican mediante este método.</li>
     * </ul>
     */
    @Override
    @Transactional
    public User updateUserByAdmin(Long userId, User userUpdateRequest, String currentAdminUsername) {
        User userToUpdate = userRepository.findById(userId).orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado con ID: " + userId));

        if (userToUpdate.getUsername().equals(PRINCIPAL_ADMIN_USERNAME)) {
            if (!currentAdminUsername.equals(PRINCIPAL_ADMIN_USERNAME)) {
                throw new RuntimeException("El administrador principal (" + PRINCIPAL_ADMIN_USERNAME + ") no puede ser modificado por otros administradores.");
            }
            if (userUpdateRequest.getRole() == Role.USER && currentAdminUsername.equals(PRINCIPAL_ADMIN_USERNAME)) {
                throw new RuntimeException("El administrador principal no puede cambiar su propio rol a USER.");
            }
        }

        if (userUpdateRequest.getRole() == Role.ADMIN && userToUpdate.getRole() != Role.ADMIN) {
            long adminCount = userRepository.countByRole(Role.ADMIN);
            if (adminCount >= MAX_ADMIN_USERS) {
                throw new RuntimeException("No se puede promover a más administradores. Límite de " + MAX_ADMIN_USERS + " alcanzado.");
            }
        }

        if (userUpdateRequest.getNombre() != null) {
            userToUpdate.setNombre(userUpdateRequest.getNombre());
        }
        if (userUpdateRequest.getRole() != null) {
            // Si el usuario es el admin principal y se intenta cambiar su rol (y no es el admin principal modificándose a USER),
            // la lógica anterior ya lo habría prevenido o manejado. Aquí solo se aplica el rol si es válido.
            userToUpdate.setRole(userUpdateRequest.getRole());
        }

        return userRepository.save(userToUpdate);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Lógica específica de esta implementación:
     * <ul>
     * <li>Verifica si el usuario a eliminar es el administrador principal (definido por {@code PRINCIPAL_ADMIN_USERNAME}).
     * Si es así, se previene la eliminación lanzando una {@link RuntimeException}.</li>
     * </ul>
     */
    @Override
    @Transactional
    public void deleteUserByAdmin(Long userIdToDelete, String currentAdminUsername) {
        User userToDelete = userRepository.findById(userIdToDelete).orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado con ID: " + userIdToDelete));

        if (userToDelete.getUsername().equals(PRINCIPAL_ADMIN_USERNAME)) {
            throw new RuntimeException("El administrador principal (" + PRINCIPAL_ADMIN_USERNAME + ") no puede ser eliminado.");
        }

        userRepository.delete(userToDelete);
    }
}