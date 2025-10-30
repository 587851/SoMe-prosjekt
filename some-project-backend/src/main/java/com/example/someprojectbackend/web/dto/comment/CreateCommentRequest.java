package com.example.someprojectbackend.web.dto.comment;

/**
 * Request-body for å opprette en ny kommentar.
 * <p>
 * Brukes i {@code POST /api/posts/{postId}/comments}.
 * <p>
 * Felter:
 * - content: selve kommentarteksten (påkrevd)
 */
public record CreateCommentRequest(
        String content
) {
}
