package com.example.someprojectbackend.web.dto.popular;

import com.example.someprojectbackend.web.dto.post.PostDto;

import java.util.List;

/**
 * DTO som representerer en side med populære innlegg.
 * <p>
 * Brukes i {@code GET /api/popular}.
 * <p>
 * Felter:
 * - posts: liste med {@link PostDto} (populære innlegg)
 * - nextCursor: peker til neste side (kan være {@code null} hvis ingen flere innlegg)
 * <p>
 * Dette muliggjør effektiv keyset pagination i "popular"-feed:
 * klienten sender med {@link PopularCursorDto}-feltene i neste request
 * for å hente flere resultater.
 */
public record PopularPostsPageDto(
        List<PostDto> posts,
        PopularCursorDto nextCursor
) {
}
