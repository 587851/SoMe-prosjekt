package com.example.someprojectbackend.service;

import com.example.someprojectbackend.domain.User;
import com.example.someprojectbackend.repo.UserRepository;
import com.example.someprojectbackend.web.dto.auth.AuthUserDto;
import com.example.someprojectbackend.web.dto.auth.RegisterRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Service-klasse for autentisering og registrering av brukere.
 * <p>
 * Håndterer logikken for:
 * - opprettelse av nye brukere
 * - hashing av passord
 * - mapping til {@link AuthUserDto}
 */
@Service
public class AuthService {
    private final UserRepository users;
    private final PasswordEncoder encoder;

    public AuthService(UserRepository users, PasswordEncoder encoder) {
        this.users = users;
        this.encoder = encoder;
    }

    /**
     * Registrerer en ny bruker i systemet.
     * <p>
     * - Sjekker om e-post allerede finnes
     * - Oppretter en ny {@link User}
     * - Hasher passordet med {@link PasswordEncoder}
     * - Lagrer brukeren i databasen
     * - Returnerer en DTO med basisinfo
     *
     * @param req registreringsforespørsel (email, displayName, password)
     * @return {@link AuthUserDto} med id, email og displayName
     * @throws IllegalStateException hvis e-post allerede er registrert
     */
    public AuthUserDto register(RegisterRequest req) {
        if (users.existsByEmail(req.email())) {
            throw new IllegalStateException("Email already registered");
        }

        var u = new User();
        u.setEmail(req.email().toLowerCase());
        u.setDisplayName(req.displayName());
        u.setPasswordHash(encoder.encode(req.password()));

        var saved = users.save(u);
        return new AuthUserDto(saved.getId().toString(), saved.getEmail(), saved.getDisplayName());
    }

    /**
     * Mapper en {@link User} til en {@link AuthUserDto}.
     *
     * @param u bruker
     * @return DTO med id, email og displayName
     */
    public AuthUserDto toDto(User u) {
        return new AuthUserDto(u.getId().toString(), u.getEmail(), u.getDisplayName());
    }
}
