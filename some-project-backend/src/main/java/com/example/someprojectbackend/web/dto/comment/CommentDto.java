package com.example.someprojectbackend.web.dto.comment;

import com.example.someprojectbackend.domain.Comment;

import java.time.Instant;
import java.util.UUID;

/**
 * DTO (Data Transfer Object) for kommentarer.
 * <p>
 * Brukes til å returnere kommentarer til frontend uten
 * å eksponere hele {@link Comment}-entiteten.
 * <p>
 * Felter:
 * - id: unik kommentar-ID (UUID)
 * - author: visningsnavn til forfatteren
 * - authorAvatarUrl: URL/sti til forfatterens avatar
 * - content: selve kommentarteksten
 * - createdAt: tidspunkt da kommentaren ble opprettet
 */
public record CommentDto(
        UUID id,
        String author,
        String authorAvatarUrl,
        String content,
        Instant createdAt
) {
    /**
     * Mapper en {@link Comment}-entitet til en {@link CommentDto}.
     *
     * @param c kommentar-entitet
     * @return en DTO med forfatterinfo, innhold og metadata
     */
    public static CommentDto from(Comment c) {
        var u = c.getAuthor();
        return new CommentDto(
                c.getId(),
                u.getDisplayName(),
                u.getAvatarKey(),
                c.getContent(),
                c.getCreatedAt()
        );
    }
}
