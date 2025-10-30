package com.example.someprojectbackend.domain;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.UUID;

/**
 * Entitet som representerer et innlegg (post).
 *
 * Hvert innlegg har en forfatter ({@link User}), tekstinnhold
 * og kan eventuelt inkludere en bilde-URL.
 * Opprettelsestidspunkt settes automatisk.
 */
@Entity
@Table(
        name = "posts",
        indexes = {
                @Index(name = "idx_posts_created_at_id", columnList = "created_at,id"),
                @Index(name = "idx_posts_author_id", columnList = "author_id")
        }
)
public class Post {

    /**
     * Primærnøkkel for innlegget.
     * Genereres automatisk som en UUID.
     */
    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(columnDefinition = "uuid")
    private UUID id;

    /**
     * Forfatteren som opprettet innlegget.
     * Mange innlegg kan være skrevet av samme bruker.
     */
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false, columnDefinition = "uuid")
    private User author;

    /**
     * Tekstinnholdet i innlegget.
     */
    @Column(columnDefinition = "text", nullable = false)
    private String content;

    /**
     * URL til et bilde tilknyttet innlegget (kan være null).
     */
    private String imageUrl;

    /**
     * Tidspunkt når innlegget ble opprettet.
     * Settes automatisk ved persistering.
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    // --- Getters ---
    public UUID getId() { return id; }
    public User getAuthor() { return author; }
    public String getContent() { return content; }
    public String getImageUrl() { return imageUrl; }
    public Instant getCreatedAt() { return createdAt; }

    // --- Setters ---
    public void setAuthor(User author) { this.author = author; }
    public void setContent(String content) { this.content = content; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
}
