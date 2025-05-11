package com.dani.spring_boot_microservice_3_api_gateway.service;

import com.dani.spring_boot_microservice_3_api_gateway.model.Role;
import com.dani.spring_boot_microservice_3_api_gateway.model.User;
import com.dani.spring_boot_microservice_3_api_gateway.repository.UserRepository;
import com.dani.spring_boot_microservice_3_api_gateway.security.jwt.JwtProvider;
import lombok.RequiredArgsConstructor; // Importar
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // Importar

import java.time.LocalDateTime;
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
}