package ch.uzh.ifi.seal.soprafs20.service;

import ch.uzh.ifi.seal.soprafs20.constant.LobbyStatus;
import ch.uzh.ifi.seal.soprafs20.entity.Lobby;
import ch.uzh.ifi.seal.soprafs20.entity.Player;
import ch.uzh.ifi.seal.soprafs20.entity.User;
import ch.uzh.ifi.seal.soprafs20.exceptions.ConflictException;
import ch.uzh.ifi.seal.soprafs20.repository.LobbyRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class LobbyService
{
    //private static final java.util.UUID UUID = ;
    private final Logger log = LoggerFactory.getLogger(LobbyService.class);
    private final LobbyRepository lobbyRepository;

    @Autowired
    private PlayerService playerService;

    @Autowired
    public LobbyService(LobbyRepository lobbyRepository) {
        this.lobbyRepository = lobbyRepository;
    }

    /**
     * This method will create a lobby in the lobby repository
     *
     * @param lobbyInput
     * @return The created Lobby
     * @see ch.uzh.ifi.seal.soprafs20.entity.Lobby
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
     * @see ch.uzh.ifi.seal.soprafs20.entity.Lobby
     */
    public List<Lobby> getLobbies()
    {
        return lobbyRepository.findAll();
    }

    /**
     * This method will get a specific Lobby by ID from the Lobby Repository
     *
     * @return The requested Lobby
     * @see ch.uzh.ifi.seal.soprafs20.entity.Lobby
     */
    public Lobby getLobbyById(Long id)
    {
        return lobbyRepository.findByLobbyId(id);
    }

    /**
     * Main Goal: Will update the Lobby with the added Player
     * First it Checks the Token (Does it belong to any User? Does the token belong to the user you're trying to edit?)
     * Checks the User ID (Does it even exist?)
     * @param lobbyId
     * @return Lobby
     */
    public Lobby addPlayerToLobby(Long lobbyId, Player playerToAdd) {
        Lobby lobby = lobbyRepository.findByLobbyId(lobbyId);
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
     * @see ch.uzh.ifi.seal.soprafs20.entity.Lobby
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
        return user.equals(lobby.getCreator());
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
    private void removePlayerFromLobby(Long lobbyId, Long playerId) {
        Player player = playerService.getPlayerById(playerId);
        Lobby lobby = this.getLobbyById(lobbyId);
        lobby.leave(player);
        lobby = lobbyRepository.save(lobby);
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
            lobbyRepository.delete(lobby);
            result = true;
        }
        return result;
    }
}
