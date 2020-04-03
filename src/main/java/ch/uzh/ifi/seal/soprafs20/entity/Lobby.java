package ch.uzh.ifi.seal.soprafs20.entity;

import ch.uzh.ifi.seal.soprafs20.constant.GameModeStatus;
import ch.uzh.ifi.seal.soprafs20.constant.Language;
import ch.uzh.ifi.seal.soprafs20.constant.LobbyStatus;
import ch.uzh.ifi.seal.soprafs20.helpers.Deck;

import javax.persistence.*;
import java.util.*;

/**
 * Internal Lobby Representation
 * This class composes the internal representation of the Lobby and defines how the Lobby data is stored in the database.
 * Every variable will be mapped into a database field with the @Column annotation
 * - nullable = false -> this cannot be left empty
 * - unique = true -> this value must be unqiue across the database -> composes the primary key
 */
@Entity
@Table(name = "LOBBY")
public class Lobby
{
    @Id
    @Column(nullable = false, unique = true)
    @GeneratedValue
    private Long lobbyId;
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
    @Column(nullable = false)
    private Language language;

    public Long getId() {
        return lobbyId;
    }

    public void setId(Long id) {
        this.lobbyId = id;
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

    public void setLanguage(Language language) {
        this.language = language;
    }

    public Language getLanguage() {
        return language;
    }
}
