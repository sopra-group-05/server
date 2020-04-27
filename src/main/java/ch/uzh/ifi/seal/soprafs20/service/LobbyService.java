package ch.uzh.ifi.seal.soprafs20.service;

import ch.uzh.ifi.seal.soprafs20.constant.*;
import ch.uzh.ifi.seal.soprafs20.entity.*;
import ch.uzh.ifi.seal.soprafs20.exceptions.ConflictException;
import ch.uzh.ifi.seal.soprafs20.exceptions.ForbiddenException;
import ch.uzh.ifi.seal.soprafs20.exceptions.NotFoundException;
import ch.uzh.ifi.seal.soprafs20.exceptions.SopraServiceException;
import ch.uzh.ifi.seal.soprafs20.repository.LobbyRepository;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.util.Pair;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.zip.DataFormatException;

@Service
@Transactional
public class LobbyService
{
    //private static final java.util.UUID UUID = ;
    private final Logger log = LoggerFactory.getLogger(LobbyService.class);
    private final LobbyRepository lobbyRepository;


    private PlayerService playerService;
    private UserService userService;
    private DeckService deckService;
    private CardService cardService;
    @Autowired
    private MysteryWordService mysteryWordService;

    @Autowired
    public LobbyService(@Qualifier("lobbyRepository") LobbyRepository lobbyRepository, UserService userService, PlayerService playerService, DeckService deckService, CardService cardService) {
        this.lobbyRepository = lobbyRepository;
        this.playerService = playerService;
        this.userService = userService;
        this.deckService = deckService;
        this.cardService = cardService;
    }

    /**
     * This method will create a lobby in the lobby repository
     *
     * @param lobbyInput
     * @return The created Lobby
     * @see Lobby
     */
    public Lobby createLobby(Lobby lobbyInput)
    {
        //checks if there is a lobby with the same name or with the same creator
        checkIfLobbyExists(lobbyInput);

        lobbyInput.setId(UUID.randomUUID().getMostSignificantBits() & Long.MAX_VALUE);
        lobbyInput.setLobbyStatus(LobbyStatus.WAITING);
        //TODO: function to generate a Deck Object and set it to lobbyInput

        // saves the given entity but data is only persisted in the database once flush() is called
        lobbyInput = lobbyRepository.save(lobbyInput);
        lobbyRepository.flush();

        log.debug("Created Information for Lobby: {}", lobbyInput);
        return lobbyInput;
    }

    /**
     * This method will get all lobbies from the lobby repository
     *
     * @return A List of all Lobbies
     * @see Lobby
     */
    public List<Lobby> getLobbies()
    {
        return lobbyRepository.findAll();
    }

    /**
     * This method will get a specific Lobby by ID from the Lobby Repository
     *
     * @return The requested Lobby
     * @see Lobby
     */
    public Lobby getLobbyById(Long id)
    {
        if (lobbyRepository.findByLobbyId(id) != null) {
            return lobbyRepository.findByLobbyId(id);
        }
        else  { throw new NotFoundException("The requested Lobby does not exist."); }
    }

    public boolean isUsernameInLobby(String username, Lobby lobby) {
        for (Player player : lobby.getPlayers()) {
            if (username.equals(player.getUsername())) {
                return true;
            }
        }
        return false;
    }
    /**
     * Main Goal: Will update the Lobby with the added Player
     * First it Checks the Token (Does it belong to any User? Does the token belong to the user you're trying to edit?)
     * Checks the User ID (Does it even exist?)
     * @param lobby
     * @return Lobby
     */
    public Lobby addPlayerToLobby(Lobby lobby, Player playerToAdd) {
                lobby.addPlayer(playerToAdd);
                lobby = lobbyRepository.save(lobby);
                lobbyRepository.flush();
                return lobby;
    }


    /**
     * This is a helper method that will check the uniqueness criteria of the username
     * defined in the User entity. The method will do nothing if the input is unique and throw an error otherwise.
     *
     * @param lobbyToBeCreated
     * @throws ConflictException
     * @see Lobby
     */
    private void checkIfLobbyExists(Lobby lobbyToBeCreated) {
        Lobby lobbyByLobbyName = lobbyRepository.findByLobbyName(lobbyToBeCreated.getLobbyName());
        Lobby lobbyByCreator = lobbyRepository.findByCreator(lobbyToBeCreated.getCreator());
        if (lobbyByCreator != null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Lobby Creator Conflict",
                    new ConflictException("The creator of the lobby is already host of another lobby." +
                            " Therefore, the lobby could not be created!"));
        }
        else if (lobbyByLobbyName != null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Lobby Name Conflict",
                    new ConflictException("The lobby name provided is not unique. Therefore, the lobby could not be created!"));
        }
    }

    /**
     *
     * Verify whether the user is lobby creator
     *
     * @param lobbyId - the Lobby to check the creator against
     * @param user - the user to verify against the lobby
     *
     * @return true if the user is the creator of the Lobby
     * */
    public boolean isUserLobbyCreator(Long lobbyId, User user) {
        Lobby lobby = this.getLobbyById(lobbyId);
        Player player = playerService.getPlayerById(user.getId());
        return player.equals(lobby.getCreator());
    }

    /**
     *
     * Verify whether the user is in this lobby
     *
     * @param lobbyId - the Lobby to check the Players against
     * @param user - the user to verify against the lobby
     *
     * @return true if the user is in this Lobby
     * */
    public Boolean isUserInLobby(User user, long lobbyId) {
        Lobby lobby = this.getLobbyById(lobbyId);
        Player player = playerService.getPlayerById(user.getId());
        return lobby.getPlayers().contains(player);

    }

    /**
     *
     * Verify whether the user is the Guesser of this Lobby
     *
     * @param lobbyId - the Lobby to check the Players against
     * @param user - the user to verify against the lobby
     *
     * @return true if the user is Guesser of this Lobby
     * */
    public Boolean isGuesserOfLobby(User user, long lobbyId) {
        if(isUserInLobby(user, lobbyId)) {
            Player player = playerService.getPlayerById(user.getId());
            return player.getRole() == PlayerRole.GUESSER;
        }
        else return false;
    }

    /**
     * this method is to remove playerId from the Lobby, only the creator can kick the player out
     *
     * @param lobbyId - the Lobby of the game
     * @param throwOutPlayerId - the player to kick out
     * @param creator - the creator of the lobby
     *
     * @return true if player could be successfully kickedout, otherwise false
     */
    public boolean kickOutPlayer(User creator, Long throwOutPlayerId, Long lobbyId) {
        boolean result = false;
        if(isUserLobbyCreator(lobbyId, creator)) {
            removePlayerFromLobby(lobbyId, throwOutPlayerId);
            result = true;
        }
        return result;
    }

    /**
     * this method is to remove playerId from the Lobby
     *
     * @param lobbyId - the Lobby of the game
     * @param playerId - the player to kick out
     */
    public void removePlayerFromLobby(Long lobbyId, Long playerId) {
        Player player = playerService.getPlayerById(playerId);
        Lobby lobby = this.getLobbyById(lobbyId);
        lobby.leave(player);
        lobby = lobbyRepository.save(lobby);
        playerService.deletePlayer(player);
        lobbyRepository.flush();
    }

    /**
     * this method is to   end the Lobby, only the creator can end the Lobby.
     *
     * @param lobbyId - the Lobby of the game
     * @param creator - the creator of the lobby
     *
     * @return true if Lobby could be successfully ended, otherwise false
     */
    public boolean endLobby(Long lobbyId, User creator ){
        boolean result = false;
        if(isUserLobbyCreator(lobbyId, creator)) {
            Lobby lobby = lobbyRepository.findByLobbyId(lobbyId);
            Set<Player> playersSet = lobby.getPlayers();
            lobbyRepository.delete(lobby);
            playerService.deletePlayers(playersSet);
            result = true;
        }
        return result;
    }

    /**
     * this method checks whether all Players are ready
     *
     * @param players - the Set of Players of the Lobby
     *
     * @return true if all Players are ready, false otherwise
     */
    public boolean areAllPlayersReady(Set<Player> players){
        for (Player player : players) {
            if (player.getStatus() != PlayerStatus.READY) {return false;}
        }
        return true;
    }

    /**
     * this method is to start the Lobby, only the creator can start the Lobby.
     *
     * @param lobbyId - the Lobby of the game
     *
     * @return true if Lobby could be successfully started, otherwise false
     */
    public boolean startGame(Long lobbyId){
        try {
            Lobby lobbyToBeStarted = lobbyRepository.findByLobbyId(lobbyId);
            lobbyToBeStarted.setLobbyStatus(LobbyStatus.RUNNING);
            nextRound(lobbyId);
            return true;
        }
        catch (Exception e) {return false;}
    }

    /**
     * This method is to stop the Lobby, the requesting Player leaves the Game.
     * In all cases the requesting Player is deleted from the Lobby and the Player Repository
     * Additionally, the game will be stopped as follows:
     * - The lobby contains at least 3 Human Players and the Game Mode is Bots
     *      --> The Game will resume with (#HumanPlayers-1 and #Bots+1)
     * - The Lobby contains at least 4 Human Players and the Game Mode is Humans
     *      --> The Game will resume with (#HumanPlayers-1)
     * - In ALL other cases, the Game Status is changed to STOPPED and the frontend directs back to the lobby
     *      --> The Lobby contains (#HumanPlayers-1) and is stopped
     *
     * @param lobbyId,player - the Lobby of the game and the leaving Player
     *
     * @return LobbyStatus
     * (RUNNING if the game can be continued, STOPPED if the game cannot be continued)
     */
    public Lobby stopGame(Long lobbyId, Player player) {
        LobbyStatus lobbyStatus = LobbyStatus.RUNNING;
        Lobby lobby = getLobbyById(lobbyId);
        Set<Player> players = lobby.getPlayers();
        int numberOfPlayers = players.size();
        GameModeStatus gameMode = lobby.getGameMode();
        if (gameMode == GameModeStatus.HUMANS && numberOfPlayers < 4) {
            lobbyStatus = LobbyStatus.STOPPED;
        }
        else if (gameMode == GameModeStatus.BOTS) {
            if (numberOfPlayers < 3) {
                lobbyStatus = LobbyStatus.STOPPED;
            }
            else {//TODO: Add one Bot as a replacement for the leaving Human Player
                //TODO: this.addPlayerToLobby(lobbyId, BOT.getId())
            }
        }
        //Update Points of the Player in the User Repository
        updateUserStatistics(lobbyId, player);

        //Set Lobby Status to what is defined within the function
        lobby.setLobbyStatus(lobbyStatus);
        //Remove the Player from the Lobby
        this.removePlayerFromLobby(lobbyId, player.getId());

        //If the response of the API call is "STOPPED" the frontend must redirect back to the lobby!
        //Otherwise the game can continue and the frontend should only show a message how the game will proceed.
        return lobby;
    }

    /**
     *
     * to get the list of Mystery word from the Lobby
     *
     * @param lobbyId - the current lobbyId
     * */
    public List<MysteryWord> getMysteryWordsFromLobby(Long lobbyId) {
        Lobby lobby = getLobbyById(lobbyId);
        Deck deck = lobby.getDeck();
        if(deck == null) {
            throw new SopraServiceException("Lobby has no Deck assigned!");
        }
        List<Card> cards = deck.getCards();

        if(!cards.isEmpty()) {
            Card card = cards.remove(0);
            card.setDrawn(true);
            deck.setActiveCard(card);
            deckService.save(deck);
            cardService.save(card);
            return card.getMysteryWords();
        } else {
            throw new SopraServiceException("No more cards to play!!");
        }
    }

    /**
     * updates the selected index of the currently played card
     * */
    public void updateSelectedMysteryWord(Long lobbyId, int selectedIndex) {
        Lobby lobby = getLobbyById(lobbyId);
        Deck deck = lobby.getDeck();
        if(deck == null) {
            throw new SopraServiceException("Lobby has no Deck assigned!");
        }
        Card activeCard = deck.getActiveCard();
        if(activeCard != null) {
            MysteryWord word = activeCard.getMysteryWords().get(selectedIndex-1);
            word.setStatus(MysteryWordStatus.IN_USE);
            word.setTimedrawn(new Date());
            mysteryWordService.save(word);
            lobby.setMysteryWord(word);
        }

    }

    /**
 * returns the active Mystery Word
 * */
public MysteryWord getActiveMysteryWord(Long lobbyId) {
    Lobby lobby = getLobbyById(lobbyId);
    Deck deck = lobby.getDeck();
    if(deck == null) {
        throw new SopraServiceException("Lobby has no Deck assigned!");
    }

    return lobby.getMysteryWord();
}

    /**
     * updates the Roles of all Players in the Lobby
     * */
    private Player updatePlayerRoles(Long lobbyId) {
        Lobby lobby = getLobbyById(lobbyId);
        ArrayList<Player> players = new ArrayList<>(lobby.getPlayers());
        int currentIndex = 0;
        Player nextGuesser;
        for (Player player : players) {
            if (player.getRole() == PlayerRole.GUESSER) {
                player.setRole(PlayerRole.CLUE_CREATOR);
                currentIndex = players.indexOf(player);
            }
        }
        if (players.get(currentIndex + 1) != null) {
            nextGuesser = players.get(currentIndex + 1);
        }
        else {
            nextGuesser = players.get(0);
        }
        return nextGuesser;
    }

    /**
     * updates the Points/Statistics of a Player to its User balance after a Game or when leaving a Game
     * */
    private void updateUserStatistics(Long lobbyId, Player player) {
        Lobby lobby = getLobbyById(lobbyId);
        //Update Points of the Player in the User Repository
        int currentPoints = player.getPoints();
        User user = userService.getUserByID(player.getId());
        userService.updatePoints(user, currentPoints);
    }

    /**
     * Reset the Roles of all Players in the Lobby to initial setup
     * */
    private Player resetPlayerRoles(Long lobbyId) {
        Lobby lobby = getLobbyById(lobbyId);
        Player creator = lobby.getCreator();
        Set<Player> players = lobby.getPlayers();
        for (Player player : players) {
            player.setRole(PlayerRole.CLUE_CREATOR);
        }
        creator.setRole(PlayerRole.GUESSER);
        return creator;
    }

    /**
     * Reset the Status of all Players in the Lobby to JOINED
     * */
    private void resetPlayerStatus(Long lobbyId) {
        Lobby lobby = getLobbyById(lobbyId);
        Set<Player> players = lobby.getPlayers();
        for (Player player : players) {
            player.setStatus(PlayerStatus.JOINED);
        }
    }

    /**
     * Reset the Statistics of all Players in the lobby to 0
     * */
    private void resetPlayerStatistics(Long lobbyId) {
        Lobby lobby = getLobbyById(lobbyId);
        Set<Player> players = lobby.getPlayers();
        for (Player player : players) {
            updateUserStatistics(lobbyId, player);
            player.setPoints(0);
        }
    }

    /**
     * Reset the Lobby to get back to it in order to:
     * play a new Game or to leave the lobby afterwards alternatively
     * */
    private void resetLobby(Long lobbyId) {
        Lobby lobby = getLobbyById(lobbyId);
        lobby.setLobbyStatus(LobbyStatus.WAITING);
        lobby.setRoundCount(0);
        lobby.setMysteryWord(null);
        resetPlayerRoles(lobbyId);
        resetPlayerStatistics(lobbyId);
        resetPlayerStatus(lobbyId);
    }

    /**
     * "starts" a new Round
     * return true when it was not Round 13 and false if the game is over.
     * */
    public boolean nextRound(Long lobbyId) {
        Lobby lobby = getLobbyById(lobbyId);
        if (lobby.getRoundCount() < 13) {
            lobby.nextRound();
            if (lobby.getRoundCount() != 0) {
                updatePlayerRoles(lobbyId);
            }
            return true;
        }
        else {
            resetLobby(lobbyId);
            return false;
        }
    }

    /**
     * collects statistics of the players individually and the total sum of the group
     * returns the stats in a json object
     * */
    public Statistics getStatistics(Long lobbyId) throws JSONException {
        Lobby lobby = getLobbyById(lobbyId);
        Statistics allStats = new Statistics();
        ArrayList<Pair<String, Integer>> playerStats = new ArrayList<>();
        int total = 0;
        Set<Player> players = lobby.getPlayers();
        for (Player player : players) {
            total += player.getPoints();
            playerStats.add(Pair.of(player.getUsername(), player.getPoints()));
        }
        allStats.setGroupStats(total);
        allStats.setPlayerStats(playerStats);
        return allStats;
    }
}
