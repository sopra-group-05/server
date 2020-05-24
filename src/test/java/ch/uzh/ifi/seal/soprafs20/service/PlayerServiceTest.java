package ch.uzh.ifi.seal.soprafs20.service;

import ch.uzh.ifi.seal.soprafs20.constant.PlayerRole;
import ch.uzh.ifi.seal.soprafs20.constant.PlayerStatus;
import ch.uzh.ifi.seal.soprafs20.constant.PlayerType;
import ch.uzh.ifi.seal.soprafs20.entity.Lobby;
import ch.uzh.ifi.seal.soprafs20.entity.Player;
import ch.uzh.ifi.seal.soprafs20.entity.User;
import ch.uzh.ifi.seal.soprafs20.exceptions.ForbiddenException;
import ch.uzh.ifi.seal.soprafs20.exceptions.NotFoundException;
import ch.uzh.ifi.seal.soprafs20.exceptions.UnauthorizedException;
import ch.uzh.ifi.seal.soprafs20.repository.PlayerRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import javax.validation.constraints.AssertTrue;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class PlayerServiceTest {

    @Mock
    private PlayerRepository playerRepository;

    @InjectMocks
    private PlayerService playerService;

    private Player player;

    private Player botPlayer;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.initMocks(this);
        player = new Player();
        player.setId(1L);
        player.setToken("abc");
        player.setUsername("user1");
        player.setRole(PlayerRole.CLUE_CREATOR);
        player.setPlayerType(PlayerType.HUMAN);
        player.setStatus(PlayerStatus.JOINED);

        botPlayer = new Player();
        botPlayer.setPlayerType(PlayerType.FRIENDLYBOT);
        botPlayer.setStatus(PlayerStatus.READY);
        botPlayer.setRole(PlayerRole.CLUE_CREATOR);
        botPlayer.setUsername("botPlayer");
        botPlayer.setId(2L);
        botPlayer.setToken("def");
    }

    @Test
    public void convertUserToPlayerSuccess(){
        Mockito.when(playerRepository.save(Mockito.any(Player.class))).thenReturn(player);
        User user = new User();
        user.setId(1L);
        user.setToken("abc");
        user.setUsername("user1");
        Player createdPlayer = playerService.convertUserToPlayer(user, PlayerRole.CLUE_CREATOR);
        Assertions.assertEquals(player, createdPlayer);
    }

    @Test
    public void getPlayerByIdWrongId(){
        Mockito.when(playerRepository.findById(Mockito.anyLong())).thenReturn(null);
        String exceptionMessage = "Player not found";
        ForbiddenException exception = assertThrows(ForbiddenException.class, () -> playerService.getPlayerById(Mockito.anyLong()), exceptionMessage);
        assertEquals(exceptionMessage, exception.getMessage());
    }

    @Test
    public void checkPlayerTokenSuccess(){
        Mockito.when(playerRepository.findByToken("abc")).thenReturn(player);
        Assertions.assertFalse(playerService.checkPlayerToken("abc"));
    }

    @Test
    public void checkPlayerTokenNotFound(){
        Mockito.when(playerRepository.findByToken(Mockito.anyString())).thenReturn(null);
        Assertions.assertTrue(playerService.checkPlayerToken("abc"));
    }

    @Test
    public void setPlayerReadySuccess(){
        Assertions.assertTrue(playerService.setPlayerReady(player));
        assertEquals(PlayerStatus.READY, player.getStatus());
    }

    @Test
    public void setPlayerToNotReadySuccess(){
        player.setStatus(PlayerStatus.READY);
        playerService.setPlayerToNotReady(player);
        Assertions.assertEquals(PlayerStatus.JOINED, player.getStatus());
    }

    @Test
    public void isPlayerReadyTrue(){
        player.setStatus(PlayerStatus.READY);
        Assertions.assertTrue(playerService.isPlayerReady(player));

    }

    @Test
    public void isPlayerReadyFalse(){
        Assertions.assertFalse(playerService.isPlayerReady(player));
    }

    @Test
    public void isAllowedToStartTrue(){
        player.setRole(PlayerRole.GUESSER);
        Mockito.when(playerRepository.findByToken(Mockito.anyString())).thenReturn(player);
        Assertions.assertTrue(playerService.isAllowedToStart("abc"));
    }

    @Test
    public void isAllowedToStartFalse(){
        Mockito.when(playerRepository.findByToken(Mockito.anyString())).thenReturn(player);
        Assertions.assertFalse(playerService.isAllowedToStart("abc"));
    }

    @Test
    public void getPlayerByTokenSuccess(){
        Mockito.when(playerRepository.findByToken(Mockito.anyString())).thenReturn(player);
        Player playerByToken = playerService.getPlayerByToken("abc");
        Assertions.assertEquals(player, playerByToken);
    }

    @Test
    public void getPlayerByTokenNotFound(){
        Mockito.when(playerRepository.findByToken(Mockito.anyString())).thenReturn(null);
        String exceptionMessage = "Player not found";
        ForbiddenException exception = assertThrows(ForbiddenException.class, () -> playerService.getPlayerByToken(Mockito.anyString()), exceptionMessage);
        assertEquals(exceptionMessage, exception.getMessage());
    }

    @Test
    public void createBotPlayerSuccess(){
        Mockito.when(playerRepository.findByToken(Mockito.anyString())).thenReturn(null);
        Mockito.when(playerRepository.findById(Mockito.anyLong())).thenReturn(null);
        Mockito.when(playerRepository.findByUsername(Mockito.anyString())).thenReturn(null);
        Mockito.when(playerRepository.save(Mockito.any())).thenReturn(botPlayer);
        Player botPlayer2 = playerService.createBotPlayer(PlayerType.FRIENDLYBOT);
        Assertions.assertEquals(botPlayer, botPlayer2);

    }

    @Test
    public void createBotPlayerNotBot(){
        String exceptionMessage = "Is not Bot";
        UnsupportedOperationException exception = assertThrows(UnsupportedOperationException.class, () -> playerService.createBotPlayer(PlayerType.HUMAN), exceptionMessage);
        assertEquals(exceptionMessage, exception.getMessage());
    }

    @Test
    public void getHumanPlayersExceptActivePlayerOnlyHumans(){
        Player player2 = new Player();
        player2.setPlayerType(PlayerType.HUMAN);
        player2.setRole(PlayerRole.CLUE_CREATOR);
        Player player3 = new Player();
        player3.setPlayerType(PlayerType.HUMAN);
        player3.setRole(PlayerRole.CLUE_CREATOR);
        Player player4 = new Player();
        player4.setPlayerType(PlayerType.HUMAN);
        player4.setRole(PlayerRole.GUESSER);
        Lobby lobby = new Lobby();
        lobby.addPlayer(player2);
        lobby.addPlayer(player);
        lobby.addPlayer(player3);
        lobby.addPlayer(player4);
        List players = playerService.getHumanPlayersExceptActivePlayer(lobby);
        Assertions.assertTrue(players.contains(player2));
        Assertions.assertTrue(players.contains(player3));
        Assertions.assertTrue(players.contains(player));
        Assertions.assertTrue(players.size() == 3);

    }

    @Test
    public void getHumanPlayersExceptActivePlayerWithBots(){
        Player player2 = new Player();
        player2.setPlayerType(PlayerType.HUMAN);
        player2.setRole(PlayerRole.CLUE_CREATOR);
        Player player4 = new Player();
        player4.setPlayerType(PlayerType.HUMAN);
        player4.setRole(PlayerRole.GUESSER);
        Lobby lobby = new Lobby();
        lobby.addPlayer(player2);
        lobby.addPlayer(player);
        lobby.addPlayer(botPlayer);
        lobby.addPlayer(player4);
        List players = playerService.getHumanPlayersExceptActivePlayer(lobby);
        Assertions.assertTrue(players.contains(player2));
        Assertions.assertTrue(players.contains(player));
        Assertions.assertTrue(players.size() == 2);

    }

    @Test
    public void getBotPlayersNoBotPlayers(){
        Player player2 = new Player();
        player2.setPlayerType(PlayerType.HUMAN);
        Player player3 = new Player();
        player3.setPlayerType(PlayerType.HUMAN);
        Player player4 = new Player();
        player4.setPlayerType(PlayerType.HUMAN);
        Lobby lobby = new Lobby();
        lobby.addPlayer(player2);
        lobby.addPlayer(player);
        lobby.addPlayer(player3);
        lobby.addPlayer(player4);
        List players = playerService.getBotPlayers(lobby);
        Assertions.assertTrue(players.size() == 0);
    }

    @Test
    public void getBotPlayersWithBotPlayers(){
        Player player3 = new Player();
        player3.setPlayerType(PlayerType.CRAZYBOT);
        Player player4 = new Player();
        player4.setPlayerType(PlayerType.HUMAN);
        Lobby lobby = new Lobby();
        lobby.addPlayer(botPlayer);
        lobby.addPlayer(player);
        lobby.addPlayer(player3);
        lobby.addPlayer(player4);
        List players = playerService.getBotPlayers(lobby);
        Assertions.assertTrue(players.contains(botPlayer));
        Assertions.assertTrue(players.contains(player3));
        Assertions.assertTrue(players.size() == 2);
    }

    @Test
    public void playerIsClueCreatorTrue(){
        Mockito.when(playerRepository.findByToken(Mockito.anyString())).thenReturn(player);
        Assertions.assertTrue(playerService.playerIsClueCreator("abc"));
    }

    @Test
    public void playerIsClueCreatorFalse(){
        player.setRole(PlayerRole.GUESSER);
        Mockito.when(playerRepository.findByToken(Mockito.anyString())).thenReturn(player);
        String exceptionMessage = "Player is not Clue Creator";
        UnauthorizedException exception = assertThrows(UnauthorizedException.class, () -> playerService.playerIsClueCreator(Mockito.anyString()), exceptionMessage);
        assertEquals(exceptionMessage, exception.getMessage());
    }

    @Test
    public void playerIsInLobbyTrue(){
        Mockito.when(playerRepository.findByToken(Mockito.anyString())).thenReturn(player);
        Lobby lobby = new Lobby();
        lobby.addPlayer(player);
        Assertions.assertTrue(playerService.playerIsInLobby("abc", lobby));
    }

    @Test
    public void playerIsInLobbyFalse(){
        Mockito.when(playerRepository.findByToken(Mockito.anyString())).thenReturn(player);
        Lobby lobby = new Lobby();
        String exceptionMessage = "Player is not in Lobby";
        UnauthorizedException exception = assertThrows(UnauthorizedException.class, () -> playerService.playerIsInLobby(Mockito.anyString(), lobby), exceptionMessage);
        assertEquals(exceptionMessage, exception.getMessage());
    }
}
