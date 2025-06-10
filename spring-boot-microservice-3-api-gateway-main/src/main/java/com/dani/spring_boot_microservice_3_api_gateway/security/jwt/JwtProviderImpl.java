package com.dani.spring_boot_microservice_3_api_gateway.security.jwt;

import com.dani.spring_boot_microservice_3_api_gateway.model.User;
import com.dani.spring_boot_microservice_3_api_gateway.security.UserPrincipal;
import com.dani.spring_boot_microservice_3_api_gateway.utils.SecurityUtils;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Arrays;
import java.util.Date;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Implementación de la interfaz {@link JwtProvider} para generar y validar tokens JWT.
 * <p>
 * Esta clase utiliza la librería JJWT para la manipulación de tokens.
 * Configura la clave secreta y el tiempo de expiración de los tokens a través de
 * propiedades de la aplicación (inyectadas con {@link Value}).
 *
 * @see JwtProvider Interfaz que esta clase implementa.
 * @author Daniel Núñez Rojas (danidev fullstack software)
 * @version 1.1
 * @since 2025-05-04 // Fecha de creación o última modificación significativa
 */
@Component
public class JwtProviderImpl implements JwtProvider {

    @Value("${app.jwt.secret}")
    private String JWT_SECRET;

    @Value("${app.jwt.expiration-in-ms}")
    private Long JWT_EXPIRATION_IN_MS;

    /**
     * Clave secreta utilizada para firmar y verificar los tokens JWT.
     * Se deriva de la propiedad {@code app.jwt.secret}.
     * Es importante que esta clave sea suficientemente compleja y se mantenga segura.
     */
    private Key key;

    /**
     * Método de inicialización que se ejecuta después de la inyección de dependencias.
     * Genera la {@link Key} a partir de la cadena {@code JWT_SECRET}.
     */
    @jakarta.annotation.PostConstruct
    protected void init() {
        this.key = Keys.hmacShaKeyFor(JWT_SECRET.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * {@inheritDoc}
     * <p>
     * Los claims del token incluyen:
     * <ul>
     * <li>Subject: Nombre de usuario (obtenido de {@link UserPrincipal#getUsername()})</li>
     * <li>"roles": Autoridades del usuario (como una cadena separada por comas)</li>
     * <li>"userId": ID del usuario (obtenido de {@link UserPrincipal#getId()})</li>
     * </ul>
     * La expiración se calcula sumando {@code JWT_EXPIRATION_IN_MS} al tiempo actual.
     * El token se firma usando el algoritmo HS512 y la clave secreta.
     */
    @Override
    public String generateToken(UserPrincipal auth) {
        String authorities = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        return Jwts.builder()
                .setSubject(auth.getUsername())
                .claim("roles", authorities)
                .claim("userId", auth.getId())
                .setExpiration(new Date(System.currentTimeMillis() + JWT_EXPIRATION_IN_MS))
                .signWith(key, SignatureAlgorithm.HS512) // Usa la clave generada en init()
                .compact();
    }

    /**
     * {@inheritDoc}
     * <p>
     * Los claims del token incluyen:
     * <ul>
     * <li>Subject: Nombre de usuario (obtenido de {@link User#getUsername()})</li>
     * <li>"roles": El rol del usuario (como el nombre del enum, ej. "USER")</li>
     * <li>"userId": ID del usuario (obtenido de {@link User#getId()})</li>
     * </ul>
     * La expiración y firma son análogas al método {@link #generateToken(UserPrincipal)}.
     */
    @Override
    public String generateToken(User user) {
        return Jwts.builder()
                .setSubject(user.getUsername())
                .claim("roles", user.getRole().name())
                .claim("userId", user.getId())
                .setExpiration(new Date(System.currentTimeMillis() + JWT_EXPIRATION_IN_MS))
                .signWith(key, SignatureAlgorithm.HS512) // Usa la clave generada en init()
                .compact();
    }

    /**
     * {@inheritDoc}
     * <p>
     * Esta implementación extrae el token del header "Authorization" (tipo Bearer)
     * usando {@link SecurityUtils#extractAuthTokenFromRequest(HttpServletRequest)}.
     * Luego, parsea el token para obtener los claims (username, userId, roles).
     * Con esta información, construye un {@link UserPrincipal} y, a partir de él,
     * un {@link UsernamePasswordAuthenticationToken} que representa la autenticación.
     * Si el token es inválido o no se puede parsear, devuelve {@code null}.
     */
    @Override
    public Authentication getAuthentication(HttpServletRequest request) {
        Claims claims = extractClaims(request);
        if (claims == null) {
            return null;
        }

        String username = claims.getSubject();
        if (username == null) {
            return null;
        }

        Long userId = claims.get("userId", Long.class); // Extraer userId como Long

        Set<GrantedAuthority> authorities = Arrays.stream(claims.get("roles").toString().split(","))
                .map(SecurityUtils::convertToAuthority)
                .collect(Collectors.toSet());

        UserDetails userDetails = UserPrincipal.builder()
                .username(username)
                .authorities(authorities)
                .id(userId)
                // La contraseña no se almacena en el token, por lo que no se establece aquí
                // para el UserPrincipal construido a partir del token.
                .build();

        return new UsernamePasswordAuthenticationToken(userDetails, null, authorities);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Esta implementación extrae los claims del token. Si la extracción es exitosa
     * (el token existe y es parseable con la clave secreta), verifica que la fecha
     * de expiración del token no sea anterior a la fecha actual.
     *
     * @see #extractClaims(HttpServletRequest)
     */
    @Override
    public boolean istTokenValid(HttpServletRequest request) { // Manteniendo el typo "ist"
        Claims claims = extractClaims(request);
        if (claims == null) {
            return false;
        }
        return !claims.getExpiration().before(new Date());
    }

    /**
     * Extrae los claims (cuerpo/payload) de un token JWT a partir de una {@link HttpServletRequest}.
     * El token se espera en el header "Authorization" con el prefijo "Bearer ".
     *
     * @param request La petición HTTP entrante.
     * @return Un objeto {@link Claims} si el token se pudo extraer y parsear/validar correctamente
     * con la clave secreta; {@code null} en caso contrario (ej. token no presente, firma inválida).
     */
    private Claims extractClaims(HttpServletRequest request) {
        String token = SecurityUtils.extractAuthTokenFromRequest(request);
        if (token == null) {
            return null;
        }
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(key) // Usa la clave generada en init()
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception e) {
            // En un entorno real, sería bueno loggear esta excepción con nivel DEBUG o WARN
            // para diagnosticar problemas con tokens inválidos.
            // log.warn("Error al parsear o validar token JWT: {}", e.getMessage());
            return null;
        }
    }
}