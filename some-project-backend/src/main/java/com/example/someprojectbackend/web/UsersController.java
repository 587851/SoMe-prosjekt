package com.example.someprojectbackend.web;

import com.example.someprojectbackend.repo.UserRepository;
import com.example.someprojectbackend.web.dto.user.UserProfileDto;
import com.example.someprojectbackend.web.dto.user.UserSearchDto;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

/**
 * REST-controller for bruker-relaterte operasjoner.
 * <p>
 * Endepunkter:
 * - søk etter brukere (/search)
 * - hente brukerprofil etter displayName
 * - oppdatere egen profil (/me)
 */
@RestController
@RequestMapping("/api/users")
public class UsersController {

    private final UserRepository users;

    public UsersController(UserRepository users) {
        this.users = users;
    }

    /**
     * Søk etter brukere basert på displayName.
     * <p>
     * GET /api/users/search?q=te&limit=8
     * <p>
     * - Minimum 2 tegn i søkestrengen (ellers returneres tom liste for å unngå full table scan).
     * - Maks limit = 20.
     *
     * @param q     søkestreng (min 2 tegn)
     * @param limit maks antall resultater (default 8, max 20)
     * @return liste med brukere som matcher
     */
    @GetMapping(value = "/search", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<UserSearchDto> search(
            @RequestParam(name = "q", required = false) String q,
            @RequestParam(name = "limit", defaultValue = "8") int limit
    ) {
        if (q == null || q.trim().length() < 2) {
            return List.of();
        }
        int safeLimit = Math.max(1, Math.min(limit, 20));
        var page = PageRequest.of(0, safeLimit);

        return users.searchByDisplayName(q.trim(), page).stream()
                .map(UserSearchDto::from)
                .toList();
    }

    /**
     * Henter en offentlig brukerprofil basert på displayName.
     * <p>
     * GET /api/users/{displayName}
     *
     * @param displayName visningsnavn til brukeren
     * @return brukerprofil
     * @throws ResponseStatusException hvis brukeren ikke finnes (404)
     */
    @GetMapping(value = "/{displayName:^(?!me$|search$).+}", produces = MediaType.APPLICATION_JSON_VALUE)
    public UserProfileDto getByDisplayName(@PathVariable String displayName) {
        final var dn = displayName == null ? "" : displayName.trim();
        var user = users.findByDisplayNameCaseInsensitive(dn)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "User not found with name " + displayName));
        return UserProfileDto.from(user);
    }

    /**
     * Oppdaterer den innloggede brukerens bio.
     * <p>
     * PUT /api/users/me
     * <p>
     * - Krever autentisering (principal != null).
     * - Bio trunkeres til maks 280 tegn.
     *
     * @param body      request-body med feltet "bio"
     * @param principal innlogget bruker (fra Spring Security)
     * @return oppdatert brukerprofil
     */
    @PutMapping(value = "/me", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public UserProfileDto updateMe(@RequestBody Map<String, String> body,
                                   java.security.Principal principal) {
        if (principal == null) throw new RuntimeException("Unauthorized");

        var me = users.findByEmail(principal.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        var bio = body.getOrDefault("bio", "");
        bio = (bio == null) ? "" : bio.trim();
        if (bio.length() > 280) bio = bio.substring(0, 280);

        me.setBio(bio);
        users.save(me);
        return UserProfileDto.from(me);
    }
}
