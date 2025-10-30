package com.example.someprojectbackend.domain;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

/**
 * Entitet som representerer en "følge"-relasjon mellom brukere.
 *
 * En {@link User} (follower) kan følge en annen {@link User} (followee).
 * Hver kombinasjon av (follower, followee) er unik.
 */
@Entity
@Table(
        name = "user_follows",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_user_follow",
                columnNames = {"follower_id", "followee_id"}
        ),
        indexes = {
                @Index(name = "idx_follows_follower", columnList = "follower_id"),
                @Index(name = "idx_follows_followee", columnList = "followee_id")
        }
)
public class UserFollow {

    /**
     * Primærnøkkel for UserFollow.
     * Bruker autoinkrement (IDENTITY).
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Brukeren som følger en annen.
     */
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "follower_id", nullable = false, columnDefinition = "uuid")
    private User follower;

    /**
     * Brukeren som blir fulgt.
     */
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "followee_id", nullable = false, columnDefinition = "uuid")
    private User followee;

    /**
     * Tidspunkt da følge-relasjonen ble opprettet.
     * Settes automatisk ved persistering.
     */
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    // --- Getters ---
    public Long getId() { return id; }
    public User getFollower() { return follower; }
    public User getFollowee() { return followee; }
    public Instant getCreatedAt() { return createdAt; }

    // --- Setters ---
    public void setFollower(User follower) { this.follower = follower; }
    public void setFollowee(User followee) { this.followee = followee; }
}
