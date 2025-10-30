package com.example.someprojectbackend.repo;

import com.example.someprojectbackend.domain.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for {@link User}-entiteten.
 *
 * Brukes til oppslag, validering og søk på brukere.
 */
public interface UserRepository extends JpaRepository<User, UUID> {

    /**
     * Henter en bruker basert på e-post.
     *
     * @param email e-postadresse
     * @return Optional med bruker hvis funnet
     */
    Optional<User> findByEmail(String email);

    /**
     * Sjekker om det finnes en bruker med gitt e-post.
     *
     * @param email e-postadresse
     * @return true hvis brukeren finnes, ellers false
     */
    boolean existsByEmail(String email);

    /**
     * Henter en bruker basert på visningsnavn (case-insensitivt).
     *
     * @param displayName visningsnavn å lete etter
     * @return Optional med bruker hvis funnet
     */
    @Query("select u from User u where lower(u.displayName) = lower(:dn)")
    Optional<User> findByDisplayNameCaseInsensitive(@Param("dn") String displayName);

    /**
     * Søker etter brukere basert på delvis match i displayName (case-insensitivt).
     * Brukes typisk for "search users"-funksjonalitet.
     *
     * @param q søkestreng
     * @param page pagineringsinformasjon
     * @return liste med brukere som matcher
     */
    @Query("""
        select u from User u
        where lower(u.displayName) like lower(concat('%', :q, '%'))
        order by u.displayName asc
    """)
    List<User> searchByDisplayName(String q, Pageable page);
}
