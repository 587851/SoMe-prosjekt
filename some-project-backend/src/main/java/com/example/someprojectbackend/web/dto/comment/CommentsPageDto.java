package com.example.someprojectbackend.web.dto.comment;

import com.example.someprojectbackend.web.dto.common.CursorDto;

import java.util.List;

/**
 * DTO som representerer en side med kommentarer.
 * <p>
 * Brukes ved paginering av kommentarer på et innlegg,
 * typisk i {@code GET /api/posts/{postId}/comments}.
 * <p>
 * Felter:
 * - comments: liste med {@link CommentDto}
 * - nextCursor: peker til neste side (eller {@code null} hvis ingen flere)
 */
public record CommentsPageDto(
        List<CommentDto> comments,
        CursorDto nextCursor
) {
}
