package com.example.someprojectbackend.web.dto.popular;

import java.time.Instant;
import java.util.UUID;

/**
 * Cursor-DTO for paginering i populære innlegg-feed.
 * <p>
 * Brukes i {@code GET /api/popular}.
 * <p>
 * Felter:
 * - score: beregnet popularitetsscore (likes*2 + comments)
 * - createdAt: tidspunkt da innlegget ble opprettet
 * - id: unik ID (UUID) til innlegget (for å skille mellom poster med samme score og tidspunkt)
 * <p>
 * Kombinasjonen av disse feltene gir en stabil keyset pagination
 * der man kan bla videre uten problemer selv om nye poster opprettes.
 */
public record PopularCursorDto(
        long score,
        Instant createdAt,
        UUID id
) {
}
