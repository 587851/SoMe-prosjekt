package com.example.someprojectbackend.web;

import com.example.someprojectbackend.repo.UserRepository;
import com.example.someprojectbackend.security.JwtUtil;
import com.example.someprojectbackend.service.AuthService;
import com.example.someprojectbackend.web.dto.auth.AuthUserDto;
import com.example.someprojectbackend.web.dto.auth.LoginRequest;
import com.example.someprojectbackend.web.dto.auth.LoginResponse;
import com.example.someprojectbackend.web.dto.auth.RegisterRequest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

/**
 * REST-controller for autentisering og registrering.
 * <p>
 * Endepunkter:
 * - /register: opprett ny bruker
 * - /login: logg inn med e-post og passord
 * - /me: hent innlogget bruker basert på JWT
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserRepository users;
    private final PasswordEncoder encoder;
    private final JwtUtil jwt;
    private final AuthService svc;

    public AuthController(UserRepository users, PasswordEncoder encoder, JwtUtil jwt, AuthService svc) {
        this.users = users;
        this.encoder = encoder;
        this.jwt = jwt;
        this.svc = svc;
    }

    /**
     * Registrerer en ny bruker og utsteder JWT-token.
     * <p>
     * POST /api/auth/register
     *
     * @param req registreringsdata (email, displayName, password)
     * @return {@link LoginResponse} med token og brukerinfo
     */
    @PostMapping(value = "/register",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public LoginResponse register(@RequestBody RegisterRequest req) {
        var userDto = svc.register(req);
        var token = jwt.issue(userDto.email());
        return new LoginResponse(token, userDto);
    }

    /**
     * Logger inn en eksisterende bruker.
     * <p>
     * POST /api/auth/login
     * <p>
     * - Sjekker at brukeren finnes.
     * - Verifiserer passord med {@link PasswordEncoder}.
     * - Utsteder JWT hvis alt stemmer.
     *
     * @param req innloggingsdata (email, password)
     * @return {@link LoginResponse} med token og brukerinfo
     */
    @PostMapping(value = "/login",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public LoginResponse login(@RequestBody LoginRequest req) {
        var user = users.findByEmail(req.email().toLowerCase())
                .orElseThrow(() -> new IllegalArgumentException("Invalid credentials"));

        if (!encoder.matches(req.password(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid credentials");
        }

        var dto = svc.toDto(user);
        return new LoginResponse(jwt.issue(dto.email()), dto);
    }

    /**
     * Returnerer innlogget bruker basert på JWT i Authorization-header.
     * <p>
     * GET /api/auth/me
     * <p>
     * - Returnerer null hvis ingen token eller ugyldig token.
     * - Brukes av frontend for å hente innlogget bruker ved refresh.
     *
     * @param auth Authorization-header (Bearer <token>)
     * @return {@link AuthUserDto} eller null
     */
    @GetMapping(value = "/me", produces = MediaType.APPLICATION_JSON_VALUE)
    public AuthUserDto me(@RequestHeader(name = "Authorization", required = false) String auth) {
        if (auth == null || !auth.startsWith("Bearer ")) return null;

        var email = jwt.parseSubject(auth.substring(7));
        var user = users.findByEmail(email).orElse(null);

        return user == null ? null : svc.toDto(user);
    }
}
