package com.example.someprojectbackend.web;

import com.example.someprojectbackend.domain.User;
import com.example.someprojectbackend.repo.UserRepository;
import com.example.someprojectbackend.service.PostService;
import com.example.someprojectbackend.sse.PostSseHub;
import com.example.someprojectbackend.web.dto.comment.CommentDto;
import com.example.someprojectbackend.web.dto.comment.CommentsPageDto;
import com.example.someprojectbackend.web.dto.comment.CreateCommentRequest;
import com.example.someprojectbackend.web.dto.common.CursorDto;
import com.example.someprojectbackend.web.dto.post.CreatePostRequest;
import com.example.someprojectbackend.web.dto.post.PostDto;
import com.example.someprojectbackend.web.dto.post.PostsPageDto;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.security.Principal;
import java.time.Instant;
import java.util.UUID;

/**
 * REST-controller for innlegg (posts).
 * <p>
 * Endepunkter:
 * - CRUD på innlegg
 * - likes/unlikes
 * - kommentarer
 * - SSE-stream for sanntidsoppdateringer
 */
@RestController
@RequestMapping("/api")
public class PostController {

    private final PostService service;
    private final PostSseHub hub;
    private final UserRepository userRepository;

    public PostController(PostService service, PostSseHub hub, UserRepository userRepository) {
        this.service = service;
        this.hub = hub;
        this.userRepository = userRepository;
    }

    /**
     * Henter innlogget bruker fra {@link Principal}, eller null hvis ikke logget inn.
     */
    private User current(Principal principal) {
        if (principal == null) return null;
        return userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    /**
     * Henter global feed av innlegg med keyset pagination.
     * <p>
     * GET /api/posts
     */
    @GetMapping(value = "/posts", produces = MediaType.APPLICATION_JSON_VALUE)
    public PostsPageDto list(@RequestParam(defaultValue = "10") int limit,
                             @RequestParam(required = false) String cursorCreatedAt,
                             @RequestParam(required = false) UUID cursorId,
                             Principal principal) {
        CursorDto cursor = (cursorCreatedAt != null && cursorId != null)
                ? new CursorDto(Instant.parse(cursorCreatedAt), cursorId)
                : null;
        int safeLimit = Math.max(1, Math.min(limit, 50));
        return service.list(safeLimit, cursor, current(principal));
    }

    /**
     * Oppretter et nytt innlegg for innlogget bruker.
     * <p>
     * POST /api/posts
     */
    @PostMapping(value = "/posts", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public PostDto create(@RequestBody CreatePostRequest req, Principal principal) {
        if (principal == null) throw new RuntimeException("Authentication required");
        var user = current(principal);
        var dto = service.create(req, user);
        hub.broadcastPost(dto);
        return dto;
    }

    /**
     * Henter innlegg skrevet av en bestemt bruker.
     * <p>
     * GET /api/users/{displayName}/posts
     */
    @GetMapping(value = "/users/{displayName}/posts", produces = MediaType.APPLICATION_JSON_VALUE)
    public PostsPageDto listByUser(@PathVariable String displayName,
                                   @RequestParam(defaultValue = "10") int limit,
                                   @RequestParam(required = false) String cursorCreatedAt,
                                   @RequestParam(required = false) UUID cursorId,
                                   Principal principal) {
        CursorDto cursor = (cursorCreatedAt != null && cursorId != null)
                ? new CursorDto(Instant.parse(cursorCreatedAt), cursorId)
                : null;
        int safeLimit = Math.max(1, Math.min(limit, 50));
        return service.listByAuthor(displayName, safeLimit, cursor, current(principal));
    }

    // --- Likes ---

    /**
     * Liker et innlegg.
     * <p>
     * POST /api/posts/{postId}/likes
     */
    @PostMapping(value = "/posts/{postId}/likes", produces = MediaType.APPLICATION_JSON_VALUE)
    public PostDto like(@PathVariable UUID postId, Principal principal) {
        var user = current(principal);
        var dto = service.like(postId, user);
        try {
            hub.broadcastPost(dto);
        } catch (Exception ignored) {
        }
        return dto;
    }

    /**
     * Unliker et innlegg.
     * <p>
     * DELETE /api/posts/{postId}/likes
     */
    @DeleteMapping(value = "/posts/{postId}/likes", produces = MediaType.APPLICATION_JSON_VALUE)
    public PostDto unlike(@PathVariable UUID postId, Principal principal) {
        var user = current(principal);
        var dto = service.unlike(postId, user);
        try {
            hub.broadcastPost(dto);
        } catch (Exception ignored) {
        }
        return dto;
    }

    // --- Comments ---

    /**
     * Legger til en kommentar på et innlegg.
     * <p>
     * POST /api/posts/{postId}/comments
     */
    @PostMapping(value = "/posts/{postId}/comments",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public CommentDto addComment(@PathVariable UUID postId,
                                 @RequestBody CreateCommentRequest req,
                                 Principal principal) {
        if (principal == null) throw new RuntimeException("Authentication required");
        var user = current(principal);

        var comment = service.addComment(postId, req.content(), user);

        var freshPost = service.snapshot(postId, user);
        hub.broadcastPost(freshPost);

        return comment;
    }

    /**
     * Henter kommentarer på et innlegg med keyset pagination.
     * <p>
     * GET /api/posts/{postId}/comments
     */
    @GetMapping(value = "/posts/{postId}/comments", produces = MediaType.APPLICATION_JSON_VALUE)
    public CommentsPageDto listComments(@PathVariable UUID postId,
                                        @RequestParam(defaultValue = "10") int limit,
                                        @RequestParam(required = false) String cursorCreatedAt,
                                        @RequestParam(required = false) UUID cursorId) {
        CursorDto cursor = (cursorCreatedAt != null && cursorId != null)
                ? new CursorDto(Instant.parse(cursorCreatedAt), cursorId)
                : null;
        int safeLimit = Math.max(1, Math.min(limit, 50));
        return service.listComments(postId, safeLimit, cursor);
    }

    // --- SSE ---

    /**
     * Åpner en SSE-strøm for innleggshendelser (nye, likes, kommentarer, slettede).
     * <p>
     * GET /api/stream/posts
     */
    @GetMapping(path = "/stream/posts", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream() {
        return hub.subscribe();
    }

    // --- Delete post ---

    /**
     * Sletter et innlegg (krever at requester er eier eller admin).
     * <p>
     * DELETE /api/posts/{postId}
     */
    @DeleteMapping(value = "/posts/{postId}")
    public void delete(@PathVariable UUID postId, Principal principal) {
        if (principal == null) throw new RuntimeException("Authentication required");
        var user = current(principal);
        service.deletePost(postId, user);
        try {
            hub.broadcastPostDeleted(postId);
        } catch (Exception ignored) {
        }
    }
}
