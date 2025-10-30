package com.example.someprojectbackend.web;

import com.example.someprojectbackend.domain.User;
import com.example.someprojectbackend.repo.UserRepository;
import com.example.someprojectbackend.service.FollowService;
import com.example.someprojectbackend.service.PostService;
import com.example.someprojectbackend.web.dto.common.CursorDto;
import com.example.someprojectbackend.web.dto.user.FollowStatsDto;
import com.example.someprojectbackend.web.dto.post.PostsPageDto;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.Instant;
import java.util.UUID;

/**
 * REST-controller for følger-funksjonalitet.
 * <p>
 * Endepunkter:
 * - følge og slutte å følge en bruker
 * - hente følge-statistikk for en bruker
 * - hente home-feed (innlegg fra følgede brukere)
 */
@RestController
@RequestMapping("/api")
public class FollowController {
    private final FollowService followService;
    private final PostService postService;
    private final UserRepository users;

    public FollowController(FollowService followService, PostService postService, UserRepository users) {
        this.followService = followService;
        this.postService = postService;
        this.users = users;
    }

    /**
     * Henter innlogget bruker fra {@link Principal}, eller null hvis ikke logget inn.
     */
    private User current(Principal principal) {
        if (principal == null) return null;
        return users.findByEmail(principal.getName()).orElse(null);
    }

    /**
     * Følg en bruker.
     * <p>
     * POST /api/users/{displayName}/follow
     *
     * @param displayName displayName til brukeren som skal følges
     * @param principal   innlogget bruker (må være satt)
     */
    @PostMapping(value = "/users/{displayName}/follow")
    public void follow(@PathVariable String displayName, Principal principal) {
        var me = current(principal);
        if (me == null) throw new RuntimeException("Authentication required");
        followService.follow(me, displayName);
    }

    /**
     * Slutt å følge en bruker.
     * <p>
     * DELETE /api/users/{displayName}/follow
     */
    @DeleteMapping(value = "/users/{displayName}/follow")
    public void unfollow(@PathVariable String displayName, Principal principal) {
        var me = current(principal);
        if (me == null) throw new RuntimeException("Authentication required");
        followService.unfollow(me, displayName);
    }

    /**
     * Henter følge-statistikk for en bruker.
     * <p>
     * GET /api/users/{displayName}/follow-stats
     *
     * @param displayName brukeren vi henter statistikk for
     * @param principal   innlogget bruker (kan være null, men brukes til "followingByMe"-flagget)
     * @return DTO med followers, following og "followingByMe"
     */
    @GetMapping(value = "/users/{displayName}/follow-stats", produces = MediaType.APPLICATION_JSON_VALUE)
    public FollowStatsDto stats(@PathVariable String displayName, Principal principal) {
        var me = current(principal);
        return followService.getStats(me, displayName);
    }

    /**
     * Henter home-feed for innlogget bruker.
     * <p>
     * GET /api/home
     * <p>
     * - Viser innlegg kun fra brukere som innlogget bruker følger.
     * - Bruker keyset pagination (createdAt + id).
     *
     * @param limit           maks antall innlegg
     * @param cursorCreatedAt tidspunkt for siste post fra forrige side (valgfritt)
     * @param cursorId        id til siste post fra forrige side (valgfritt)
     * @param principal       innlogget bruker
     * @return feed-side med innlegg
     */
    @GetMapping(value = "/home", produces = MediaType.APPLICATION_JSON_VALUE)
    public PostsPageDto home(@RequestParam(defaultValue = "10") int limit,
                             @RequestParam(required = false) String cursorCreatedAt,
                             @RequestParam(required = false) UUID cursorId,
                             Principal principal) {
        var me = current(principal);
        var cursor = (cursorCreatedAt != null && cursorId != null)
                ? new CursorDto(Instant.parse(cursorCreatedAt), cursorId)
                : null;
        return postService.listHome(me, limit, cursor);
    }
}
