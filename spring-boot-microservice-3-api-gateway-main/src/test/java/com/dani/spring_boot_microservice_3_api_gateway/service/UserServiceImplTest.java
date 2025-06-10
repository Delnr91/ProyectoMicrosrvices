package com.dani.spring_boot_microservice_3_api_gateway.service;

import com.dani.spring_boot_microservice_3_api_gateway.model.Role;
import com.dani.spring_boot_microservice_3_api_gateway.model.User;
import com.dani.spring_boot_microservice_3_api_gateway.repository.UserRepository;
import com.dani.spring_boot_microservice_3_api_gateway.security.jwt.JwtProvider;
import org.junit.jupiter.api.BeforeEach; // Puedes quitar este import si no lo usas directamente
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor; // Import para capturar argumentos
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
@ExtendWith(MockitoExtension.class) // Habilita las anotaciones de Mockito para esta clase de prueba
class UserServiceImplTest {

    @Mock // Crea un mock (objeto simulado) para UserRepository
    private UserRepository userRepositoryMock;

    @Mock // Crea un mock para PasswordEncoder (aunque no lo usemos en findByUsername, es dependencia del constructor)
    private PasswordEncoder passwordEncoderMock;

    @Mock // Crea un mock para JwtProvider (ídem)
    private JwtProvider jwtProviderMock;

    @InjectMocks // Crea una instancia de UserServiceImpl e inyecta los mocks creados arriba en ella
    private UserServiceImpl userService;

    // Podríamos usar @BeforeEach para configuraciones comunes a varias pruebas,
    // pero por ahora @InjectMocks es suficiente para este caso.

    @Test
    void findByUsername_cuandoUsuarioExiste_deberiaRetornarUsuario() {
        // 1. Arrange (Organizar/Preparar los datos y condiciones de la prueba)

        // Definimos un nombre de usuario para la prueba
        String usernameExistente = "testuser";

        // Creamos un objeto User de ejemplo que esperamos que el mock devuelva
        User usuarioEsperado = new User();
        usuarioEsperado.setId(1L);
        usuarioEsperado.setUsername(usernameExistente);
        usuarioEsperado.setNombre("Test User");
        usuarioEsperado.setPassword("encodedPassword"); // No importa el valor real aquí para findByUsername
        usuarioEsperado.setRole(Role.USER);
        usuarioEsperado.setFechaCreacion(LocalDateTime.now());

        // Le decimos a Mockito: "Cuando se llame al método findByUsername del userRepositoryMock
        // con el argumento usernameExistente, entonces devuelve un Optional que contiene usuarioEsperado"
        when(userRepositoryMock.findByUsername(usernameExistente)).thenReturn(Optional.of(usuarioEsperado));

        // 2. Act (Actuar/Ejecutar el método que queremos probar)
        Optional<User> resultadoOptional = userService.findByUsername(usernameExistente);

        // 3. Assert (Afirmar/Verificar que el resultado es el esperado)
        assertTrue(resultadoOptional.isPresent(), "El Optional no debería estar vacío si el usuario existe");
        User usuarioEncontrado = resultadoOptional.get();

        assertEquals(usuarioEsperado.getId(), usuarioEncontrado.getId(), "El ID del usuario no coincide");
        assertEquals(usuarioEsperado.getUsername(), usuarioEncontrado.getUsername(), "El username no coincide");
        assertEquals(usuarioEsperado.getNombre(), usuarioEncontrado.getNombre(), "El nombre no coincide");
        assertEquals(usuarioEsperado.getRole(), usuarioEncontrado.getRole(), "El rol no coincide");

        // Verificamos que el método findByUsername del mock fue llamado exactamente una vez con el username correcto
        verify(userRepositoryMock, times(1)).findByUsername(usernameExistente);
    }

    // Aquí podemos añadir otro test para el caso en que el usuario NO existe
    @Test
    void findByUsername_cuandoUsuarioNoExiste_deberiaRetornarOptionalVacio() {
        // 1. Arrange
        String usernameNoExistente = "usuarioQueNoExiste";

        // Le decimos a Mockito: "Cuando se llame al método findByUsername del userRepositoryMock
        // con el argumento usernameNoExistente, entonces devuelve un Optional vacío"
        when(userRepositoryMock.findByUsername(usernameNoExistente)).thenReturn(Optional.empty());

        // 2. Act
        Optional<User> resultadoOptional = userService.findByUsername(usernameNoExistente);

        // 3. Assert
        assertFalse(resultadoOptional.isPresent(), "El Optional debería estar vacío si el usuario no existe");
        assertTrue(resultadoOptional.isEmpty(), "El Optional debería estar vacío si el usuario no existe"); // Alternativa a assertFalse

        // Verificamos que el método findByUsername del mock fue llamado exactamente una vez
        verify(userRepositoryMock, times(1)).findByUsername(usernameNoExistente);
    }

    @Test
    void saveUser_cuandoUsuarioEsNuevo_deberiaGuardarCorrectamente() {
        // 1. Arrange (Organizar/Preparar)
        User usuarioParaGuardar = new User();
        usuarioParaGuardar.setUsername("nuevoUsuario");
        usuarioParaGuardar.setNombre("Nuevo Usuario de Prueba");
        usuarioParaGuardar.setPassword("passwordPlano"); // Contraseña original en texto plano

        String passwordHasheadaSimulada = "hashedPassword123";
        String tokenJwtSimulado = "token.jwt.simulado.valor";
        long idUsuarioGenerado = 1L; // ID que simularemos que la BD asigna

        // Configurar el mock del PasswordEncoder
        when(passwordEncoderMock.encode("passwordPlano")).thenReturn(passwordHasheadaSimulada);

        // Configurar el mock del UserRepository para capturar el User y asignarle un ID
        ArgumentCaptor<User> userArgumentCaptor = ArgumentCaptor.forClass(User.class);
        // Cuando se llame a save(), devolvemos el mismo usuario que se pasó, pero después de asignarle un ID.
        // El userArgumentCaptor nos permitirá verificar el estado del User JUSTO ANTES de que se guarde.
        when(userRepositoryMock.save(userArgumentCaptor.capture())).thenAnswer(invocation -> {
            User userCapturado = invocation.getArgument(0); // Es el mismo que userArgumentCaptor.getValue() después de la llamada
            userCapturado.setId(idUsuarioGenerado); // Simulamos que la BD asigna un ID
            return userCapturado;
        });

        // Configurar el mock del JwtProvider
        // Usaremos un ArgumentCaptor para asegurar que el token se genera para el usuario con el ID ya asignado.
        ArgumentCaptor<User> userForTokenCaptor = ArgumentCaptor.forClass(User.class);
        when(jwtProviderMock.generateToken(userForTokenCaptor.capture())).thenReturn(tokenJwtSimulado);

        // AQUÍ COMIENZAN LOS NUEVOS CAMBIOS:
        // 2. Act (Actuar)
        // Llamamos al método saveUser de nuestro servicio, que es la unidad que estamos probando.
        User usuarioGuardado = userService.saveUser(usuarioParaGuardar);

        // 3. Assert (Afirmar/Verificar)
        // Verificar que el usuario devuelto no sea nulo
        assertNotNull(usuarioGuardado, "El usuario guardado no debería ser nulo.");

        // Verificar que el ID fue asignado (como simulamos en el mock del repositorio)
        assertEquals(idUsuarioGenerado, usuarioGuardado.getId(), "El ID del usuario guardado no es el esperado.");

        // Verificar que los datos originales (username, nombre) se mantuvieron
        assertEquals("nuevoUsuario", usuarioGuardado.getUsername(), "El username no coincide.");
        assertEquals("Nuevo Usuario de Prueba", usuarioGuardado.getNombre(), "El nombre no coincide.");

        // Verificar que la contraseña fue codificada y asignada
        assertEquals(passwordHasheadaSimulada, usuarioGuardado.getPassword(), "La contraseña no fue codificada correctamente.");

        // Verificar que el rol por defecto USER fue asignado
        assertEquals(Role.USER, usuarioGuardado.getRole(), "El rol asignado no es USER.");

        // Verificar que la fecha de creación fue asignada (al menos que no sea nula)
        assertNotNull(usuarioGuardado.getFechaCreacion(), "La fecha de creación no debería ser nula.");

        // Verificar que el token JWT fue generado y asignado
        assertEquals(tokenJwtSimulado, usuarioGuardado.getToken(), "El token JWT no fue asignado o no es el esperado.");

        // Verificaciones de Interacción con los Mocks (muy importantes)

        // Verificar que passwordEncoder.encode() fue llamado una vez con la contraseña original
        verify(passwordEncoderMock, times(1)).encode("passwordPlano");

        // Verificar que userRepository.save() fue llamado una vez.
        // El objeto User que se pasó a save() fue capturado por userArgumentCaptor.
        // Podemos verificar sus propiedades justo antes de ser "guardado":
        verify(userRepositoryMock, times(1)).save(any(User.class)); // O usar el captor: verify(userRepositoryMock).save(userArgumentCaptor.capture());
        User usuarioPasadoARepo = userArgumentCaptor.getValue();
        assertNotNull(usuarioPasadoARepo, "El usuario pasado al repositorio no debería ser nulo.");
        assertEquals(passwordHasheadaSimulada, usuarioPasadoARepo.getPassword(), "La contraseña del usuario pasado al repo no estaba hasheada.");
        assertEquals(Role.USER, usuarioPasadoARepo.getRole(), "El rol del usuario pasado al repo no era USER.");
        assertNotNull(usuarioPasadoARepo.getFechaCreacion(), "La fecha de creación del usuario pasado al repo era nula.");

        // Verificar que jwtProvider.generateToken() fue llamado una vez.
        // El objeto User que se pasó a generateToken() fue capturado por userForTokenCaptor.
        // Debería ser el usuario ya con el ID asignado por el mock del repositorio.
        verify(jwtProviderMock, times(1)).generateToken(any(User.class)); // O usar el captor: verify(jwtProviderMock).generateToken(userForTokenCaptor.capture());
        User usuarioParaToken = userForTokenCaptor.getValue();
        assertNotNull(usuarioParaToken, "El usuario pasado al provider de token no debería ser nulo.");
        assertEquals(idUsuarioGenerado, usuarioParaToken.getId(), "El usuario pasado al provider de token no tenía el ID esperado.");
    }

    @Test
    void changeRole_cuandoUsuarioExisteYHayEspacioAdmin_deberiaCambiarRolAAdmin() {
        // 1. Arrange (Organizar/Preparar)
        String usernameExistente = "userParaSerAdmin";
        User usuarioActual = new User();
        usuarioActual.setId(2L);
        usuarioActual.setUsername(usernameExistente);
        usuarioActual.setRole(Role.USER); // El usuario es actualmente USER

        // Simular que el usuario existe
        when(userRepositoryMock.findByUsername(usernameExistente)).thenReturn(Optional.of(usuarioActual));

        // Simular que hay menos de MAX_ADMIN_USERS (3) administradores actualmente
        // MAX_ADMIN_USERS es 3, así que un conteo de 0, 1, o 2 permitiría el cambio.
        when(userRepositoryMock.countByRole(Role.ADMIN)).thenReturn(1L); // Solo hay 1 admin

        // El método updateUserRole en el repositorio es void, así que no devuelve nada.
        // Usamos doNothing() para indicar que no haga nada (y no lance excepción) cuando se llame.
        // Usamos eq() para asegurar que los argumentos son exactamente los esperados.
        doNothing().when(userRepositoryMock).updateUserRole(eq(usernameExistente), eq(Role.ADMIN));

        // 2. Act (Actuar)
        // Intentamos cambiar el rol del usuario a ADMIN.
        // Como esperamos que esta operación sea exitosa (no lance excepción), no necesitamos assertDoesNotThrow aquí,
        // simplemente la ejecución sin errores es parte de la prueba.
        userService.changeRole(Role.ADMIN, usernameExistente);

        // 3. Assert (Afirmar/Verificar)
        // Verificamos que el método updateUserRole del mock fue llamado exactamente una vez
        // y con los argumentos correctos.
        verify(userRepositoryMock, times(1)).updateUserRole(usernameExistente, Role.ADMIN);

        // Adicionalmente, verificamos que se consultó el conteo de admins
        verify(userRepositoryMock, times(1)).countByRole(Role.ADMIN);

        // Verificamos que se buscó al usuario
        verify(userRepositoryMock, times(1)).findByUsername(usernameExistente);
    }

    @Test
    void changeRole_cuandoLimiteDeAdminAlcanzado_deberiaLanzarRuntimeException() {
        // 1. Arrange (Organizar/Preparar)
        String usernameExistente = "userQueQuiereSerAdmin";
        User usuarioActual = new User();
        usuarioActual.setId(3L);
        usuarioActual.setUsername(usernameExistente);
        usuarioActual.setRole(Role.USER); // El usuario es actualmente USER

        // Simular que el usuario existe
        when(userRepositoryMock.findByUsername(usernameExistente)).thenReturn(Optional.of(usuarioActual));

        // Simular que ya hay MAX_ADMIN_USERS (3) o más administradores
        // MAX_ADMIN_USERS es 3 en UserServiceImpl.
        when(userRepositoryMock.countByRole(Role.ADMIN)).thenReturn(3L);

        // 2. Act & 3. Assert (Actuar y Afirmar/Verificar juntos para excepciones)

        // Verificamos que se lanza una RuntimeException cuando se intenta cambiar el rol a ADMIN
        // y el límite de administradores ya ha sido alcanzado.
        RuntimeException exceptionLanzada = assertThrows(RuntimeException.class, () -> {
            userService.changeRole(Role.ADMIN, usernameExistente);
        });

        // Opcionalmente, podemos verificar el mensaje de la excepción
        assertEquals("No se puede añadir más administradores. Límite de 3 alcanzado.", exceptionLanzada.getMessage());

        // Verificamos que el método updateUserRole del mock NUNCA fue llamado,
        // ya que la excepción debería haber detenido el flujo antes.
        verify(userRepositoryMock, never()).updateUserRole(anyString(), any(Role.class));

        // Verificamos que sí se consultó el conteo de admins
        verify(userRepositoryMock, times(1)).countByRole(Role.ADMIN);

        // Verificamos que sí se buscó al usuario
        verify(userRepositoryMock, times(1)).findByUsername(usernameExistente);
    }

    @Test
    void deleteUserByAdmin_cuandoUsuarioNoEsAdminPrincipal_deberiaEliminarUsuario() {
        // 1. Arrange (Organizar/Preparar)
        Long userIdAEliminar = 5L;
        String usernameAEliminar = "usuarioAEliminar";
        String adminQueEjecuta = "testuser"; // Asumimos que testuser es un admin válido

        User usuarioAEliminarObj = new User();
        usuarioAEliminarObj.setId(userIdAEliminar);
        usuarioAEliminarObj.setUsername(usernameAEliminar); // Importante que NO sea el PRINCIPAL_ADMIN_USERNAME
        usuarioAEliminarObj.setRole(Role.USER);

        // Simular que el usuario a eliminar existe
        when(userRepositoryMock.findById(userIdAEliminar)).thenReturn(Optional.of(usuarioAEliminarObj));

        // Configurar el mock para el método delete (es void)
        // No es estrictamente necesario usar doNothing() para métodos void si solo verificas la llamada,
        // pero es una buena práctica para ser explícito.
        doNothing().when(userRepositoryMock).delete(usuarioAEliminarObj);

        // Para acceder a PRINCIPAL_ADMIN_USERNAME, que es un campo privado en UserServiceImpl,
        // necesitaríamos usar reflexión o inyectar el valor en el test si fuera una propiedad pública/configurable.
        // Por ahora, asumiremos que la lógica de UserServiceImpl lo comparará internamente.
        // En este test, nos aseguramos que el usernameAEliminar NO COINCIDA con el valor
        // que sabemos tiene PRINCIPAL_ADMIN_USERNAME en application.properties ("testuser").
        // Si usernameAEliminar fuera "testuser", este test debería probar el caso de no poder eliminarlo.
        // Para este caso, nos aseguramos que usernameAEliminar != "testuser".

        // 2. Act (Actuar)
        // No debería lanzar excepción
        assertDoesNotThrow(() -> {
            userService.deleteUserByAdmin(userIdAEliminar, adminQueEjecuta);
        });

        // 3. Assert (Afirmar/Verificar)
        // Verificar que el método findById fue llamado una vez
        verify(userRepositoryMock, times(1)).findById(userIdAEliminar);

        // Verificar que el método delete fue llamado una vez con el objeto usuario correcto
        verify(userRepositoryMock, times(1)).delete(usuarioAEliminarObj);
    }
    @Test
    void deleteUserByAdmin_cuandoSeIntentaEliminarAdminPrincipal_deberiaLanzarRuntimeException() {
        // 1. Arrange (Organizar/Preparar)
        Long idAdminPrincipal = 1L;
        String usernameAdminPrincipalEnProperties = "testuser"; // Este es el valor de tu application.properties
        String adminQueEjecuta = "otroAdmin";

        User adminPrincipalObj = new User();
        adminPrincipalObj.setId(idAdminPrincipal);
        adminPrincipalObj.setUsername(usernameAdminPrincipalEnProperties);
        adminPrincipalObj.setRole(Role.ADMIN);

        // Simular que el admin principal es encontrado
        when(userRepositoryMock.findById(idAdminPrincipal)).thenReturn(Optional.of(adminPrincipalObj));

        // === CAMBIO IMPORTANTE: Inyectar el valor de PRINCIPAL_ADMIN_USERNAME en el servicio bajo prueba ===
        ReflectionTestUtils.setField(userService, "PRINCIPAL_ADMIN_USERNAME", usernameAdminPrincipalEnProperties);
        // Esto asegura que dentro de UserServiceImpl, this.PRINCIPAL_ADMIN_USERNAME == "testuser"

        // 2. Act & 3. Assert (Actuar y Afirmar/Verificar juntos para excepciones)
        RuntimeException exceptionLanzada = assertThrows(RuntimeException.class, () -> {
            userService.deleteUserByAdmin(idAdminPrincipal, adminQueEjecuta);
        });

        // Verificar el mensaje de la excepción
        assertEquals("El administrador principal (" + usernameAdminPrincipalEnProperties + ") no puede ser eliminado.", exceptionLanzada.getMessage());

        // Verificar que el método findById fue llamado una vez
        verify(userRepositoryMock, times(1)).findById(idAdminPrincipal);

        // Verificar que el método delete NUNCA fue llamado
        verify(userRepositoryMock, never()).delete(any(User.class));
    }

    @Test
    void deleteUserByAdmin_cuandoUsuarioNoExiste_deberiaLanzarUsernameNotFoundException() {
        // 1. Arrange (Organizar/Preparar)
        Long idUsuarioNoExistente = 999L; // Un ID que simularemos que no existe
        String adminQueEjecuta = "testuser";

        // Simular que el usuario no es encontrado por findById
        when(userRepositoryMock.findById(idUsuarioNoExistente)).thenReturn(Optional.empty());

        // 2. Act & 3. Assert (Actuar y Afirmar/Verificar juntos para excepciones)
        UsernameNotFoundException exceptionLanzada = assertThrows(UsernameNotFoundException.class, () -> {
            userService.deleteUserByAdmin(idUsuarioNoExistente, adminQueEjecuta);
        });

        // Opcional: Verificar el mensaje de la excepción si es relevante y consistente
        assertEquals("Usuario no encontrado con ID: " + idUsuarioNoExistente, exceptionLanzada.getMessage());

        // Verificar que el método findById fue llamado una vez
        verify(userRepositoryMock, times(1)).findById(idUsuarioNoExistente);

        // Verificar que el método delete NUNCA fue llamado, ya que el usuario no fue encontrado
        verify(userRepositoryMock, never()).delete(any(User.class));
    }
}