package ru.twentyoneh.embedderservice.utils;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import ru.twentyoneh.embedderservice.service.JwtService;

import java.io.IOException;
import java.util.List;

public class JwtAuthFilter extends OncePerRequestFilter {
    private final JwtService jwt;

    public JwtAuthFilter(JwtService jwt) { this.jwt = jwt; }

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws IOException, ServletException {
        String auth = req.getHeader("Authorization");
        if (auth != null && auth.startsWith("Bearer ")) {
            try {
                var jws = jwt.parse(auth.substring(7));
                String user = jws.getBody().getSubject();
                List<String> roles = jws.getBody().get("roles", List.class);
                var authToken = new UsernamePasswordAuthenticationToken(user, null,
                        roles.stream().map(SimpleGrantedAuthority::new).toList());
                SecurityContextHolder.getContext().setAuthentication(authToken);
            } catch (Exception e) {
                // токен битый — оставляем контекст пустым; доступ решит Security
            }
        }
        chain.doFilter(req, res);
    }

    // можно исключить пути, где фильтр не нужен:
    @Override
    protected boolean shouldNotFilter(HttpServletRequest req) {
        String uri = req.getRequestURI();
        return uri.startsWith("/auth/") || uri.equals("/actuator/health");
    }
}

