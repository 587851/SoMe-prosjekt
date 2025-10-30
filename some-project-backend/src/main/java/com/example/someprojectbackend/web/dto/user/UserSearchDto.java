package com.example.someprojectbackend.web.dto.user;

import com.example.someprojectbackend.domain.User;

import java.util.UUID;

/**
 * DTO som representerer et søkeresultat for en bruker.
 * <p>
 * Brukes i {@code GET /api/users/search}.
 * <p>
 * Felter:
 * - id: unik bruker-ID (UUID)
 * - displayName: visningsnavn (unikt)
 * - avatarUrl: url til profilbilde
 * <p>
 * Dette er en lettvektsrepresentasjon av en bruker
 * som egner seg for søkeresultat-lister, autocomplete osv.
 */
public record UserSearchDto(
        UUID id,
        String displayName,
        String avatarUrl
) {
    /**
     * Mapper en {@link User}-entitet til en {@link UserSearchDto}.
     *
     * @param u bruker-entitet
     * @return DTO med kun id, displayName og avatarKey
     */
    public static UserSearchDto from(User u) {
        return new UserSearchDto(
                u.getId(),
                u.getDisplayName(),
                u.getAvatarKey());
    }
}
