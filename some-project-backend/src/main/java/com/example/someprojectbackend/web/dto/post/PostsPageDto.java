package com.example.someprojectbackend.web.dto.post;

import com.example.someprojectbackend.web.dto.common.CursorDto;

import java.util.List;

/**
 * DTO som representerer en side med innlegg (posts).
 * <p>
 * Brukes i flere endepunkter, bl.a.:
 * - {@code GET /api/posts} (global feed)
 * - {@code GET /api/users/{displayName}/posts} (brukerens poster)
 * - {@code GET /api/home} (home-feed fra brukere man følger)
 * <p>
 * Felter:
 * - posts: liste med {@link PostDto}
 * - nextCursor: peker til neste side (eller {@code null} hvis ingen flere resultater)
 * <p>
 * Gir effektiv keyset pagination, der klienten sender med cursor
 * fra forrige side for å hente neste.
 */
public record PostsPageDto(
        List<PostDto> posts,
        CursorDto nextCursor
) {
}
