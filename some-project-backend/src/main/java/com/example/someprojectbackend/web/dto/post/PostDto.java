package com.example.someprojectbackend.web.dto.post;

import com.example.someprojectbackend.domain.Post;

import java.time.Instant;
import java.util.UUID;

/**
 * DTO (Data Transfer Object) som representerer et innlegg (post).
 * <p>
 * Brukes i API-responser slik at frontend får alle nødvendige felt
 * uten å eksponere hele {@link Post}-entiteten.
 * <p>
 * Felter:
 * - id: unik post-ID (UUID)
 * - author: visningsnavn til forfatteren
 * - authorAvatarUrl: avatar-bilde til forfatteren (kan være null)
 * - content: tekstinnholdet i innlegget
 * - imageUrl: evt. bilde knyttet til innlegget (kan være null)
 * - createdAt: tidspunkt da posten ble opprettet
 * - likeCount: antall likes
 * - commentCount: antall kommentarer
 * - likedByMe: true hvis innlogget bruker har likt innlegget
 */
public record PostDto(
        UUID id,
        String author,
        String authorAvatarUrl,
        String content,
        String imageUrl,
        Instant createdAt,
        long likeCount,
        long commentCount,
        boolean likedByMe
) {
    /**
     * Mapper en {@link Post}-entitet til en {@link PostDto}.
     *
     * @param p            selve innlegget
     * @param likeCount    antall likes
     * @param commentCount antall kommentarer
     * @param likedByMe    true hvis innlogget bruker har likt posten
     * @return DTO med ferdig aggregert data
     */
    public static PostDto from(Post p,
                               long likeCount,
                               long commentCount,
                               boolean likedByMe) {
        var u = p.getAuthor();
        return new PostDto(
                p.getId(),
                u.getDisplayName(),
                u.getAvatarKey(),
                p.getContent(),
                p.getImageUrl(),
                p.getCreatedAt(),
                likeCount,
                commentCount,
                likedByMe
        );
    }
}
