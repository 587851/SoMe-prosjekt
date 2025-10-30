// src/main/java/com/example/someprojectbackend/repo/PostPopularRepository.java
package com.example.someprojectbackend.repo;

import com.example.someprojectbackend.domain.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Repository for å hente populære {@link Post}-objekter.
 *
 * Bruker native SQL-spørringer for å beregne et "score"-felt basert på:
 *  - antall likes (vektet dobbelt)
 *  - antall kommentarer
 *
 * Resultatene brukes for å bygge en populær-feed
 * med keyset pagination (dvs. effektiv "uendelig scroll").
 */
@Repository
public interface PostPopularRepository extends JpaRepository<Post, UUID> {

    /**
     * Projeksjonsgrensesnitt for rader hentet av spørringene.
     * Mapper direkte til kolonnealias definert i SQL.
     */
    interface PopularRow {
        UUID getId();
        Instant getCreatedAt();
        String getContent();
        String getImageUrl();
        String getAuthor();          // users.display_name
        String getAuthorAvatarUrl(); // users.avatar_key
        long getLikeCount();
        long getCommentCount();
        long getScore();             // (likes * 2 + comments)
    }

    /**
     * Henter første side med populære innlegg, basert på aktivitet siden et gitt tidspunkt.
     *
     * Sortering: høyeste score først, deretter nyeste innlegg.
     *
     * @param since bare innlegg nyere enn denne tidsverdien tas med
     * @param limit maks antall resultater
     * @return liste av {@link PopularRow}-projeksjoner
     */
    @Query(value = """
        SELECT
          p.id                          AS id,
          p.created_at                  AS createdAt,
          p.content                     AS content,
          p.image_url                   AS imageUrl,
          u.display_name                AS author,
          u.avatar_key                  AS authorAvatarUrl,
          COALESCE(l.like_count, 0)     AS likeCount,
          COALESCE(cm.comment_count, 0) AS commentCount,
          (COALESCE(l.like_count,0)*2 + COALESCE(cm.comment_count,0)) AS score
        FROM posts p
        JOIN users u ON u.id = p.author_id
        LEFT JOIN (
          SELECT post_id, COUNT(*)::bigint AS like_count
          FROM post_likes
          GROUP BY post_id
        ) l ON l.post_id = p.id
        LEFT JOIN (
          SELECT post_id, COUNT(*)::bigint AS comment_count
          FROM comments
          GROUP BY post_id
        ) cm ON cm.post_id = p.id
        WHERE p.created_at >= :since
        ORDER BY score DESC, p.created_at DESC, p.id DESC
        LIMIT :limit
        """, nativeQuery = true)
    List<PopularRow> findPopularFirst(
            @Param("since") Instant since,
            @Param("limit") int limit
    );

    /**
     * Henter neste side (paginering) av populære innlegg basert på en "cursor".
     *
     * Keyset pagination gjøres her ved å bruke:
     *  - score (likes*2 + comments)
     *  - createdAt
     *  - id
     *
     * Dermed kan man fortsette der forrige side sluttet, uten å bruke offset.
     *
     * @param since bare innlegg nyere enn denne tidsverdien tas med
     * @param cursorScore score til siste rad i forrige resultat
     * @param cursorCreatedAt opprettelsestidspunkt til siste rad i forrige resultat
     * @param cursorId ID til siste rad i forrige resultat
     * @param limit maks antall resultater
     * @return liste av {@link PopularRow}-projeksjoner
     */
    @Query(value = """
        SELECT
          p.id                          AS id,
          p.created_at                  AS createdAt,
          p.content                     AS content,
          p.image_url                   AS imageUrl,
          u.display_name                AS author,
          u.avatar_key                  AS authorAvatarUrl,
          COALESCE(l.like_count, 0)     AS likeCount,
          COALESCE(cm.comment_count, 0) AS commentCount,
          (COALESCE(l.like_count,0)*2 + COALESCE(cm.comment_count,0)) AS score
        FROM posts p
        JOIN users u ON u.id = p.author_id
        LEFT JOIN (
          SELECT post_id, COUNT(*)::bigint AS like_count
          FROM post_likes
          GROUP BY post_id
        ) l ON l.post_id = p.id
        LEFT JOIN (
          SELECT post_id, COUNT(*)::bigint AS comment_count
          FROM comments
          GROUP BY post_id
        ) cm ON cm.post_id = p.id
        WHERE p.created_at >= :since
          AND (
            ( (COALESCE(l.like_count,0)*2 + COALESCE(cm.comment_count,0)) < :cursorScore )
            OR (
              ( (COALESCE(l.like_count,0)*2 + COALESCE(cm.comment_count,0)) = :cursorScore )
              AND ( p.created_at < :cursorCreatedAt
                    OR (p.created_at = :cursorCreatedAt AND p.id < :cursorId) )
            )
          )
        ORDER BY score DESC, p.created_at DESC, p.id DESC
        LIMIT :limit
        """, nativeQuery = true)
    List<PopularRow> findPopularAfter(
            @Param("since") Instant since,
            @Param("cursorScore") long cursorScore,
            @Param("cursorCreatedAt") Instant cursorCreatedAt,
            @Param("cursorId") UUID cursorId,
            @Param("limit") int limit
    );
}
