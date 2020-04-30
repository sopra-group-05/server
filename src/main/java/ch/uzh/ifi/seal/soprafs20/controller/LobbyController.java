package ch.uzh.ifi.seal.soprafs20.controller;

import ch.uzh.ifi.seal.soprafs20.constant.*;
import ch.uzh.ifi.seal.soprafs20.entity.Clue;
import ch.uzh.ifi.seal.soprafs20.entity.Lobby;
import ch.uzh.ifi.seal.soprafs20.entity.MysteryWord;
import ch.uzh.ifi.seal.soprafs20.entity.Player;
import ch.uzh.ifi.seal.soprafs20.entity.User;
import ch.uzh.ifi.seal.soprafs20.exceptions.ConflictException;
import ch.uzh.ifi.seal.soprafs20.exceptions.ForbiddenException;
import ch.uzh.ifi.seal.soprafs20.exceptions.UnauthorizedException;
import ch.uzh.ifi.seal.soprafs20.rest.dto.*;
import ch.uzh.ifi.seal.soprafs20.rest.mapper.DTOMapper;
import ch.uzh.ifi.seal.soprafs20.service.ClueService;
import ch.uzh.ifi.seal.soprafs20.service.LobbyService;
import ch.uzh.ifi.seal.soprafs20.service.PlayerService;
import ch.uzh.ifi.seal.soprafs20.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

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
    private final ClueService clueService;

    @Autowired
    LobbyController(UserService userService, LobbyService lobbyService, PlayerService playerService, ClueService clueService) {
        this.lobbyService = lobbyService;
        this.userService = userService;
        this.playerService = playerService;
        this.clueService = clueService;
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

        //check if User is already Player in another Lobby/Game
        Boolean isPlayerToJoin = playerService.checkPlayerToken(token);
        //check Access rights via token
        User creator = userService.checkUserToken(token);

        if (isPlayerToJoin) {
            Player player = playerService.convertUserToPlayer(creator, PlayerRole.GUESSER);
            // convert API lobby to internal representation
            Lobby lobbyInput = DTOMapper.INSTANCE.convertLobbyPostDTOtoEntity(lobbyPostDTO);
            lobbyInput.setCreator(player);
            lobbyInput.addPlayer(player);
            // create lobby
            Lobby createdLobby = lobbyService.createLobby(lobbyInput);
            // convert internal representation of lobby back to API
            // return with status code 201 created the Location and user object
            return DTOMapper.INSTANCE.convertEntityToLobbyGetDTO(createdLobby);
        }
        else throw new ConflictException("You are already in a Lobby or in a Game.");
    }

    /**
     * GET all Lobbies
     * @return Status Code 200 and a list of all lobbies
     */
    @GetMapping("/lobbies")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public List<LobbyGetDTO> getAllLobbies(@RequestHeader(name = "Token", required = false) String token) {
        //check Access rights via token
        userService.checkUserToken(token);

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
    public LobbyGetDTO getLobbyById(@RequestHeader(name = "Token", required = false) String token,
                                    @PathVariable long lobbyId) {
        //check Access rights via token
        User user = userService.checkUserToken(token);
        // get the requested lobby; send message to the LobbyService
        Lobby lobby = lobbyService.getLobbyById(lobbyId);
            if (lobbyService.isUsernameInLobby(user.getUsername(), lobby)) {
                // return with status code 200
                return DTOMapper.INSTANCE.convertEntityToLobbyGetDTO(lobby);
            }
            else throw new ForbiddenException(
                    "You are not in the requested Lobby. Therefore access is Forbidden.");
    }

    /**
     * PUT Update a specific Lobby by a Player joining the lobby
     * @param lobbyId
     * @return Status Code 204
     */
    @PutMapping("/lobbies/{lobbyId}/join")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @ResponseBody
    public void joinLobbyById(@PathVariable long lobbyId,
                                       @RequestHeader(name = "Token", required = false) String token) {
        //check if User is already Player in another Lobby/Game
        Boolean isPlayerToJoin = playerService.checkPlayerToken(token);
        //check Access rights via token
        User userToJoin = userService.checkUserToken(token);
        Lobby lobby = lobbyService.getLobbyById(lobbyId);

        if (isPlayerToJoin) {
            String forbiddenExceptionMsg = "The requested Lobby is already " + lobby.getLobbyStatus();
            if (lobby.getLobbyStatus() != LobbyStatus.RUNNING) {
                // convert the User to Player
                Player player = playerService.convertUserToPlayer(userToJoin, PlayerRole.CLUE_CREATOR);
                // get the requested Lobby and add the Player to the Lobby
                lobbyService.addPlayerToLobby(lobby, player);
                // return with status code 204
            }
            else throw new ForbiddenException(forbiddenExceptionMsg);
        }
        else throw new ConflictException("You are already in a Lobby or in a Game.");
    }

    @PutMapping("/lobbies/{lobbyId}/leave")
    @ResponseBody
    public ResponseEntity<?> leaveLobby(@PathVariable long lobbyId,
                                           @RequestHeader(name = "Token", required = false) String token) {
        //check Access rights via token
        User lobbyCreator = userService.checkUserToken(token);

        //verify if the throwing out player is the lobby creator
        lobbyService.removePlayerFromLobby(lobbyId, lobbyCreator.getId());
        return new ResponseEntity<>("", HttpStatus.NO_CONTENT);


    }

    @PutMapping("/lobbies/{lobbyId}/terminate")
    @ResponseBody
    public ResponseEntity<?> stopLobbyById(@PathVariable long lobbyId,
                                           @RequestHeader(name = "Token", required = false) String token) {
        //check Access rights via token
        User lobbyCreator = userService.checkUserToken(token);

        //verify if the throwing out player is the lobby creator
        if(lobbyService.endLobby(lobbyId , lobbyCreator)) {
            return new ResponseEntity<>("", HttpStatus.OK);
        } else {
            return new ResponseEntity<>("Unauthorized (invalid Token)", HttpStatus.UNAUTHORIZED);
        }
    }

    @PutMapping("/lobbies/{lobbyId}/ready")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @ResponseBody
    public Boolean readyLobbyById(@PathVariable long lobbyId,
                                           @RequestHeader(name = "Token", required = false) String token) {
        //check Access rights via token
        User user = userService.checkUserToken(token);

        //check whether User is in this Lobby
        Boolean isInThisLobby = lobbyService.isUserInLobby(user, lobbyId);

        Player player = playerService.getPlayerById(user.getId());

        if(!isInThisLobby) {
            throw new ForbiddenException("The user is not in this Lobby.");
        }
        playerService.setPlayerReady(player);
        return true;
    }

    @PutMapping("/lobbies/{lobbyId}/kick/{userID}")
    @ResponseBody
    public ResponseEntity<?> kickPlayerOut(@PathVariable long lobbyId, @PathVariable long userID,
                                           @RequestHeader(name = "Token", required = false) String token) {
        //check Access rights via token
        User lobbyCreator = userService.checkUserToken(token);

        //verify if the throwing out player is the lobby creator
        if(lobbyService.kickOutPlayer(lobbyCreator, userID, lobbyId)) {
            return new ResponseEntity<>("", HttpStatus.NO_CONTENT);
        } else {
            return new ResponseEntity<>("Forbidden: User is not creator of lobby or is not even in the lobby", HttpStatus.FORBIDDEN);
        }

    }


    /**
     * PUT Start the game
     * @param lobbyId
     * @return Status Code 204
     */
    @PutMapping("/lobbies/{lobbyId}/start")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @ResponseBody
    public void startLobbyById(@PathVariable long lobbyId,
                               @RequestHeader(name = "Token", required = false) String token) {
        //check if Player is the Host of the lobby and therefore allowed to start the game
        Boolean isPlayerAllowedToStart = playerService.isAllowedToStart(token);
        //check Access rights via token
        userService.checkUserToken(token);
        Lobby lobby = lobbyService.getLobbyById(lobbyId);

        if (isPlayerAllowedToStart) {
            String forbiddenExceptionMsg = "Not all players in the Lobby are ready yet.";
            Set<Player> players = lobby.getPlayers();

            boolean areAllPlayersReady = lobbyService.areAllPlayersReady(players);
            if (areAllPlayersReady) {
                lobbyService.startGame(lobbyId);
            }
            else throw new ForbiddenException(forbiddenExceptionMsg);
        }
        else throw new ForbiddenException("You are no Host of the Lobby or not even in the lobby.");
    }

    /**
     * PUT Stop the Game (A Player leaves the Game)
     * @param lobbyId
     * @return Status Code 204
     */
    @PutMapping("/lobbies/{lobbyId}/stop")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public LobbyGetDTO stopLobbyById2(@PathVariable long lobbyId,
                               @RequestHeader(name = "Token", required = false) String token) {
        //check Access rights via token
        User userToJoin = userService.checkUserToken(token);
        lobbyService.getLobbyById(lobbyId);
        Player player = playerService.getPlayerById(userToJoin.getId());

        Lobby lobby = lobbyService.stopGame(lobbyId, player);

        return DTOMapper.INSTANCE.convertEntityToLobbyGetDTO(lobby);
    }


    /**
     * API to get list of mystery words for the current game play
     *
     * */
    @GetMapping("/lobbies/{lobbyId}/card")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public List<MysteryWordGetDto> getMysteryWords(@PathVariable long lobbyId,
                                             @RequestHeader(name = "Token", required = false) String token) {
        //check Access rights via token
        User user = userService.checkUserToken(token);
        Lobby lobby = lobbyService.getLobbyById(lobbyId);
        if (!lobbyService.isUsernameInLobby(user.getUsername(), lobby) || PlayerRole.GUESSER == playerService.getPlayerById(user.getId()).getRole()) {
            throw new ForbiddenException(
                "You are not in this lobby or is the active player");
        }

        List<MysteryWord> mysteryWordList = lobbyService.getMysteryWordsFromLobby(lobbyId);
        List<MysteryWordGetDto> mysteryWordGetDtoList = new ArrayList<>();
        for(MysteryWord mysteryWord : mysteryWordList) {
            mysteryWordGetDtoList.add(DTOMapper.INSTANCE.convertMysteryWordToMysteryWordGetDTO(mysteryWord));
        }
        return mysteryWordGetDtoList;
    }

    /**
     * API to post the selected number from the guesser from the current game play
     *
     * */
    @PostMapping("/lobbies/{lobbyId}/number")
    @ResponseBody
    public ResponseEntity<?> updateSelectedMysteryWord(@PathVariable long lobbyId,
                                          @RequestBody int selectedMysteryWordIndex,
                                             @RequestHeader(name = "Token", required = false) String token) {
        //check Access rights via token
        User user = userService.checkUserToken(token);
        lobbyService.getLobbyById(lobbyId);
        if(selectedMysteryWordIndex < 1 || selectedMysteryWordIndex > 5) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
        lobbyService.updateSelectedMysteryWord(lobbyId, selectedMysteryWordIndex);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(null);
    }


    @PostMapping("/lobbies/{lobbyId}/clues")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public void addClue(@PathVariable long lobbyId,
                                     @RequestHeader(name = "Token", required = false) String token, @RequestBody CluePostDTO cluePostDTO){
        Clue clue = DTOMapper.INSTANCE.convertCluePOSTDTOToEntity(cluePostDTO);

        Lobby lobby = lobbyService.getLobbyById(lobbyId);
        Player thisPlayer = playerService.getPlayerByToken(token);
        lobbyService.setNewStatusToPlayer(lobby.getPlayers(), thisPlayer, PlayerStatus.WAITING_FOR_REVIEW, PlayerStatus.REVIEWING_CLUES);

        clueService.addClue(clue, lobbyId, token);
    }

    @GetMapping("/lobbies/{lobbyId}/clues")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public List<ClueGetDTO> getClues(@PathVariable long lobbyId,
                                   @RequestHeader(name = "Token", required = false) String token, @RequestBody CluePostDTO cluePostDTO){
        List<Clue> clues= clueService.getCluesForChecking(lobbyId, token);
        List<ClueGetDTO> clueGetDTOs= new ArrayList<ClueGetDTO>();
        for (Clue clue:clues){
            clueGetDTOs.add(DTOMapper.INSTANCE.convertClueToClueGetDTO(clue));
        }
        return clueGetDTOs;
    }

    @PutMapping("/lobbies/{lobbyId}/clues/{clueId}")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public void flagClue(@PathVariable long lobbyId, @PathVariable long clueId, @RequestHeader(name = "Token", required = false) String token){
        clueService.flagClue(clueId, token, lobbyId);
    }

    @PutMapping("/lobbies/{lobbyId}/clues/flag")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public void flagMultipleClue(@PathVariable long lobbyId, @RequestHeader(name = "Token", required = false) String token){

        // todo add @RequestBody with a List of all Clues that should be flagged.
        // todo go trough list of Clue IDs and flag all of them => CluesToFlag
        //clueService.flagClue(clueId, token, lobbyId);

        // todo remove and put at right place, status of players HAVE to be updated somewhere...
        Lobby lobby = lobbyService.getLobbyById(lobbyId);
        Player player = playerService.getPlayerById(userService.checkUserToken(token).getId());
        lobbyService.setNewStatusToPlayer(lobby.getPlayers(), player, PlayerStatus.END_OF_TURN, PlayerStatus.END_OF_TURN);
    }


    @PostMapping("/lobbies/{lobbyId}/guess")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @ResponseBody
    public ResponseEntity<?> guessMysteryWord(@RequestHeader(name = "Token", required = false) String token,
                                              @PathVariable long lobbyId,
                                              @RequestBody String guess) {

        //check Access rights via token
        User user = userService.checkUserToken(token);

        //check whether User is in this Lobby and has the role of the Guesser
        Boolean isGuesserOfLobby = lobbyService.isGuesserOfLobby(user, lobbyId);

        if (!isGuesserOfLobby) {
            throw new UnauthorizedException("User is not the current Guesser of the Lobby.");
        }

        Lobby lobby = lobbyService.getLobbyById(lobbyId);

        // todo the guess has to be saved and compared to the mystery word
        // todo add points if correct (distribute them)
        // todo move arround roles of players? (Guesser vs Clue maker etc)
        // todo end of game what happens??

        // set Status of all Players to End of Turn.
        lobbyService.setNewPlayersStatus(lobby.getPlayers(), PlayerStatus.END_OF_TURN, PlayerStatus.END_OF_TURN);

        return ResponseEntity.noContent().build();
    }
}
