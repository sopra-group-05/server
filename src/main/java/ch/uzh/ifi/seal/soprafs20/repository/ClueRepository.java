package ch.uzh.ifi.seal.soprafs20.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ch.uzh.ifi.seal.soprafs20.entity.Clue;
import ch.uzh.ifi.seal.soprafs20.entity.User;

@Repository("clueRepository")
public interface ClueRepository extends JpaRepository<Clue, Long> {
    Clue findClueById(long id);
    Clue findClueByUser(User user);
}


