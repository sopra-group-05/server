package ch.uzh.ifi.seal.soprafs20.entity;

import ch.uzh.ifi.seal.soprafs20.constant.GameModeStatus;
import ch.uzh.ifi.seal.soprafs20.constant.LobbyStatus;
import ch.uzh.ifi.seal.soprafs20.helpers.Deck;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.*;

@Entity
@Table(name = "LOBBY")
public class Lobby
{
    @Id
    @Column(nullable = false)
    private Long id;
    @Column(nullable = false)
    private String lobbyName;
    @Column(nullable = true)
    private Deck deck;
    @Column(nullable = false)
    private LobbyStatus lobbyStatus;
    @Column(nullable = false)
    private ArrayList<User> players = new ArrayList<>();
    @Column(nullable = false)
    private GameModeStatus gameMode;
    @Column(nullable = false)
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
