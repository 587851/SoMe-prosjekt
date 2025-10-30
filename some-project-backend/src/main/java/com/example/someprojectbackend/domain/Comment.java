package com.example.someprojectbackend.domain;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.UUID;

/**
 * Entitet som representerer en kommentar på et innlegg.
 *
 * Kommentaren er koblet til både et {@link Post}-objekt og en {@link User}-forfatter.
 * Kommentarer lagres i databasen i tabellen <code>comments</code>.
 */
@Entity
@Table(
        name = "comments",
        indexes = {
                @Index(
                        name = "idx_comments_post_created_id",
                        columnList = "post_id,created_at,id"
                )
        }
)
public class Comment {

    /**
     * Primærnøkkel for kommentaren.
     * Genereres automatisk som en UUID.
     */
    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(columnDefinition = "uuid")
    private UUID id;

    /**
     * Relasjon til innlegget kommentaren hører til.
     * Mange kommentarer kan tilhøre ett innlegg.
     */
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false, columnDefinition = "uuid")
    private Post post;

    /**
     * Relasjon til brukeren som skrev kommentaren.
     * Mange kommentarer kan skrives av én bruker.
     */
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false, columnDefinition = "uuid")
    private User author;

    /**
     * Selve innholdet i kommentaren.
     */
    @Column(columnDefinition = "text", nullable = false)
    private String content;

    /**
     * Tidspunkt når kommentaren ble opprettet.
     * Settes automatisk av databasen/Hibernate ved innsetting.
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    // --- Getters ---
    public UUID getId() { return id; }
    public Post getPost() { return post; }
    public User getAuthor() { return author; }
    public String getContent() { return content; }
    public Instant getCreatedAt() { return createdAt; }

    // --- Setters ---
    public void setPost(Post post) { this.post = post; }
    public void setAuthor(User author) { this.author = author; }
    public void setContent(String content) { this.content = content; }
}
