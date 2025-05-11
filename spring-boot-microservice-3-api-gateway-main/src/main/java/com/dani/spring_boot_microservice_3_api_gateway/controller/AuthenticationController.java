package com.dani.spring_boot_microservice_3_api_gateway.controller;

import com.dani.spring_boot_microservice_3_api_gateway.model.User;
import com.dani.spring_boot_microservice_3_api_gateway.service.AuthenticationService;
import com.dani.spring_boot_microservice_3_api_gateway.service.UserService;
import lombok.RequiredArgsConstructor; // Importar
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// Imports OpenAPI
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Controlador REST para manejar la autenticación de usuarios (registro e inicio de sesión).
 */
@RestController
@RequestMapping("api/authentication")
@RequiredArgsConstructor // Lombok para inyección por constructor
@Tag(name = "Autenticación", description = "API para registro (Sign-Up) e inicio de sesión (Sign-In) de usuarios.")
public class AuthenticationController {

    // Inyección por constructor (final)
    private final AuthenticationService authenticationService;
    private final UserService userService;

    /**
     * Endpoint para registrar un nuevo usuario.
     * Verifica si el nombre de usuario ya existe antes de intentar guardar.
     * @param user Datos del usuario a registrar (username, password, nombre).
     * @return ResponseEntity con el usuario creado (incluyendo token inicial) y estado CREATED,
     * o estado CONFLICT si el usuario ya existe.
     */
    @PostMapping("sign-up")
    @Operation(summary = "Registrar un nuevo usuario", description = "Crea una nueva cuenta de usuario en el sistema.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Usuario registrado exitosamente",
                    content = { @Content(mediaType = "application/json", schema = @Schema(implementation = User.class)) }),
            @ApiResponse(responseCode = "409", description = "Conflicto: El nombre de usuario ya existe", content = @Content),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor", content = @Content)
    })
    // ResponseEntity específico <User>
    public ResponseEntity<User> signUp(@RequestBody User user) {
        // Evaluar si el usuario registrado existe
        if (userService.findByUsername(user.getUsername()).isPresent()) {
            // Devolver 409 Conflict si el usuario ya existe
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }
        // Guardar usuario y devolver 201 Created
        return new ResponseEntity<>(userService.saveUser(user), HttpStatus.CREATED);
    }

    /**
     * Endpoint para iniciar sesión (autenticar) un usuario existente.
     * @param user Credenciales del usuario (username y password).
     * @return ResponseEntity con los datos del usuario autenticado (incluyendo token JWT) y estado OK.
     */
    @PostMapping("sign-in")
    @Operation(summary = "Iniciar sesión de usuario", description = "Autentica a un usuario con su username y password, y devuelve un token JWT si es exitoso.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Inicio de sesión exitoso",
                    content = { @Content(mediaType = "application/json", schema = @Schema(implementation = User.class)) }),
            @ApiResponse(responseCode = "401", description = "Credenciales inválidas (Unauthorized)", content = @Content), // Spring Security suele devolver 401 por defecto
            @ApiResponse(responseCode = "500", description = "Error interno del servidor", content = @Content)
    })
    // ResponseEntity específico <User>
    public ResponseEntity<User> signIn(@RequestBody User user) {
        // El AuthenticationService maneja la lógica de autenticación y generación de JWT
        User authenticatedUser = authenticationService.signInAndReturnJWT(user);
        return new ResponseEntity<>(authenticatedUser, HttpStatus.OK);
    }
}