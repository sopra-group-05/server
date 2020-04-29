package ch.uzh.ifi.seal.soprafs20.repository;

import ch.uzh.ifi.seal.soprafs20.entity.MysteryWord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository("mysteryWordRepository")
public interface MysteryWordRepository extends JpaRepository<MysteryWord, Long> {

    @Query(value = "SELECT * FROM MYSTERYWORD ORDER BY random() LIMIT 65", nativeQuery = true)
    List<MysteryWord> findAllLimit65Words();
}
