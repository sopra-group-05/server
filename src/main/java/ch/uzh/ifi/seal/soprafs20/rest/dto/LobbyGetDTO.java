package ch.uzh.ifi.seal.soprafs20.rest.dto;

import ch.uzh.ifi.seal.soprafs20.constant.GameModeStatus;
import ch.uzh.ifi.seal.soprafs20.constant.LobbyStatus;
import ch.uzh.ifi.seal.soprafs20.entity.Player;
import ch.uzh.ifi.seal.soprafs20.entity.Deck;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class LobbyGetDTO {

    private Long id;
    private String lobbyName;
    private LobbyStatus lobbyStatus;
    private List<Player> players;
    private GameModeStatus gameMode;
    private Player creator;
    private String language;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLobbyName() {
        return lobbyName;
    }

    public void setLobbyName(String lobbyName) {
        this.lobbyName = lobbyName;
    }

    public LobbyStatus getLobbyStatus() {
        return lobbyStatus;
    }

    public void setLobbyStatus(LobbyStatus lobbyStatus) {
        this.lobbyStatus = lobbyStatus;
    }

    public List<Player> getPlayers() {
        List<Player> playersAsList = new ArrayList<>(players);
        playersAsList.sort(Comparator.comparing(Player::getId));
        return playersAsList;
    }

    public void setPlayers(List<Player> players) {
        this.players = players;
    }

    public GameModeStatus getGameMode() {
        return gameMode;
    }

    public void setGameMode(GameModeStatus gameMode) {
        this.gameMode = gameMode;
    }

    public Player getCreator() {
        return creator;
    }

    public void setCreator(Player creator) {
        this.creator = creator;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getLanguage() {
        return language;
    }
}
