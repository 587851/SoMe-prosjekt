package com.example.someprojectbackend.service;

import com.example.someprojectbackend.repo.PostLikeRepository;
import com.example.someprojectbackend.repo.PostPopularRepository;
import com.example.someprojectbackend.web.dto.popular.PopularCursorDto;
import com.example.someprojectbackend.web.dto.popular.PopularPostsPageDto;
import com.example.someprojectbackend.web.dto.post.PostDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

/**
 * Service-klasse for å hente populære innlegg basert på likes og kommentarer.
 *
 * Bruker {@link PostPopularRepository} til å hente innlegg med beregnet "score"
 * og {@link PostLikeRepository} til å sjekke hvilke innlegg som er likt av viewer.
 *
 * Støtter keyset pagination med cursor (score + createdAt + id).
 */
@Service
public class PopularService {
    private final PostPopularRepository popularRepo;
    private final PostLikeRepository likeRepo;
    private final Clock clock;

    public PopularService(PostPopularRepository popularRepo,
                          PostLikeRepository likeRepo,
                          Clock clock) {
        this.popularRepo = popularRepo;
        this.likeRepo = likeRepo;
        this.clock = clock;
    }

    /**
     * Regner ut "since"-dato for en gitt range (f.eks. siste dag eller siste uke).
     *
     * @param range "day", "24h", "week" eller "7d" (default = "day")
     * @return tidspunkt som definerer nedre grense for innlegg
     */
    private Instant sinceForRange(String range) {
        Instant now = clock.instant();
        return switch ((range == null ? "day" : range).toLowerCase(Locale.ROOT)) {
            case "week", "7d" -> now.minus(Duration.ofDays(7));
            case "day", "24h" -> now.minus(Duration.ofDays(1));
            default -> now.minus(Duration.ofDays(1));
        };
    }

    /**
     * Henter en side med populære innlegg, sortert etter score og opprettelsestidspunkt.
     *
     * - Likes teller dobbelt, kommentarer teller én.
     * - Bruker keyset pagination for effektiv "infinite scroll".
     * - Returnerer {@link PostDto}-objekter med flagg for om viewer har likt innlegget.
     *
     * @param range tidsvindu (day/24h eller week/7d)
     * @param limit maks antall poster (1–50)
     * @param cursorScore score til siste post i forrige side (null for første side)
     * @param cursorCreatedAt tidspunkt til siste post i forrige side
     * @param cursorId id til siste post i forrige side
     * @param viewerId id til brukeren som ser feeden (kan være null)
     * @return {@link PopularPostsPageDto} med innlegg + eventuell neste cursor
     */
    @Transactional(readOnly = true)
    public PopularPostsPageDto listPopular(String range,
                                           int limit,
                                           Long cursorScore,
                                           Instant cursorCreatedAt,
                                           UUID cursorId,
                                           UUID viewerId) {

        int safeLimit = Math.max(1, Math.min(limit, 50));
        Instant since = sinceForRange(range);
        int fetch = safeLimit + 1; // hent én ekstra for å vite om det finnes neste side

        // Første side eller etter en cursor
        List<PostPopularRepository.PopularRow> rows =
                (cursorScore == null || cursorCreatedAt == null || cursorId == null)
                        ? popularRepo.findPopularFirst(since, fetch)
                        : popularRepo.findPopularAfter(since, cursorScore, cursorCreatedAt, cursorId, fetch);

        // Bygg cursor hvis vi fikk mer enn limit
        PopularCursorDto next = null;
        if (rows.size() > safeLimit) {
            var last = rows.remove(rows.size() - 1);
            next = new PopularCursorDto(last.getScore(), last.getCreatedAt(), last.getId());
        }

        // Hent hvilke innlegg viewer har likt
        final List<UUID> ids = rows.stream().map(PostPopularRepository.PopularRow::getId).toList();
        final Set<UUID> likedIds = (viewerId != null && !ids.isEmpty())
                ? new HashSet<>(likeRepo.findLikedPostIds(viewerId, ids))
                : Collections.emptySet();

        final boolean hasViewer = (viewerId != null);

        // Map database-rader til PostDto
        var posts = rows.stream().map(r ->
                new PostDto(
                        r.getId(),
                        r.getAuthor(),
                        r.getAuthorAvatarUrl(),
                        r.getContent(),
                        r.getImageUrl(),
                        r.getCreatedAt(),
                        r.getLikeCount(),
                        r.getCommentCount(),
                        hasViewer && likedIds.contains(r.getId())
                )
        ).toList();

        return new PopularPostsPageDto(posts, next);
    }
}
