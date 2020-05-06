package ch.uzh.ifi.seal.soprafs20.service;

import ch.uzh.ifi.seal.soprafs20.constant.*;
import ch.uzh.ifi.seal.soprafs20.entity.*;
import ch.uzh.ifi.seal.soprafs20.exceptions.ConflictException;
import ch.uzh.ifi.seal.soprafs20.exceptions.ForbiddenException;
import ch.uzh.ifi.seal.soprafs20.exceptions.NotFoundException;
import ch.uzh.ifi.seal.soprafs20.exceptions.SopraServiceException;
import ch.uzh.ifi.seal.soprafs20.repository.LobbyRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

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
    private GameService gameService;
    @Autowired
    private MysteryWordService mysteryWordService;

    @Autowired
    public LobbyService(@Qualifier("lobbyRepository") LobbyRepository lobbyRepository, UserService userService, PlayerService playerService, DeckService deckService, CardService cardService, GameService gameService) {
        this.lobbyRepository = lobbyRepository;
        this.playerService = playerService;
        this.userService = userService;
        this.deckService = deckService;
        this.cardService = cardService;
        this.gameService = gameService;
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
        //todo fix
        Player player = playerService.getPlayerById(playerId);
        Lobby lobby = this.getLobbyById(lobbyId);
        List<Clue> clues = player.getClues();
        for(Clue clue:clues){
            clue.setPlayer(null);
        }
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
            lobbyToBeStarted.setDeck(deckService.constructDeckForLanguage(lobbyToBeStarted.getLanguage()));
            lobbyToBeStarted.setGame(gameService.createNewGame(lobbyToBeStarted));
            lobbyToBeStarted.setLobbyStatus(LobbyStatus.RUNNING);
            this.setNewPlayersStatus(lobbyToBeStarted.getPlayers(),PlayerStatus.PICKING_NUMBER, PlayerStatus.WAITING_FOR_NUMBER);
            return true;
        }
        catch (Exception e) {return false;}
    }

    /**
     * Sets the status of ALL players according to the input params!
     * @param guesserStatus to what should the status of the player that guesses change? PlayerStatus Enum
     * @param cluesStatus to what should the status of the player that writes clues change? PlayerStatus Enum
     */
    public void setNewPlayersStatus(Set<Player> players, PlayerStatus guesserStatus, PlayerStatus cluesStatus) {
        for (Player player : players) {
            // set Roles of Players
            if (player.getRole() == PlayerRole.GUESSER) {
                player.setStatus(guesserStatus);
            } else {
                player.setStatus(cluesStatus);
            }
        }
        playerService.saveAll(players);
    }

    public void addBots(Long lobbyId){
        if(this.getLobbyById(lobbyId).getGameMode().equals(GameModeStatus.BOTS)) {
            Lobby lobby = this.getLobbyById(lobbyId);
            if (lobby.getPlayers().size() < 2) {
                throw new ForbiddenException("Not enough Players");
            }
            if (lobby.getGameMode().equals(GameModeStatus.BOTS)) {
                while (lobby.getPlayers().size() < 4) {
                    if (lobby.getNumBots() == 0) {
                        this.addPlayerToLobby(lobby, playerService.createBotPlayer(PlayerType.FRIENDLYBOT));
                        lobby.setNumBots(1);
                    }
                    else {
                        this.addPlayerToLobby(lobby, playerService.createBotPlayer(PlayerType.MALICIOUSBOT));
                        lobby.setNumBots(2);
                    }
                }
            }
        }
    }

    /**
     * Checks if there are enough Human Players in the lobby to start the game.
     * Has to be run before Bots are added.
     */
    public void lobbyHasEnoughPlayers(long lobbyId) {
        Lobby lobby = this.getLobbyById(lobbyId);
        if (lobby.getGameMode().equals(GameModeStatus.BOTS)) {
            // game mode with bots: has to be 2 players minimum (+bots)
            if (lobby.getPlayers().size() < 2) {
                throw new ForbiddenException("You need at least two players to start a game with bots.");
            }
        } else {
            // game mode without bots: has to be 3 players minimum
            if (lobby.getPlayers().size() < 3) {
                throw new ForbiddenException("You need at least thre players to start a game.");
            }
        }
    }

    /**
     * Set status of this player according to input params
     * If all other players with already set their status to the new one, update the Guesser to his next status
     */

    public void setNewStatusToPlayer(Set<Player> players, Player thisPlayer, PlayerStatus guesserStatus, PlayerStatus cluesStatus) {
        // change status of individual player (thisPlayer)
        thisPlayer.setStatus(cluesStatus);

        // count how many players have this new status
        // also change status of Bots to new Status!
        int playersSize = players.size() - 1; // without the Guesser
        for (Player player : players) {
            if(player.getPlayerType() != PlayerType.HUMAN) {
                player.setStatus(cluesStatus);
            }
            if(player.getStatus().equals(cluesStatus)) {
                playersSize = playersSize-1; // remove 1 for every player
            }
        }

        // change Status of Guesser when all other players changed to new status
        if (playersSize == 0) {
            for (Player player : players) {
                // set Roles of Players
                if (player.getRole() == PlayerRole.GUESSER) {
                    player.setStatus(guesserStatus);
                }
            }
        }

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
            if(this.getLobbyById(lobbyId).getNumBots() == 0) {
                this.addPlayerToLobby(lobby, playerService.createBotPlayer(PlayerType.FRIENDLYBOT));
                this.getLobbyById(lobbyId).setNumBots(this.getLobbyById(lobbyId).getNumBots()+1);
            } else if (this.getLobbyById(lobbyId).getNumBots() == 1) {
                this.addPlayerToLobby(lobby, playerService.createBotPlayer(PlayerType.MALICIOUSBOT));
                this.getLobbyById(lobbyId).setNumBots(this.getLobbyById(lobbyId).getNumBots()+1);
            } else{
                lobbyStatus = LobbyStatus.STOPPED;
            }
        }
        //Update Points of the Player in the User Repository
        int currentPoints = player.getPoints();
        User user = userService.getUserByID(player.getId());
        userService.updatePoints(user, currentPoints);

        //Set Lobby Status to what is defined within the function
        lobby.setLobbyStatus(lobbyStatus);
        //Remove the Player from the Lobby
        this.removePlayerFromLobby(lobbyId, player.getId());

        //If the response of the API call is "STOPPED" the frontend must redirect back to the lobby!
        //Otherwise the game can continue and the frontend should only show a message how the game will proceed.
        return lobby;
    }

    /**
     *todo: check if needed
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
            Card card = cards.get(0);
            //cards.remove(0);
            //card.setDrawn(true);
            deck.setActiveCard(card);
            deckService.save(deck);
            //cardService.save(card);
            return card.getMysteryWords();
        } else {
            throw new SopraServiceException("No more cards to play!!");
        }
    }

    /**
     * updates the selected index of the currently played card
     * */
    public void updateSelectedMysteryWord(Long lobbyId, int selectedNumber) {
        Lobby lobby = getLobbyById(lobbyId);
        Deck deck = lobby.getDeck();
        if(deck == null) {
            throw new SopraServiceException("Lobby has no Deck assigned!");
        }
        this.setNewPlayersStatus(lobby.getPlayers(),PlayerStatus.WAITING_FOR_CLUES, PlayerStatus.WRITING_CLUES);
        Card activeCard = deck.getActiveCard();
        if(activeCard != null) {
            for(MysteryWord word : activeCard.getMysteryWords()) {
                if(word.getNumber() == selectedNumber) {
                    word.setStatus(MysteryWordStatus.IN_USE);
                    word.setTimedrawn(new Date());
                    mysteryWordService.save(word);
                }
            }
        }

    }

    public void removeFromLobbyAndDeletePlayer(User toDeleteUser){
        if (playerService.doesPlayerWithTokenExist(toDeleteUser.getToken())) {
            Player player = playerService.getPlayerByToken(toDeleteUser.getToken());
            Lobby lobbyToRemovePlayer = null;
            List<Lobby> lobbies= this.getLobbies();
            for(Lobby lobby:lobbies){
                Set<Player> players = lobby.getPlayers();
                if(players.contains(player)){
                    lobbyToRemovePlayer = lobby;
                    break;
                }
            }
            if(lobbyToRemovePlayer != null) {
                this.removePlayerFromLobby(lobbyToRemovePlayer.getId(), player.getId());
                playerService.deletePlayer(player);
            }
        }
    }

    public void nextRound(long lobbyId, String token){
        Lobby lobby = this.getLobbyById(lobbyId);
        Game game = lobby.getGame();
        List<MysteryWord> mysteryWords= lobby.getDeck().getActiveCard().getMysteryWords();
        for(MysteryWord mysteryWord:mysteryWords){
            mysteryWord.setStatus(MysteryWordStatus.NOT_USED);
        }
        List<Clue> clues = game.getClues();
        for(Clue clue:clues){
            clue.setClueStatus(ClueStatus.INACTIVE);
        }
        Deck deck = lobby.getDeck();
        List<Card> cards = deck.getCards();
        Card cardToRemove = cards.remove(0);
        //cardService.delete(cardToRemove);
        game.setComparingGuessCounter(0);
        Card card = cards.get(0);
        if(card.equals(null)){
            this.endGame(lobby);
        }
        deck.setActiveCard(card);
        deckService.save(deck); 
        game.setActiveGuess("");//todo check if needed
        this.setNewRoleOfPlayers(lobby);
        this.setNewPlayersStatus(lobby.getPlayers(), PlayerStatus.PICKING_NUMBER, PlayerStatus.WAITING_FOR_NUMBER);
        Set<Player> allPlayers = lobby.getPlayers();
        for (Player player:allPlayers){
            if(!player.getPlayerType().equals(PlayerType.HUMAN)){
                player.setStatus(PlayerStatus.READY);
            }
        }
    }

    /**
     * This method will assign the Role GUESSER to a new Player (only humans)
     * @param lobby
     */
    public void setNewRoleOfPlayers(Lobby lobby) {
        int oldPosition = 0; //position of oldGuesser
        for (Player player : lobby.getPlayers()) {
            if (player.getRole() == PlayerRole.GUESSER){
                // set old Guesser to Clue Creator
                player.setRole(PlayerRole.CLUE_CREATOR);
                break;
            }
            if (player.getPlayerType() == PlayerType.HUMAN) {
                // count at which position the older Guesser was
                oldPosition = oldPosition + 1;
            }
        }

        int size = this.getNumberOfHumanPlayersInLobby(lobby);
        int newPosition = (oldPosition + 1) % size; // calculate new position of next player
        int count = 0;

        for (Player player : lobby.getPlayers()) {
            if (player.getPlayerType() == PlayerType.HUMAN) {
                if (count == newPosition) {
                    player.setRole(PlayerRole.GUESSER);
                    break;
                }
                count = count + 1;
            }
        }
    }

    private int getNumberOfHumanPlayersInLobby(Lobby lobby) {
        int num = 0;
        for (Player player : lobby.getPlayers()) {
            if (player.getPlayerType() == PlayerType.HUMAN) {
                num = num + 1;
            }
        }
        return num;
    }

    /**
     * This method will invite the user to given lobby
     *
     * @param lobby lobby user is invited to
     * @param user invited user
     *
     */
    public void inviteUserToLobby(User user, Lobby lobby){
        Player player = playerService.convertUserToPlayer(user, PlayerRole.CLUE_CREATOR);
        addPlayerToLobby(lobby, player);
        // add ranking for player
        gameService.addStats(player.getId(),lobby.getId());
    }

    public void endGame(Lobby lobby){
        Set<Player> players = lobby.getPlayers();
        for (Player player:players){
            player.setStatus(PlayerStatus.FINISHED);
        }
        lobby.setLobbyStatus(LobbyStatus.STOPPED);
    }
}
