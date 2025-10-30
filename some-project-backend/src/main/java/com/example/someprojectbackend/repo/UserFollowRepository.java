package com.example.someprojectbackend.repo;

import com.example.someprojectbackend.domain.UserFollow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

/**
 * Repository for {@link UserFollow}-entiteten.
 *
 * Brukes til å håndtere "følger-relasjoner" mellom brukere,
 * inkludert oppslag, telling og sletting.
 */
public interface UserFollowRepository extends JpaRepository<UserFollow, Long> {

    /**
     * Sjekker om en bruker allerede følger en annen.
     *
     * @param followerId ID til brukeren som følger
     * @param followeeId ID til brukeren som blir fulgt
     * @return true hvis follower følger followee
     */
    boolean existsByFollower_IdAndFollowee_Id(UUID followerId, UUID followeeId);

    /**
     * Sletter en følge-relasjon mellom to brukere.
     *
     * @param followerId ID til brukeren som følger
     * @param followeeId ID til brukeren som blir fulgt
     */
    void deleteByFollower_IdAndFollowee_Id(UUID followerId, UUID followeeId);

    /**
     * Teller hvor mange brukere en gitt bruker følger.
     *
     * @param followerId ID til brukeren
     * @return antall brukere som følges
     */
    long countByFollower_Id(UUID followerId);

    /**
     * Teller hvor mange følgere en gitt bruker har.
     *
     * @param followeeId ID til brukeren
     * @return antall følgere
     */
    long countByFollowee_Id(UUID followeeId);

    /**
     * Henter ID-ene til alle brukere som en gitt bruker følger.
     * Brukes f.eks. for å hente feed av innlegg fra følgede brukere.
     *
     * @param followerId ID til brukeren
     * @return liste med ID-er til følgede brukere
     */
    @Query("select uf.followee.id from UserFollow uf where uf.follower.id = :followerId")
    List<UUID> findFolloweeIdsByFollowerId(UUID followerId);
}
