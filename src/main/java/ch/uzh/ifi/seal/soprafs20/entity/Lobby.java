package ch.uzh.ifi.seal.soprafs20.entity;

import ch.uzh.ifi.seal.soprafs20.constant.GameModeStatus;
import ch.uzh.ifi.seal.soprafs20.constant.Language;
import ch.uzh.ifi.seal.soprafs20.constant.LobbyStatus;

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

    @ElementCollection
    private Set<Player> players = new HashSet<>();

    @Column(nullable = false)
    private GameModeStatus gameMode;

    @OneToOne
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

    public Set<Player> getPlayers() {
        return players;
    }

    public void addPlayer(Player player) {
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

    public void join(Player player){
        this.players.add(player);
    }
    public void leave(Player player){
        this.players.remove(player);
    }
}
