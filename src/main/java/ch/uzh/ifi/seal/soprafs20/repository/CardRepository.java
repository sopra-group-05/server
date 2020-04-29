package ch.uzh.ifi.seal.soprafs20.repository;

import ch.uzh.ifi.seal.soprafs20.entity.Card;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository("cardRepository")
public interface CardRepository extends JpaRepository<Card, Long> {
    @Query(value = "SELECT * FROM CARD ORDER BY random() LIMIT 13", nativeQuery = true)
    List<Card> findLimit13Words();
}
