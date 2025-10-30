// src/main/java/com/example/someprojectbackend/web/PopularController.java
package com.example.someprojectbackend.web;

import com.example.someprojectbackend.domain.User;
import com.example.someprojectbackend.repo.UserRepository;
import com.example.someprojectbackend.service.PopularService;
import com.example.someprojectbackend.web.dto.popular.PopularPostsPageDto;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.Instant;
import java.util.UUID;

/**
 * REST-controller for å hente populære innlegg.
 * <p>
 * Bygger på {@link PopularService} for å hente innlegg
 * rangert etter "score" (likes*2 + comments).
 * Støtter tidsvindu (day/week) og keyset pagination.
 */
@RestController
@RequestMapping("/api")
public class PopularController {

    private final PopularService popular;
    private final UserRepository users;

    public PopularController(PopularService popular, UserRepository users) {
        this.popular = popular;
        this.users = users;
    }

    /**
     * Henter viewerId fra Principal (kan være null hvis ikke innlogget).
     */
    private UUID viewerId(Principal principal) {
        if (principal == null) return null;
        return users.findByEmail(principal.getName())
                .map(User::getId)
                .orElse(null);
    }

    /**
     * Henter en side med populære innlegg.
     * <p>
     * GET /api/popular
     * <p>
     * - range: tidsvindu ("day"/"24h" eller "week"/"7d"), default = "day"
     * - limit: maks antall innlegg (1–50, default 10)
     * - cursorScore, cursorCreatedAt, cursorId: brukes for keyset pagination
     *
     * @param range           tidsvindu (day eller week)
     * @param limit           maks antall resultater
     * @param cursorScore     score til siste post fra forrige side
     * @param cursorCreatedAt tidspunkt til siste post fra forrige side
     * @param cursorId        id til siste post fra forrige side
     * @param principal       innlogget bruker (kan være null)
     * @return en side med populære innlegg + cursor til neste side
     */
    @GetMapping(value = "/popular", produces = MediaType.APPLICATION_JSON_VALUE)
    public PopularPostsPageDto popular(
            @RequestParam(defaultValue = "day") String range,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(required = false) Long cursorScore,
            @RequestParam(required = false) String cursorCreatedAt,
            @RequestParam(required = false) UUID cursorId,
            Principal principal
    ) {
        Instant createdAt = (cursorCreatedAt != null ? Instant.parse(cursorCreatedAt) : null);
        return popular.listPopular(
                range,
                limit,
                cursorScore,
                createdAt,
                cursorId,
                viewerId(principal)
        );
    }
}
