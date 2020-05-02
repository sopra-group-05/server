package ch.uzh.ifi.seal.soprafs20.repository;

import ch.uzh.ifi.seal.soprafs20.entity.GameStats;
import ch.uzh.ifi.seal.soprafs20.entity.Lobby;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository("statsRepository")
public interface StatsRepository extends JpaRepository<GameStats, Long> {

	GameStats findStatsByPlayerId(Long playerId);
	
}
