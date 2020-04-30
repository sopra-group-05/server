package ch.uzh.ifi.seal.soprafs20.repository;

import ch.uzh.ifi.seal.soprafs20.constant.Language;
import ch.uzh.ifi.seal.soprafs20.entity.Card;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository("cardRepository")
public interface CardRepository extends JpaRepository<Card, Long> {

    @Query(value = "SELECT * FROM CARD c where c.language = :language ORDER BY random() LIMIT 13", nativeQuery = true)
    List<Card> findLimit13Words(@Param("language") String language);

    List<Card> findByLanguage(Language language);
}
