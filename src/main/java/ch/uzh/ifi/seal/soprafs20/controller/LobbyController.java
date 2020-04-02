package ch.uzh.ifi.seal.soprafs20.controller;

import ch.uzh.ifi.seal.soprafs20.constant.GameModeStatus;
import ch.uzh.ifi.seal.soprafs20.constant.LobbyStatus;
import ch.uzh.ifi.seal.soprafs20.entity.Lobby;
import ch.uzh.ifi.seal.soprafs20.entity.User;
import ch.uzh.ifi.seal.soprafs20.rest.dto.*;
import ch.uzh.ifi.seal.soprafs20.rest.mapper.DTOMapper;
import ch.uzh.ifi.seal.soprafs20.service.LobbyService;
import ch.uzh.ifi.seal.soprafs20.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

/**
 * User Controller
 * This class is responsible for handling all REST request that are related to the user.
 * The controller will receive the request and delegate the execution to the UserService and finally return the result.
 */
@RestController
public class LobbyController {

    private final LobbyService lobbyService;

    LobbyController(LobbyService lobbyService) {
        this.lobbyService = lobbyService;
    }

    /**
     * CREATE a new lobby
     * @param lobbyPostDTO
     * @return Status Code 201 and the created lobby
     */
    @PostMapping("/lobbies")
    @ResponseStatus(HttpStatus.CREATED)
    @ResponseBody
    public LobbyGetDTO createLobby(@RequestBody LobbyPostDTO lobbyPostDTO) {
        // convert API lobby to internal representation
        Lobby lobbyInput = DTOMapper.INSTANCE.convertLobbyPostDTOtoEntity(lobbyPostDTO);

/*        if (lobbyPostDTO.getGameMode() == 0) {
            lobbyInput.setGameMode(GameModeStatus.HUMANS);
        }
        else {lobbyInput.setGameMode(GameModeStatus.BOTS);}*/

        // create lobby
        Lobby createdLobby = lobbyService.createLobby(lobbyInput);

        // convert internal representation of lobby back to API
        // return with status code 201 created the Location and user object
        return DTOMapper.INSTANCE.convertEntityToLobbyGetDTO(createdLobby);
    }

    /**
     * GET all Lobbies
     * @return Status Code 200 and a list of all lobbies
     */
    @GetMapping("/lobbies")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public LobbyGetDTO getAllLobbies() {
        // convert API lobby to internal representation
        Lobby lobbyInput = null;

        // create lobby
        Lobby createdLobby = lobbyService.createLobby(lobbyInput);

        // convert internal representation of lobby back to API
        LobbyGetDTO lobbyGetDTO = DTOMapper.INSTANCE.convertEntityToLobbyGetDTO(createdLobby);

        // return with status code 201 created the Location and user object
        return lobbyGetDTO;
    }
}
