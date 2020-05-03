package ch.uzh.ifi.seal.soprafs20.repository;

import ch.uzh.ifi.seal.soprafs20.entity.GameStats;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository("statsRepository")
public interface StatsRepository extends JpaRepository<GameStats, Long> {

	GameStats findByPlayerIdAndLobbyId(Long playerId, Long lobbyId);
	
	List<GameStats> findAllByLobbyId(Long lobbyId);
	
}
