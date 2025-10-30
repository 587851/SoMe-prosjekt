package com.example.someprojectbackend.web.dto.auth;

/**
 * DTO (Data Transfer Object) som representerer en bruker i autentiseringskontekst.
 * <p>
 * Brukes for å sende minimal og sikker brukerinfo til frontend,
 * uten sensitive felt som passord-hash.
 * <p>
 * Felter:
 * - id: unik bruker-ID (UUID som String)
 * - email: brukerens e-post
 * - displayName: visningsnavn valgt av brukeren
 * <p>
 * Returneres typisk fra:
 * - AuthController (/register, /login, /me)
 * - AuthService.toDto(User)
 */
public record AuthUserDto(
        String id,
        String email,
        String displayName
) {
}
