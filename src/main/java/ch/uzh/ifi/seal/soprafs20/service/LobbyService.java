package ch.uzh.ifi.seal.soprafs20.service;

import ch.uzh.ifi.seal.soprafs20.constant.LobbyStatus;
import ch.uzh.ifi.seal.soprafs20.entity.Lobby;
import ch.uzh.ifi.seal.soprafs20.entity.Player;
import ch.uzh.ifi.seal.soprafs20.entity.User;
import ch.uzh.ifi.seal.soprafs20.exceptions.ConflictException;
import ch.uzh.ifi.seal.soprafs20.exceptions.ForbiddenException;
import ch.uzh.ifi.seal.soprafs20.exceptions.NotFoundException;
import ch.uzh.ifi.seal.soprafs20.repository.LobbyRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;
import java.util.zip.DataFormatException;

@Service
@Transactional
public class LobbyService
{
    //private static final java.util.UUID UUID = ;
    private final Logger log = LoggerFactory.getLogger(LobbyService.class);
    private final LobbyRepository lobbyRepository;

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
        lobbyInput.setLobbyStatus(LobbyStatus.RUNNING);
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
        if (lobbyRepository.findByLobbyId(id) != null) {
            return lobbyRepository.findByLobbyId(id);
        }
        else  { throw new NotFoundException("The requested Lobby does not exist."); }
    }

    public boolean isUsernameInLobby(String username, Lobby lobby) {
        for (Player player : lobby.getPlayers()) {
            if (username == player.getUsername()) {
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
}
