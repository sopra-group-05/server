package ch.uzh.ifi.seal.soprafs20.controller;

import ch.uzh.ifi.seal.soprafs20.constant.GameModeStatus;
import ch.uzh.ifi.seal.soprafs20.constant.LobbyStatus;
import ch.uzh.ifi.seal.soprafs20.constant.PlayerRole;
import ch.uzh.ifi.seal.soprafs20.constant.UserStatus;
import ch.uzh.ifi.seal.soprafs20.entity.Lobby;
import ch.uzh.ifi.seal.soprafs20.entity.Player;
import ch.uzh.ifi.seal.soprafs20.entity.User;
import ch.uzh.ifi.seal.soprafs20.rest.dto.*;
import ch.uzh.ifi.seal.soprafs20.rest.mapper.DTOMapper;
import ch.uzh.ifi.seal.soprafs20.service.LobbyService;
import ch.uzh.ifi.seal.soprafs20.service.PlayerService;
import ch.uzh.ifi.seal.soprafs20.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
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
    private final UserService userService;
    private final PlayerService playerService;

    @Autowired
    LobbyController(UserService userService, LobbyService lobbyService, PlayerService playerService) {
        this.lobbyService = lobbyService;
        this.userService = userService;
        this.playerService = playerService;
    }

    /**
     * CREATE a new lobby
     * @param lobbyPostDTO
     * @return Status Code 201 and the created lobby
     */
    @PostMapping("/lobbies")
    @ResponseStatus(HttpStatus.CREATED)
    @ResponseBody
    public LobbyGetDTO createLobby(@RequestHeader(name = "Token", required = false) String token,
                                   @RequestBody LobbyPostDTO lobbyPostDTO) {
        //check Access rights via token
        User creator = userService.checkUserToken(token);
        Player player = playerService.convertUserToPlayer(creator, PlayerRole.GUESSER);

        // convert API lobby to internal representation
        Lobby lobbyInput = DTOMapper.INSTANCE.convertLobbyPostDTOtoEntity(lobbyPostDTO);
        lobbyInput.setCreator(creator);
        lobbyInput.addPlayer(player);

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
    public List<LobbyGetDTO> getAllLobbies() {
        // get all lobbies
        List<Lobby> lobbies = lobbyService.getLobbies();

        List<LobbyGetDTO> lobbyGetDTOs = new ArrayList<>();

        // return with status code 200
        // convert each lobby to the API representation
        for (Lobby lobby : lobbies) {
            lobbyGetDTOs.add(DTOMapper.INSTANCE.convertEntityToLobbyGetDTO(lobby));
        }
        return lobbyGetDTOs;
    }

    /**
     * GET a specific Lobby with all its Players by ID
     * @return Status Code 200, the requested Lobby that contains a List of all its Players
     */
    @GetMapping("/lobbies/{lobbyId}")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public LobbyGetDTO getLobbyById(@PathVariable long lobbyId) {
        // get the requested lobby; send message to the LobbyService
        Lobby lobby = lobbyService.getLobbyById(lobbyId);

        // return with status code 200
        return DTOMapper.INSTANCE.convertEntityToLobbyGetDTO(lobby);
    }

}
