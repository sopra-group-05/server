package ch.uzh.ifi.seal.soprafs20.entity;

import ch.uzh.ifi.seal.soprafs20.constant.GameModeStatus;
import ch.uzh.ifi.seal.soprafs20.constant.LobbyStatus;
import ch.uzh.ifi.seal.soprafs20.helpers.Deck;

import java.util.*;

public class Lobby
{
    private Long id;
    private String lobbyName;
    private Deck deck;
    private LobbyStatus lobbyStatus;
    private ArrayList<User> players = new ArrayList<>();
    private GameModeStatus gameMode;
    private User creator;

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

    public Deck getDeck() {
        return deck;
    }

    public void setDeck(Deck deck) {
        this.deck = deck;
    }

    public LobbyStatus getLobbyStatus() {
        return lobbyStatus;
    }

    public void setLobbyStatus(LobbyStatus lobbyStatus) {
        this.lobbyStatus = lobbyStatus;
    }

    public ArrayList<User> getPlayers() {
        return players;
    }

    public void addPlayer(User player) {
        this.players.add(player);
    }

    public GameModeStatus getGameMode() {
        return gameMode;
    }

    public void setGameMode(GameModeStatus gameMode) {
        this.gameMode = gameMode;
    }

    public User getCreator() {
        return creator;
    }

    public void setCreator(User creator) {
        this.creator = creator;
    }
}
