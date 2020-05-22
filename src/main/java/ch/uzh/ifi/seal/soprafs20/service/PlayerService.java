package ch.uzh.ifi.seal.soprafs20.service;

import ch.uzh.ifi.seal.soprafs20.constant.PlayerRole;
import ch.uzh.ifi.seal.soprafs20.constant.PlayerStatus;
import ch.uzh.ifi.seal.soprafs20.constant.PlayerType;
import ch.uzh.ifi.seal.soprafs20.entity.Clue;
import ch.uzh.ifi.seal.soprafs20.entity.Lobby;
import ch.uzh.ifi.seal.soprafs20.entity.Player;
import ch.uzh.ifi.seal.soprafs20.entity.User;
import ch.uzh.ifi.seal.soprafs20.exceptions.ForbiddenException;
import ch.uzh.ifi.seal.soprafs20.exceptions.UnauthorizedException;
import ch.uzh.ifi.seal.soprafs20.repository.PlayerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.ArrayList;
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
     * @param user - user to be converted
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
     * @param player - the Player to be removed from the Player repository
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
     * @param playersSet - set of players to be removed from the Player repository
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
            while (name.equals("") || playerRepository.findByUsername(name) != null) {
                String randomAddition = Integer.toString(random.nextInt(100));
                name = playerType.toString() + "_" + randomAddition;
            }

            while (token.equals("") || playerRepository.findByToken(token) != null) {
                token = Integer.toString(random.nextInt(1000000));
            }

            /*todo: make sure no ID is used twice
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

    /*
     * helper function to get all Players that are humans
     * @param lobby - lobby to check for human players
     * @return List<Player> - list of human players in the lobby
     */
    public List<Player> getHumanPlayersExceptActivePlayer(Lobby lobby){
        Set<Player> players= lobby.getPlayers();
        List<Player> humanPlayers= new ArrayList<>();
        for(Player player:players){
            if(player.getPlayerType().equals(PlayerType.HUMAN) && !player.getRole().equals(PlayerRole.GUESSER)){
                humanPlayers.add(player);
            }
        }
        return humanPlayers;
    }

    /*
     * helper function to get all Players that are bots
     * @param lobby - lobby to check for bot players
     * @return List<Player> - list of bot players
     */
    public List<Player> getBotPlayers(Lobby lobby){
        Set<Player> players= lobby.getPlayers();
        List<Player> humanPlayers= new ArrayList<>();
        for(Player player:players){
            if(player.getPlayerType().equals(PlayerType.FRIENDLYBOT) ||  player.getPlayerType().equals(PlayerType.MALICIOUSBOT)
                    || player.getPlayerType().equals(PlayerType.CRAZYBOT)){
                humanPlayers.add(player);
            }
        }
        return humanPlayers;
    }

    /*
     * helper function to check if a player is allowed to annotate clues
     * @param token - token of the player to identify him
     */

    public boolean playerIsClueCreator(String token){
        Player player = this.getPlayerByToken(token);
        if (player.getRole() != PlayerRole.CLUE_CREATOR){
            throw new UnauthorizedException("Player is not Clue Creator");
        } else {
            return true;
        }
    }

    /*
     * helper function to check wheter the player is in the lobby
     * @param token - token of the player
     * @param lobby - lobby to check if the player is part of
     */
    public boolean playerIsInLobby(String token, Lobby lobby){
        Player player = this.getPlayerByToken(token);
        if (player == null) {
            throw new UnauthorizedException("User is not a Player");
        }
        if (!lobby.getPlayers().contains(player)){
            throw new UnauthorizedException("Player is not in Lobby");
        } else{
            return true;
        }

    }



}
