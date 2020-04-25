package ch.uzh.ifi.seal.soprafs20.repository;

import ch.uzh.ifi.seal.soprafs20.entity.MysteryWord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository("mysteryWordRepository")
public interface MysteryWordRepository extends JpaRepository<MysteryWord, Long> {
}
