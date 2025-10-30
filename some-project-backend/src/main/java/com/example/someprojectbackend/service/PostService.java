package com.example.someprojectbackend.service;

import com.example.someprojectbackend.domain.Comment;
import com.example.someprojectbackend.domain.Post;
import com.example.someprojectbackend.domain.PostLike;
import com.example.someprojectbackend.domain.User;
import com.example.someprojectbackend.repo.CommentRepository;
import com.example.someprojectbackend.repo.PostLikeRepository;
import com.example.someprojectbackend.repo.PostRepository;
import com.example.someprojectbackend.repo.UserFollowRepository;
import com.example.someprojectbackend.web.dto.comment.CommentDto;
import com.example.someprojectbackend.web.dto.comment.CommentsPageDto;
import com.example.someprojectbackend.web.dto.common.CursorDto;
import com.example.someprojectbackend.web.dto.post.CreatePostRequest;
import com.example.someprojectbackend.web.dto.post.PostDto;
import com.example.someprojectbackend.web.dto.post.PostsPageDto;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.UUID;

/**
 * Service-klasse for å håndtere innlegg (posts).
 *
 * Funksjonalitet:
 *  - oppretting og sletting av innlegg
 *  - liker/unliker
 *  - legge til og hente kommentarer
 *  - hente feeds (global, forfatter, following)
 *  - snapshot av enkelt-innlegg
 */
@Service
public class PostService {
    private final PostRepository postRepo;
    private final PostLikeRepository likeRepo;
    private final CommentRepository commentRepo;
    private final UserFollowRepository followRepo;

    public PostService(PostRepository postRepo,
                       PostLikeRepository likeRepo,
                       CommentRepository commentRepo,
                       UserFollowRepository followRepo) {
        this.postRepo = postRepo;
        this.likeRepo = likeRepo;
        this.commentRepo = commentRepo;
        this.followRepo = followRepo;
    }

    /**
     * Mapper et {@link Post} til et {@link PostDto} med antall likes, kommentarer
     * og flagg for om viewer har likt det.
     */
    private PostDto toDtoWithCounts(Post p, User viewer) {
        long likes = likeRepo.countByPost_Id(p.getId());
        long comments = commentRepo.countByPost_Id(p.getId());
        boolean likedByMe = (viewer != null) && likeRepo.existsByPost_IdAndUser_Id(p.getId(), viewer.getId());
        return PostDto.from(p, likes, comments, likedByMe);
    }

    /**
     * Henter global feed (alle innlegg), med keyset pagination.
     *
     * @param limit maks antall innlegg
     * @param cursor startpunkt (createdAt + id) eller null for første side
     * @param viewer innlogget bruker (kan være null)
     */
    @Transactional(readOnly = true)
    public PostsPageDto list(int limit, CursorDto cursor, User viewer) {
        var pageReq = PageRequest.of(0, limit + 1);
        List<Post> rows = (cursor == null)
                ? postRepo.findFirstPage(pageReq)
                : postRepo.findPageAfter(cursor.createdAt(), cursor.id(), pageReq);

        CursorDto next = null;
        if (rows.size() > limit) {
            var nextPost = rows.remove(rows.size() - 1);
            next = new CursorDto(nextPost.getCreatedAt(), nextPost.getId());
        }
        return new PostsPageDto(rows.stream().map(p -> toDtoWithCounts(p, viewer)).toList(), next);
    }

    /**
     * Oppretter et nytt innlegg.
     */
    @Transactional
    public PostDto create(CreatePostRequest req, User author) {
        var p = new Post();
        p.setAuthor(author);
        p.setContent(req.content());
        p.setImageUrl(req.imageUrl());
        p = postRepo.save(p);
        return toDtoWithCounts(p, author);
    }

    /**
     * Henter innlegg skrevet av en bestemt forfatter.
     */
    @Transactional(readOnly = true)
    public PostsPageDto listByAuthor(String displayName, int limit, CursorDto cursor, User viewer) {
        var pageReq = PageRequest.of(0, limit + 1);
        List<Post> rows = (cursor == null)
                ? postRepo.findFirstPageByAuthor(displayName, pageReq)
                : postRepo.findPageAfterByAuthor(displayName, cursor.createdAt(), cursor.id(), pageReq);

        CursorDto next = null;
        if (rows.size() > limit) {
            var nextPost = rows.remove(rows.size() - 1);
            next = new CursorDto(nextPost.getCreatedAt(), nextPost.getId());
        }
        return new PostsPageDto(rows.stream().map(p -> toDtoWithCounts(p, viewer)).toList(), next);
    }

    /**
     * Liker et innlegg (oppretter {@link PostLike} hvis det ikke allerede finnes).
     */
    @Transactional
    public PostDto like(UUID postId, User user) {
        var post = postRepo.findById(postId).orElseThrow();
        if (!likeRepo.existsByPost_IdAndUser_Id(postId, user.getId())) {
            var like = new PostLike();
            like.setPost(post);
            like.setUser(user);
            likeRepo.save(like);
        }
        return toDtoWithCounts(post, user);
    }

    /**
     * Unliker et innlegg (sletter {@link PostLike}).
     */
    @Transactional
    public PostDto unlike(UUID postId, User user) {
        var post = postRepo.findById(postId).orElseThrow();
        likeRepo.deleteByPost_IdAndUser_Id(postId, user.getId());
        return toDtoWithCounts(post, user);
    }

    /**
     * Legger til en kommentar på et innlegg.
     */
    @Transactional
    public CommentDto addComment(UUID postId, String content, User author) {
        var post = postRepo.findById(postId).orElseThrow();

        var c = new Comment();
        c.setPost(post);
        c.setAuthor(author);
        c.setContent(content);

        c = commentRepo.saveAndFlush(c);
        return CommentDto.from(c);
    }

    /**
     * Henter kommentarer til et innlegg med keyset pagination.
     */
    @Transactional(readOnly = true)
    public CommentsPageDto listComments(UUID postId, int limit, CursorDto cursor) {
        var pageReq = PageRequest.of(0, limit + 1);
        List<Comment> rows = (cursor == null)
                ? commentRepo.findByPost_IdOrderByCreatedAtDescIdDesc(postId, pageReq)
                : commentRepo.findByPost_IdAndCreatedAtLessThanEqualAndIdLessThanOrderByCreatedAtDescIdDesc(
                postId, cursor.createdAt(), cursor.id(), pageReq);

        CursorDto next = null;
        if (rows.size() > limit) {
            var nextC = rows.remove(rows.size() - 1);
            next = new CursorDto(nextC.getCreatedAt(), nextC.getId());
        }
        return new CommentsPageDto(rows.stream().map(CommentDto::from).toList(), next);
    }

    /**
     * Henter snapshot av et enkelt innlegg (inkl. counts og viewer-info).
     */
    @Transactional(readOnly = true)
    public PostDto snapshot(UUID postId, User viewer) {
        var post = postRepo.findById(postId).orElseThrow();
        return toDtoWithCounts(post, viewer);
    }

    /**
     * Sletter et innlegg dersom requester er eier (eller admin).
     * Sletter også likes og kommentarer knyttet til innlegget.
     */
    @Transactional
    public void deletePost(UUID postId, User requester) {
        var post = postRepo.findById(postId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found"));

        boolean isOwner = requester != null && post.getAuthor().getId().equals(requester.getId());
        boolean isAdmin = false;
        if (!isOwner && !isAdmin) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not allowed to delete this post");
        }

        likeRepo.bulkDeleteByPostId(postId);
        commentRepo.bulkDeleteByPostId(postId);

        postRepo.delete(post);
    }

    /**
     * Henter feed for innlogget bruker (basert på følger-relasjoner).
     */
    @Transactional(readOnly = true)
    public PostsPageDto listHome(User viewer, int limit, CursorDto cursor) {
        if (viewer == null) {
            return new PostsPageDto(List.of(), null);
        }

        var followeeIds = followRepo.findFolloweeIdsByFollowerId(viewer.getId());
        if (followeeIds.isEmpty()) {
            return new PostsPageDto(List.of(), null);
        }

        var pageReq = PageRequest.of(0, Math.min(Math.max(limit, 1), 50) + 1);
        List<Post> rows = (cursor == null)
                ? postRepo.findFirstPageByAuthorIds(followeeIds, pageReq)
                : postRepo.findPageAfterByAuthorIds(followeeIds, cursor.createdAt(), cursor.id(), pageReq);

        var next = (rows.size() > limit)
                ? new CursorDto(rows.get(limit).getCreatedAt(), rows.get(limit).getId())
                : null;

        if (next != null) rows = rows.subList(0, limit);
        return new PostsPageDto(rows.stream().map(p -> toDtoWithCounts(p, viewer)).toList(), next);
    }
}
