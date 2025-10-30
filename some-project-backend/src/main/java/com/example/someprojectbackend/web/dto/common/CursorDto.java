package com.example.someprojectbackend.web.dto.common;

import java.time.Instant;
import java.util.UUID;

/**
 * DTO som representerer en "cursor" for keyset pagination.
 *
 * Brukes i paginerte API-er (f.eks. {@code /api/posts}, {@code /api/comments}, {@code /api/popular}).
 *
 * I stedet for klassisk offset/limit, peker denne cursoren direkte
 * på siste element i en side, slik at neste side kan hentes mer effektivt
 * og uten problemer ved samtidig innsetting/sletting.
 *
 * Felter:
 *  - createdAt: tidspunkt da posten/kommentaren ble opprettet
 *  - id: unik ID (UUID) for å skille mellom objekter med samme createdAt
 */
public record CursorDto(
        Instant createdAt,
        UUID id
) { }
