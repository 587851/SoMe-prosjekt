package com.example.someprojectbackend.repo;

import com.example.someprojectbackend.domain.PostLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Repository for {@link PostLike}-entiteten.
 *
 * Gir tilgang til å hente, telle og slette likes på innlegg.
 * Brukes blant annet for å sjekke om en bruker har likt et innlegg,
 * samt for å hente ut hvilke innlegg en bruker har likt.
 */
public interface PostLikeRepository extends JpaRepository<PostLike, Long> {

    /**
     * Teller antall likes på et gitt innlegg.
     *
     * @param postId ID til innlegget
     * @return antall likes
     */
    long countByPost_Id(UUID postId);

    /**
     * Sjekker om en bestemt bruker har likt et innlegg.
     *
     * @param postId ID til innlegget
     * @param userId ID til brukeren
     * @return true hvis brukeren har likt innlegget, ellers false
     */
    boolean existsByPost_IdAndUser_Id(UUID postId, UUID userId);

    /**
     * Sletter en like fra en gitt bruker på et gitt innlegg.
     *
     * @param postId ID til innlegget
     * @param userId ID til brukeren
     */
    void deleteByPost_IdAndUser_Id(UUID postId, UUID userId);

    /**
     * Sletter alle likes knyttet til et bestemt innlegg i én bulk-operasjon.
     *
     * @param postId ID til innlegget
     * @return antall slettede rader
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query("delete from PostLike pl where pl.post.id = :postId")
    int bulkDeleteByPostId(@Param("postId") UUID postId);

    /**
     * Henter en liste med ID-er for innlegg en gitt bruker har likt,
     * begrenset til en gitt mengde post-ID-er.
     *
     * @param userId ID til brukeren
     * @param postIds liste over innlegg-ID-er å sjekke
     * @return liste med innlegg-ID-er som brukeren har likt
     */
    @Query("select pl.post.id from PostLike pl where pl.user.id = :userId and pl.post.id in :postIds")
    List<UUID> findLikedPostIds(UUID userId, List<UUID> postIds);
}
