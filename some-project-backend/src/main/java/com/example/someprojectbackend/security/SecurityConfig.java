package com.example.someprojectbackend.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.http.HttpMethod;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.http.HttpStatus;

/**
 * Konfigurasjon for Spring Security.
 * <p>
 * Setter opp:
 * - stateless JWT-basert autentisering
 * - CORS og CSRF-policy
 * - autorisasjonsregler for ulike API-endpoints
 * - globalt PasswordEncoder
 */
@Configuration
public class SecurityConfig {

    /**
     * Definerer password encoder.
     * Brukes til å hashe og verifisere passord med BCrypt.
     *
     * @return PasswordEncoder
     */
    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Definerer sikkerhetsfilterkjeden for applikasjonen.
     * <p>
     * - Slår av CSRF (stateless API)
     * - Aktiverer CORS
     * - Setter session management til STATELESS
     * - Konfigurerer tilgangsregler for ulike endpoints
     * - Registrerer {@link JwtAuthFilter} før standard auth-filter
     * - Returnerer 401 Unauthorized istedenfor 500 hvis ikke autentisert
     *
     * @param http HttpSecurity builder
     * @param jwt  vårt tilpassede JWT-filter
     * @return SecurityFilterChain
     * @throws Exception hvis oppsettet feiler
     */
    @Bean
    SecurityFilterChain filterChain(HttpSecurity http, JwtAuthFilter jwt) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults())
                .sessionManagement(sm -> sm.sessionCreationPolicy(
                        org.springframework.security.config.http.SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/posts/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/stream/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/posts/**").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/api/posts/**").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/api/posts/**").authenticated()
                        .requestMatchers(HttpMethod.PATCH, "/api/posts/**").authenticated()
                        .anyRequest().permitAll()
                )
                .exceptionHandling(e -> e.authenticationEntryPoint(
                        new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
                .addFilterBefore(jwt, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
