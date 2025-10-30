package com.example.someprojectbackend.repo;

import com.example.someprojectbackend.domain.Comment;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Repository for {@link Comment}-entiteten.
 *
 * Gir tilgang til CRUD-operasjoner og spesialspørringer
 * for henting og sletting av kommentarer.
 */
public interface CommentRepository extends JpaRepository<Comment, UUID> {

    /**
     * Teller antall kommentarer knyttet til et gitt innlegg.
     *
     * @param postId ID til innlegget
     * @return antall kommentarer
     */
    long countByPost_Id(UUID postId);

    /**
     * Henter kommentarer til et innlegg med keyset pagination
     * (brukes for "infinite scroll" eller "load more").
     *
     * @param postId ID til innlegget
     * @param createdAt øvre grense for opprettelsestidspunkt
     * @param id øvre grense for kommentar-ID
     * @param pageable pagineringsinformasjon (begrensning/størrelse)
     * @return liste med kommentarer sortert etter opprettelsestid og ID, nyeste først
     */
    List<Comment> findByPost_IdAndCreatedAtLessThanEqualAndIdLessThanOrderByCreatedAtDescIdDesc(
            UUID postId, Instant createdAt, UUID id, Pageable pageable);

    /**
     * Henter første side med kommentarer til et innlegg,
     * sortert etter opprettelsestid og ID, nyeste først.
     *
     * @param postId ID til innlegget
     * @param pageable pagineringsinformasjon
     * @return liste med kommentarer
     */
    List<Comment> findByPost_IdOrderByCreatedAtDescIdDesc(UUID postId, Pageable pageable);

    /**
     * Sletter alle kommentarer til et gitt innlegg i én bulk-operasjon.
     *
     * @param postId ID til innlegget
     * @return antall slettede rader
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query("delete from Comment c where c.post.id = :postId")
    int bulkDeleteByPostId(@Param("postId") UUID postId);
}
