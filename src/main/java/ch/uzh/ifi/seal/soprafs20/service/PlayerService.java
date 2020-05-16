package ch.uzh.ifi.seal.soprafs20.service;

import ch.uzh.ifi.seal.soprafs20.constant.PlayerRole;
import ch.uzh.ifi.seal.soprafs20.constant.PlayerStatus;
import ch.uzh.ifi.seal.soprafs20.constant.PlayerType;
import ch.uzh.ifi.seal.soprafs20.entity.Clue;
import ch.uzh.ifi.seal.soprafs20.entity.Player;
import ch.uzh.ifi.seal.soprafs20.entity.User;
import ch.uzh.ifi.seal.soprafs20.exceptions.ForbiddenException;
import ch.uzh.ifi.seal.soprafs20.repository.PlayerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.List;
import java.util.Set;

import java.util.Optional;

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
    public PlayerService(@Qualifier("playerRepository") PlayerRepository playerRepository) {
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
            player.setPlayerType(PlayerType.HUMAN);
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
        Optional<Player> player = playerRepository.findById(id);
        return player.orElseThrow(()->new ForbiddenException("Player not found"));
    }

    public Boolean checkPlayerToken(String token) {
        Player playerByToken = playerRepository.findByToken(token);
        return playerByToken == null;
    }

    public Boolean setPlayerReady(Player player) {
        player.setStatus(PlayerStatus.READY);
        return true;
    }

    public void setPlayerToNotReady(Player player) {
        player.setStatus(PlayerStatus.JOINED);
    }

    public Boolean isPlayerReady(Player player) {
        return player.getStatus() == PlayerStatus.READY;
    }

    /**
     * Checks if token belongs to the lobby creator and therefore is allowed to start the game
     *
     * @param token ,the Token of the Player that needs to be checked.
     * */
    public Boolean isAllowedToStart(String token) {
        boolean bool = false;
        Player playerByToken = playerRepository.findByToken(token);
        if (playerByToken != null) {
            PlayerRole role = playerByToken.getRole();
            bool = (role == PlayerRole.GUESSER);
        }
        return bool;
    }

    /**
     * Removes the player from Player repository
     *
     * @param - the Player to be removed from the Player repository
     * */
    public void deletePlayer(Player player) {
        if(player != null) {
            List<Clue> clues = player.getClues();
            for(Clue clue:clues){
                clue.setPlayer(null);
            }
            playerRepository.delete(player);
        }
    }

    /**
     * Removes the list of given players from Player repository
     *
     * @param - set of players to be removed from the Player repository
     * */
    public void deletePlayers(Set<Player> playersSet) {
        if(playersSet != null) {
            playerRepository.deleteAll(playersSet);
        }
    }

    public Player getPlayerByToken(String token)
    {
        Optional<Player> player = Optional.ofNullable(playerRepository.findByToken(token));
        return player.orElseThrow(()->new ForbiddenException("Player not found"));
    }

    public Boolean doesPlayerWithTokenExist(String token) {
        return playerRepository.findByToken(token) != null;
    }

    /**
     * Save all players
     * */
    public void saveAll(Set<Player> playerSet) {
        playerRepository.saveAll(playerSet);
    }

    public Player createBotPlayer(PlayerType playerType) {
        if (!playerType.equals(PlayerType.HUMAN)) {
            Player botPlayer = new Player();
            botPlayer.setPlayerType(playerType);
            botPlayer.setStatus(PlayerStatus.READY);
            botPlayer.setRole(PlayerRole.CLUE_CREATOR);
            String name = "";
            String token = "";
            SecureRandom random = new SecureRandom();
            Long id = random.nextLong();
            while (name.equals("") | playerRepository.findByUsername(name) != null) {
                String randomAddition = Integer.toString(random.nextInt(100));
                name = playerType.toString() + "_" + randomAddition;
            }

            while (token.equals("") | playerRepository.findByToken(token) != null) {
                token = Integer.toString(random.nextInt(1000000));
            }

            /*todo: make shûre no ID is used twice
            while (playerRepository.findById(id) != null) {
                id = random.nextLong();
            }

             */
            botPlayer.setUsername(name);
            botPlayer.setToken(token);
            botPlayer.setId(id);
            botPlayer = playerRepository.save(botPlayer);
            return botPlayer;
        }

        else {
            throw new UnsupportedOperationException("Is not Bot");
        }
    }
}
