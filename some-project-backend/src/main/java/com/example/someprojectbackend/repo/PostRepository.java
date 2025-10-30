package com.example.someprojectbackend.repo;

import com.example.someprojectbackend.domain.Post;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Repository for {@link Post}-entiteten.
 *
 * Inneholder metoder for å hente feed av innlegg
 * (globale, per forfatter eller per forfatterliste),
 * implementert med keyset pagination for effektiv "infinite scroll".
 */
public interface PostRepository extends JpaRepository<Post, UUID> {

    /**
     * Henter første side med innlegg, sortert etter opprettelsestidspunkt (nyeste først).
     * Bruker JOIN FETCH for å hente forfatter samtidig (unngår N+1).
     *
     * @param pageable pagineringsinfo (begrensning/størrelse)
     * @return liste av innlegg
     */
    @Query("""
            select p from Post p
            join fetch p.author
            order by p.createdAt desc, p.id desc
            """)
    List<Post> findFirstPage(Pageable pageable);

    /**
     * Henter neste side med innlegg etter en gitt "cursor" (createdAt + id).
     * Brukes til effektiv keyset pagination.
     *
     * @param createdAt tidspunkt for siste post i forrige side
     * @param id id til siste post i forrige side
     * @param pageable pagineringsinfo
     * @return liste av innlegg
     */
    @Query("""
            select p from Post p
            join fetch p.author
            where (p.createdAt < :createdAt)
               or (p.createdAt = :createdAt and p.id < :id)
            order by p.createdAt desc, p.id desc
            """)
    List<Post> findPageAfter(Instant createdAt, UUID id, Pageable pageable);

    /**
     * Henter første side med innlegg skrevet av en bestemt forfatter.
     *
     * @param displayName visningsnavnet til forfatteren
     * @param pageable pagineringsinfo
     * @return liste av innlegg
     */
    @Query("""
            select p from Post p
            join fetch p.author a
            where a.displayName = :displayName
            order by p.createdAt desc, p.id desc
            """)
    List<Post> findFirstPageByAuthor(String displayName, Pageable pageable);

    /**
     * Henter neste side med innlegg skrevet av en bestemt forfatter, etter en gitt cursor.
     *
     * @param displayName visningsnavnet til forfatteren
     * @param createdAt tidspunkt for siste post i forrige side
     * @param id id til siste post i forrige side
     * @param pageable pagineringsinfo
     * @return liste av innlegg
     */
    @Query("""
            select p from Post p
            join fetch p.author a
            where a.displayName = :displayName
              and (p.createdAt < :createdAt
                   or (p.createdAt = :createdAt and p.id < :id))
            order by p.createdAt desc, p.id desc
            """)
    List<Post> findPageAfterByAuthor(String displayName, Instant createdAt, UUID id, Pageable pageable);

    /**
     * Henter første side med innlegg skrevet av en liste med forfattere.
     * Brukes typisk til å hente feed for en bruker (følgede brukere).
     *
     * @param authorIds liste av forfatter-IDer
     * @param pageable pagineringsinfo
     * @return liste av innlegg
     */
    @Query("""
            select p from Post p
            where p.author.id in :authorIds
            order by p.createdAt desc, p.id desc
            """)
    List<Post> findFirstPageByAuthorIds(List<UUID> authorIds, Pageable pageable);

    /**
     * Henter neste side med innlegg skrevet av en liste med forfattere, etter en gitt cursor.
     *
     * @param authorIds liste av forfatter-IDer
     * @param cursorCreatedAt tidspunkt for siste post i forrige side
     * @param cursorId id til siste post i forrige side
     * @param pageable pagineringsinfo
     * @return liste av innlegg
     */
    @Query("""
            select p from Post p
            where p.author.id in :authorIds
              and (p.createdAt < :cursorCreatedAt
                   or (p.createdAt = :cursorCreatedAt and p.id < :cursorId))
            order by p.createdAt desc, p.id desc
            """)
    List<Post> findPageAfterByAuthorIds(
            List<UUID> authorIds,
            Instant cursorCreatedAt,
            UUID cursorId,
            Pageable pageable
    );
}
