package com.example.someprojectbackend.domain;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

/**
 * Entitet som representerer en "like" på et innlegg.
 *
 * En {@link User} kan like et {@link Post} kun én gang,
 * noe som håndheves via en unik constraint på (post_id, user_id).
 */
@Entity
@Table(
        name = "post_likes",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_post_like",
                columnNames = {"post_id", "user_id"}
        ),
        indexes = {
                @Index(name = "idx_post_likes_post_id", columnList = "post_id")
        }
)
public class PostLike {

    /**
     * Primærnøkkel for PostLike.
     * Bruker autoinkrement (IDENTITY).
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Innlegget som er likt.
     * Mange likes kan tilhøre samme post.
     */
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false, columnDefinition = "uuid")
    private Post post;

    /**
     * Brukeren som har likt innlegget.
     * Mange likes kan tilhøre samme bruker.
     */
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, columnDefinition = "uuid")
    private User user;

    /**
     * Tidspunkt da "like" ble opprettet.
     * Settes automatisk ved persistering.
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    // --- Getters ---
    public Post getPost() { return post; }
    public User getUser() { return user; }

    // --- Setters ---
    public void setPost(Post post) { this.post = post; }
    public void setUser(User user) { this.user = user; }
}
