package com.dani.spring_boot_microservice_3_api_gateway.security.jwt;

// --- Imports Añadidos ---
import com.dani.spring_boot_microservice_3_api_gateway.model.User; // Importar la entidad User
import com.dani.spring_boot_microservice_3_api_gateway.security.UserPrincipal; // Importar UserPrincipal
// --- Fin Imports Añadidos ---

import com.dani.spring_boot_microservice_3_api_gateway.utils.SecurityUtils;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest; // Import actualizado a jakarta
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
 * Implementación de la interfaz JwtProvider para generar y validar tokens JWT.
 */
@Component
public class JwtProviderImpl implements JwtProvider {

    @Value("${app.jwt.secret}")
    private String JWT_SECRET;

    @Value("${app.jwt.expiration-in-ms}")
    private Long JWT_EXPIRATION_IN_MS;

    /**
     * Genera un token JWT basado en los detalles de un UserPrincipal autenticado.
     * Incluye username, roles y userId en los claims del token.
     *
     * @param auth El UserPrincipal autenticado.
     * @return El token JWT generado como String.
     */
    @Override
    public String generateToken(UserPrincipal auth) { // Usa UserPrincipal importado

        String authorities = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        // Genera la clave de firma a partir del secreto configurado.
        Key key = Keys.hmacShaKeyFor(JWT_SECRET.getBytes(StandardCharsets.UTF_8));

        // Construye el token JWT.
        return Jwts.builder()
                .setSubject(auth.getUsername())
                .claim("roles", authorities)
                .claim("userId", auth.getId())
                .setExpiration(new Date(System.currentTimeMillis() + JWT_EXPIRATION_IN_MS))
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();
    }

    /**
     * Genera un token JWT directamente desde un objeto User.
     * Útil después de registrar un usuario o para otras operaciones internas.
     *
     * @param user El objeto User para el cual generar el token.
     * @return El token JWT generado como String.
     */
    @Override
    public String generateToken(User user) { // Usa User importado

        Key key = Keys.hmacShaKeyFor(JWT_SECRET.getBytes(StandardCharsets.UTF_8));
        return Jwts.builder()
                .setSubject(user.getUsername())
                .claim("roles", user.getRole().name()) // Asume que getRole() devuelve el Enum Role
                .claim("userId", user.getId())
                .setExpiration(new Date(System.currentTimeMillis() + JWT_EXPIRATION_IN_MS))
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();
    }

    /**
     * Intenta extraer y validar un token JWT de una HttpServletRequest y,
     * si es válido, construye un objeto Authentication para Spring Security.
     *
     * @param request La petición HTTP entrante.
     * @return Un objeto Authentication si el token es válido, o null si no hay token o no es válido.
     */
    @Override
    public Authentication getAuthentication(HttpServletRequest request) { // Usa HttpServletRequest importado

        Claims claims = extractClaims(request);
        if (claims == null) {
            return null;
        }

        String username = claims.getSubject();
        Long userId = claims.get("userId", Long.class);

        // Extrae roles del token y los convierte a GrantedAuthority.
        Set<GrantedAuthority> authorities = Arrays.stream(claims.get("roles").toString().split(","))
                .map(SecurityUtils::convertToAuthority)
                .collect(Collectors.toSet());

        // Construye un UserDetails (UserPrincipal) a partir de la información del token.
        UserDetails userDetails = UserPrincipal.builder()
                .username(username)
                .authorities(authorities)
                .id(userId)
                .build();

        if (username == null) {
            return null;
        }

        // Retorna un objeto Authentication que Spring Security puede usar.
        return new UsernamePasswordAuthenticationToken(userDetails, null, authorities);
    }

    /**
     * Verifica si hay un token JWT válido en la HttpServletRequest.
     * Comprueba si el token existe y si no ha expirado.
     *
     * @param request La petición HTTP entrante.
     * @return true si el token es válido, false en caso contrario.
     */
    @Override
    public boolean istTokenValid(HttpServletRequest request) { // Usa HttpServletRequest importado

        Claims claims = extractClaims(request);
        if (claims == null) {
            return false;
        }
        // Verifica si la fecha de expiración es anterior a la fecha actual.
        if (claims.getExpiration().before(new Date())) {
            return false;
        }

        return true;
    }

    /**
     * Método privado auxiliar para extraer los claims (cuerpo) de un token JWT
     * a partir de una HttpServletRequest.
     *
     * @param request La petición HTTP entrante.
     * @return Los Claims del token si se pudo extraer y parsear, o null si no.
     */
    private Claims extractClaims(HttpServletRequest request) { // Usa HttpServletRequest importado

        // Usa SecurityUtils para extraer el token del header 'Authorization'.
        String token = SecurityUtils.extractAuthTokenFromRequest(request);
        if (token == null) {
            return null;
        }

        // Crea la clave de firma para validar el token.
        Key key = Keys.hmacShaKeyFor(JWT_SECRET.getBytes(StandardCharsets.UTF_8));

        // Parsea el token y extrae los claims. Si la firma o la estructura son inválidas, lanzará una excepción.
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception e) {
            // Podríamos loggear la excepción si queremos más detalles sobre por qué falló el parseo.
            // log.error("Error al parsear JWT: {}", e.getMessage());
            return null; // Si hay cualquier error al parsear, consideramos los claims como no extraíbles.
        }
    }
}