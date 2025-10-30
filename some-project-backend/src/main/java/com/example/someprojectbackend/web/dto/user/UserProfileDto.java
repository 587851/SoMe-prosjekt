package com.example.someprojectbackend.web.dto.user;

import com.example.someprojectbackend.domain.User;

import java.util.UUID;

/**
 * DTO som representerer en brukerprofil.
 * <p>
 * Brukes i API-responser for å vise profilinformasjon
 * uten å eksponere sensitive felter som passord-hash.
 * <p>
 * Felter:
 * - id: unik bruker-ID (UUID)
 * - displayName: brukerens visningsnavn (unikt)
 * - email: e-postadresse (kan være vist kun til eieren av profilen)
 * - avatarUrl: sti/URL til brukerens avatar (kan være null)
 * - bio: kort biografi (maks 280 tegn, kan være null)
 */
public record UserProfileDto(
        UUID id,
        String displayName,
        String email,
        String avatarUrl,
        String bio
) {
    /**
     * Mapper en {@link User}-entitet til en {@link UserProfileDto}.
     *
     * @param u bruker-entitet
     * @return DTO med profilinformasjon
     */
    public static UserProfileDto from(User u) {
        return new UserProfileDto(
                u.getId(),
                u.getDisplayName(),
                u.getEmail(),
                u.getAvatarKey(),
                u.getBio()
        );
    }
}
