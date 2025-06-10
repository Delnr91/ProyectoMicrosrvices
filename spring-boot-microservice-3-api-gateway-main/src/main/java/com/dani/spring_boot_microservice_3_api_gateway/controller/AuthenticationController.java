package com.dani.spring_boot_microservice_3_api_gateway.controller;

import com.dani.spring_boot_microservice_3_api_gateway.model.User;
import com.dani.spring_boot_microservice_3_api_gateway.service.AuthenticationService;
import com.dani.spring_boot_microservice_3_api_gateway.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador REST que gestiona los endpoints públicos de la API para la autenticación de usuarios.
 * Proporciona funcionalidades para el registro de nuevos usuarios (sign-up) y
 * el inicio de sesión de usuarios existentes (sign-in).
 *
 * @author Daniel Núñez Rojas (danidev fullstack software)
 * @version 1.1
 * @since 2025-06-05 (Actualizado con JavaDoc)
 */
@RestController
@RequestMapping("api/authentication")
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthenticationService authenticationService;
    private final UserService userService;

    /**
     * Endpoint para registrar un nuevo usuario en el sistema.
     * <p>
     * Antes de proceder con el guardado, verifica si el nombre de usuario proporcionado
     * ya está en uso. Si es así, devuelve un estado de conflicto para evitar duplicados.
     * Si el nombre de usuario está disponible, delega la creación del usuario al {@link UserService}.
     *
     * @param user El objeto {@link User} que contiene los datos del nuevo usuario,
     * recibido en el cuerpo de la petición. Se espera que contenga al menos
     * un nombre de usuario, una contraseña en texto plano y un nombre.
     * @return Un {@link ResponseEntity} que contiene el objeto {@link User} creado (con el ID asignado,
     * la contraseña codificada y un token JWT inicial) y el estado HTTP 201 (Created)
     * si el registro es exitoso. O devuelve un estado HTTP 409 (Conflict) si el
     * nombre de usuario ya existe.
     */
    @PostMapping("sign-up")
    public ResponseEntity<User> signUp(@RequestBody User user) {
        if (userService.findByUsername(user.getUsername()).isPresent()) {
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }
        return new ResponseEntity<>(userService.saveUser(user), HttpStatus.CREATED);
    }

    /**
     * Endpoint para autenticar a un usuario y generar un token JWT.
     * <p>
     * Recibe las credenciales del usuario (nombre de usuario y contraseña) y delega
     * el proceso de autenticación al {@link AuthenticationService}.
     *
     * @param user Un objeto {@link User} que contiene las credenciales (username y password)
     * para el intento de inicio de sesión, recibido en el cuerpo de la petición.
     * @return Un {@link ResponseEntity} que contiene el objeto {@link User} completo del usuario
     * autenticado (con el campo transitorio 'token' poblado con un nuevo JWT) y el
     * estado HTTP 200 (OK).
     * @throws org.springframework.security.core.AuthenticationException si las credenciales son inválidas,
     * lanzada por el AuthenticationManager subyacente.
     */
    @PostMapping("sign-in")
    public ResponseEntity<User> signIn(@RequestBody User user) {
        User authenticatedUser = authenticationService.signInAndReturnJWT(user);
        return new ResponseEntity<>(authenticatedUser, HttpStatus.OK);
    }
}