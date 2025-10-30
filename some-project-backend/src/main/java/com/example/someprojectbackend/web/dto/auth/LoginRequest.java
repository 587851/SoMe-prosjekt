package com.example.someprojectbackend.web.dto.auth;

/**
 * Request-body for innlogging.
 * <p>
 * Brukes i {@code POST /api/auth/login}.
 * <p>
 * Felter:
 * - email: brukerens e-postadresse (case-insensitive)
 * - password: passord i klartekst (sjekkes mot hash i databasen)
 */
public record LoginRequest(
        String email,
        String password
) {
}
