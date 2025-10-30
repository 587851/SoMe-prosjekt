package com.example.someprojectbackend.web.dto.post;

/**
 * Request-body for å opprette et nytt innlegg (post).
 * <p>
 * Brukes i {@code POST /api/posts}.
 * <p>
 * Felter:
 * - author: (valgfritt) forfatter-ID eller navn – settes vanligvis automatisk
 * basert på innlogget bruker, så dette feltet kan ignoreres på backend.
 * - content: selve innleggets tekst (påkrevd)
 * - imageUrl: URL eller sti til et eventuelt bilde (kan være null)
 */
public record CreatePostRequest(
        String author,
        String content,
        String imageUrl
) {
}
