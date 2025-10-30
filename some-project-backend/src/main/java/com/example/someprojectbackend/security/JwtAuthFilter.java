package com.example.someprojectbackend.security;

import com.example.someprojectbackend.repo.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Spring Security-filter for JWT-autentisering.
 *
 * Interseptor som kjøres én gang per request.
 * Henter Authorization-headeren, validerer JWT-tokenet og
 * legger inn en {@link UsernamePasswordAuthenticationToken}
 * i {@link SecurityContextHolder} hvis tokenet er gyldig.
 */
@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwt;
    private final UserRepository users;

    public JwtAuthFilter(JwtUtil jwt, UserRepository users) {
        this.jwt = jwt;
        this.users = users;
    }

    /**
     * Utfører filtreringen av requesten.
     *
     * - Leser Authorization-headeren
     * - Verifiserer JWT-token
     * - Henter brukeren fra databasen
     * - Oppretter en {@link UsernamePasswordAuthenticationToken}
     *   med rollen ROLE_USER
     *
     * @param req   HTTP-forespørsel
     * @param res   HTTP-respons
     * @param chain filterkjeden
     */
    @Override
    protected void doFilterInternal(HttpServletRequest req,
                                    HttpServletResponse res,
                                    FilterChain chain)
            throws ServletException, IOException {

        var auth = req.getHeader(HttpHeaders.AUTHORIZATION);

        if (auth != null && auth.startsWith("Bearer ")) {
            var token = auth.substring(7);
            try {
                var email = jwt.parseSubject(token);
                var user = users.findByEmail(email).orElse(null);
                if (user != null) {
                    var principal = user.getEmail();

                    var authorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));

                    var authentication =
                            new UsernamePasswordAuthenticationToken(principal, null, authorities);

                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            } catch (Exception ignored) {
            }
        }
        chain.doFilter(req, res);
    }
}
