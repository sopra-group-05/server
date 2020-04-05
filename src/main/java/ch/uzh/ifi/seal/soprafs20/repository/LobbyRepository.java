package ch.uzh.ifi.seal.soprafs20.repository;

import ch.uzh.ifi.seal.soprafs20.entity.Lobby;
import ch.uzh.ifi.seal.soprafs20.entity.Player;
import ch.uzh.ifi.seal.soprafs20.entity.User;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository("lobbyRepository")
public interface LobbyRepository extends JpaRepository<Lobby, Long> {

    Lobby findByLobbyName(String lobbyName);
    Lobby findByLobbyId(Long id);
    Lobby findByCreator(Player creator);
    @Override
    List<Lobby> findAll(Sort sort);
    // also haves delete, and findAll (see JPA Slides from Tutorial, page 25)
}
