package co.edu.uniquindio.config;

import co.edu.uniquindio.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
public class JwtTokenFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    public JwtTokenFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }


    /// / Filtro que verifica el token JWT en cada petición:
    /// / 1. Extrae el token del header 'Authorization'
    /// / 2. Valida su estructura (debe comenzar con "Bearer ")
    /// / 3. Si es válido, extrae el ID de usuario y su rol
    /// / 4. Crea y guarda la autenticación en el contexto de seguridad
    /// / 5. Si falla, devuelve error 401 (No autorizado)
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String header = request.getHeader("Authorization");

        if (header == null || !header.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }
        String token = header.replace("Bearer ", "");
        try {
            if (jwtUtil.validateToken(token)) {
                String userId = jwtUtil.extractUserId(token);
                Claims claims = jwtUtil.extractAllClaims(token);
                String role = claims.get("role", String.class);

                SimpleGrantedAuthorityNoPrefix authority = new SimpleGrantedAuthorityNoPrefix(role);
                Authentication auth = new UsernamePasswordAuthenticationToken(
                        userId,
                        null,
                        Collections.singletonList(authority)
                );

                SecurityContextHolder.getContext().setAuthentication(auth);
            }
        } catch (Exception e) {
            SecurityContextHolder.clearContext();
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token inválido");
            return;
        }

        filterChain.doFilter(request, response);
    }
}