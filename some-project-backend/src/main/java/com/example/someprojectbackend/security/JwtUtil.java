package com.example.someprojectbackend.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

/**
 * Utility-klasse for å generere og validere JWT-tokens.
 *
 * Bruker {@link io.jsonwebtoken.Jwts} (jjwt-biblioteket) til signering
 * og parsing av tokens.
 */
@Component
public class JwtUtil {

    private final SecretKey key;
    private final long expiresMinutes;

    /**
     * Oppretter JwtUtil med en hemmelig nøkkel og utløpstid.
     *
     * @param secret hemmelig streng fra application.properties (app.jwt.secret)
     * @param expiresMinutes hvor lenge tokens er gyldige (i minutter)
     */
    public JwtUtil(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.expiresMinutes}") long expiresMinutes) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expiresMinutes = expiresMinutes;
    }

    /**
     * Trekker ut e-post (subject) fra en Authorization-header.
     *
     * @param authHeader f.eks. "Bearer eyJhbGciOiJIUzI1NiIsInR5..."
     * @return subject (e-post) fra JWT-tokenet
     * @throws IllegalArgumentException hvis headeren er ugyldig eller mangler
     */
    public String extractEmail(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Invalid Authorization header");
        }
        String token = authHeader.substring("Bearer ".length()).trim();
        return parseSubject(token);
    }

    /**
     * Utsteder et nytt JWT-token for en gitt bruker.
     *
     * @param subjectEmail e-postadresse (brukes som subject i token)
     * @return signert JWT-token
     */
    public String issue(String subjectEmail) {
        var now = Instant.now();
        return Jwts.builder()
                .subject(subjectEmail)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(expiresMinutes * 60)))
                .signWith(key)
                .compact();
    }

    /**
     * Parser subject (e-post) fra et eksisterende JWT-token.
     *
     * @param token JWT-token
     * @return subject (e-post)
     */
    public String parseSubject(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }
}
