package com.example.someprojectbackend.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Entitet som representerer en bruker i systemet.
 *
 * En bruker har en unik e-post og visningsnavn,
 * samt valgfri avatar og kort bio.
 * Brukeren kan opprette flere {@link Post}-objekter.
 */
@Entity
@Table(
        name = "users",
        indexes = {
                @Index(name = "ux_users_email", columnList = "email", unique = true),
                @Index(name = "ux_users_display_name", columnList = "display_name", unique = true)
        }
)
public class User {

    /**
     * Primærnøkkel for brukeren.
     * Genereres automatisk som en UUID.
     */
    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(columnDefinition = "uuid")
    private UUID id;

    /**
     * Brukerens e-post (unik).
     */
    @Column(nullable = false, unique = true)
    private String email;

    /**
     * Hashet passord (ikke plaintext).
     */
    @Column(nullable = false)
    private String passwordHash;

    /**
     * Brukerens visningsnavn (unik).
     */
    @Column(nullable = false, unique = true)
    private String displayName;

    /**
     * Tidspunkt da brukeren ble opprettet.
     * Settes automatisk ved persistering.
     */
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    /**
     * Nøkkel til en avatar (f.eks. fil i S3 eller uploads/).
     * Kan være null hvis ingen avatar er satt.
     */
    @Column(nullable = true)
    private String avatarKey;

    /**
     * Kort biografi om brukeren (maks 280 tegn).
     */
    @Column(length = 280)
    private String bio;

    /**
     * Alle innlegg skrevet av brukeren.
     * Ignoreres ved JSON-serialisering for å unngå rekursjon.
     */
    @JsonIgnore
    @OneToMany(mappedBy = "author", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<Post> posts;

    // --- Getters ---
    public UUID getId() { return id; }
    public String getEmail() { return email; }
    public String getPasswordHash() { return passwordHash; }
    public String getDisplayName() { return displayName; }
    public String getAvatarKey() { return avatarKey; }
    public String getBio() { return bio; }

    // --- Setters ---
    public void setEmail(String email) { this.email = email; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }
    public void setAvatarKey(String avatarKey) { this.avatarKey = avatarKey; }
    public void setBio(String bio) { this.bio = bio; }
}
