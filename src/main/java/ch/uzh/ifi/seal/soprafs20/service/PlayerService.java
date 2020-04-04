package ch.uzh.ifi.seal.soprafs20.service;

import ch.uzh.ifi.seal.soprafs20.constant.PlayerRole;
import ch.uzh.ifi.seal.soprafs20.entity.Player;
import ch.uzh.ifi.seal.soprafs20.entity.User;
import ch.uzh.ifi.seal.soprafs20.repository.PlayerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Player Service
 * This class is the "worker" and responsible for all functionality related to the Player
 * (e.g., it converts, modifies, deletes, finds). The result will be passed back to the caller.
 */
@Service
@Transactional
public class PlayerService {

    private final Logger log = LoggerFactory.getLogger(PlayerService.class);

    private final PlayerRepository playerRepository;

    @Autowired
    public PlayerService(PlayerRepository playerRepository) {
        this.playerRepository = playerRepository;
    }

    /**
     * Converts the given User to the playerRepository
     * @param user
     * @return Player
     */
    public Player convertUserToPlayer(User user, PlayerRole playerRole) {
            Player player = new Player(user);
            player.setRole(playerRole);
            player = playerRepository.save(player);
            playerRepository.flush();
            return player;
    }

    /**
     * This method will get a specific Player by ID from the Player Repository
     *
     * @return The requested Player
     * @see ch.uzh.ifi.seal.soprafs20.entity.Player
     */
    public Player getPlayerById(Long id)
    {
        return playerRepository.findPlayerById(id);
    }
}
