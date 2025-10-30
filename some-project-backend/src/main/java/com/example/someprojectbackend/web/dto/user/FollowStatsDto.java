package com.example.someprojectbackend.web.dto.user;

/**
 * DTO som representerer følge-statistikk for en bruker.
 * <p>
 * Brukes i {@code GET /api/users/{displayName}/follow-stats}.
 * <p>
 * Felter:
 * - displayName: brukerens visningsnavn
 * - avatarUrl: URL/sti til brukerens avatar
 * - followers: antall følgere
 * - following: antall brukere denne følger
 * - followingByMe: true hvis den innloggede brukeren følger denne
 */
public record FollowStatsDto(
        String displayName,
        String avatarUrl,
        long followers,
        long following,
        boolean followingByMe
) {
}
