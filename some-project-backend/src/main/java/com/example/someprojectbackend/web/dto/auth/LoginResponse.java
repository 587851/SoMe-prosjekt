package com.example.someprojectbackend.web.dto.auth;

/**
 * Respons-body for vellykket innlogging eller registrering.
 * <p>
 * Returneres fra:
 * - {@code POST /api/auth/login}
 * - {@code POST /api/auth/register}
 * <p>
 * Felter:
 * - token: JWT-token som klienten kan bruke i Authorization-header
 * ("Bearer &lt;token&gt;") for videre forespørsler.
 * - user: grunnleggende brukerinfo ({@link AuthUserDto})
 */
public record LoginResponse(
        String token,
        AuthUserDto user
) {
}
