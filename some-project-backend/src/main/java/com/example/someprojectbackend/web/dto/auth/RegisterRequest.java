package com.example.someprojectbackend.web.dto.auth;

/**
 * Request-body for å registrere en ny bruker.
 * <p>
 * Brukes i {@code POST /api/auth/register}.
 * <p>
 * Felter:
 * - email: brukerens e-postadresse (må være unik)
 * - password: passord i klartekst (hashes i backend før lagring)
 * - displayName: valgt visningsnavn (må være unikt)
 */
public record RegisterRequest(
        String email,
        String password,
        String displayName
) {
}
