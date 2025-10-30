package com.example.someprojectbackend.service;

import com.example.someprojectbackend.domain.User;
import com.example.someprojectbackend.domain.UserFollow;
import com.example.someprojectbackend.repo.UserFollowRepository;
import com.example.someprojectbackend.repo.UserRepository;
import com.example.someprojectbackend.web.dto.user.FollowStatsDto;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

/**
 * Service-klasse for å håndtere "følg"-relasjoner mellom brukere.
 *
 * Funksjonalitet:
 *  - følge en bruker
 *  - slutte å følge en bruker
 *  - hente følgestatistikk (antall følgere, antall fulgte, om viewer følger brukeren)
 */
@Service
public class FollowService {
    private final UserRepository users;
    private final UserFollowRepository follows;

    public FollowService(UserRepository users, UserFollowRepository follows) {
        this.users = users;
        this.follows = follows;
    }

    /**
     * Oppretter en "følge"-relasjon fra {@code follower} til brukeren med {@code targetDisplayName}.
     *
     * - Kaster 404 hvis target-bruker ikke finnes
     * - Kaster 400 hvis man prøver å følge seg selv
     * - Oppretter kun relasjonen hvis den ikke allerede finnes
     *
     * @param follower brukeren som følger
     * @param targetDisplayName display name til brukeren som skal følges
     */
    @Transactional
    public void follow(User follower, String targetDisplayName) {
        var target = users.findByDisplayNameCaseInsensitive(targetDisplayName)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        if (follower.getId().equals(target.getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot follow yourself");
        }

        if (!follows.existsByFollower_IdAndFollowee_Id(follower.getId(), target.getId())) {
            var uf = new UserFollow();
            uf.setFollower(follower);
            uf.setFollowee(target);
            follows.save(uf);
        }
    }

    /**
     * Sletter en "følge"-relasjon fra {@code follower} til brukeren med {@code targetDisplayName}.
     *
     * - Kaster 404 hvis target-bruker ikke finnes
     *
     * @param follower brukeren som slutter å følge
     * @param targetDisplayName display name til brukeren som skal unfølges
     */
    @Transactional
    public void unfollow(User follower, String targetDisplayName) {
        var target = users.findByDisplayNameCaseInsensitive(targetDisplayName)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        follows.deleteByFollower_IdAndFollowee_Id(follower.getId(), target.getId());
    }

    /**
     * Henter følgestatistikk for en gitt bruker.
     *
     * @param viewer brukeren som ser på profilen (kan være null hvis ikke innlogget)
     * @param displayName display name til brukeren vi henter statistikk for
     * @return {@link FollowStatsDto} med displayName, avatar, antall følgere, antall følgede, og flagg for "følger jeg?"
     */
    @Transactional(readOnly = true)
    public FollowStatsDto getStats(User viewer, String displayName) {
        var u = users.findByDisplayNameCaseInsensitive(displayName)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        long followers = follows.countByFollowee_Id(u.getId()); // antall følgere
        long following = follows.countByFollower_Id(u.getId()); // antall fulgte
        boolean followingByMe = viewer != null
                && follows.existsByFollower_IdAndFollowee_Id(viewer.getId(), u.getId());

        return new FollowStatsDto(u.getDisplayName(), u.getAvatarKey(), followers, following, followingByMe);
    }
}
