package ch.uzh.ifi.seal.soprafs20.controller;

import ch.uzh.ifi.seal.soprafs20.constant.*;
import ch.uzh.ifi.seal.soprafs20.entity.*;
import ch.uzh.ifi.seal.soprafs20.exceptions.*;
import static java.lang.Math.toIntExact;
import ch.uzh.ifi.seal.soprafs20.rest.dto.LobbyPostDTO;
//import ch.uzh.ifi.seal.soprafs20.rest.dto.LobbyPutDTO;
import ch.uzh.ifi.seal.soprafs20.service.ClueService;
import ch.uzh.ifi.seal.soprafs20.service.GameService;
import ch.uzh.ifi.seal.soprafs20.service.LobbyService;
import ch.uzh.ifi.seal.soprafs20.service.PlayerService;
import ch.uzh.ifi.seal.soprafs20.service.UserService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mapstruct.Mapping;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * LobbyControllerTest
 * This is a WebMvcTest which allows to test the LobbyController i.e. GET/POST/PUT request without actually sending them over the network.
 * This tests if the LobbyController works.
 */
@WebMvcTest(LobbyController.class)
public class LobbyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private LobbyService lobbyService;
    @MockBean
    private UserService userService;
    @MockBean
    private PlayerService playerService;
    @MockBean
    private ClueService clueService;
    @MockBean
    private GameService gameService;

    /**
     * Tests post /lobbies
     * Valid Input, returns lobby data
     */
    @Test
    public void createLobby_validInput_lobbyCreated() throws Exception {
        // given
        Lobby lobby = new Lobby();
        lobby.setId(1L);
        lobby.setLobbyName("testName");
        Deck testDeck = new Deck();
        lobby.setDeck(testDeck);
        User testUser = new User();
        testUser.setId(1L);
        Player testPlayer = new Player(testUser);
        testPlayer.setId(1L);
        lobby.addPlayer(testPlayer);
        lobby.setGameMode(GameModeStatus.HUMANS);
        lobby.setCreator(testPlayer);

        LobbyPostDTO lobbyPostDTO = new LobbyPostDTO();
        lobbyPostDTO.setLobbyName("testName");
        lobbyPostDTO.setGameMode(0);
        lobbyPostDTO.setLanguage("EN");

        given(playerService.checkPlayerToken(Mockito.any())).willReturn(true);
        given(userService.checkUserToken(Mockito.any())).willReturn(testUser);
        given(lobbyService.createLobby(Mockito.any())).willReturn(lobby);
        given(playerService.convertUserToPlayer(Mockito.any(),Mockito.any())).willReturn(testPlayer);
        Mockito.doNothing().when(gameService).addStats(Mockito.any(),Mockito.any());


        // when/then -> do the request + validate the result
        MockHttpServletRequestBuilder postRequest = post("/lobbies")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(lobbyPostDTO));

        // then
        mockMvc.perform(postRequest)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(lobby.getId().intValue())))
                .andExpect(jsonPath("$.lobbyName", is(lobby.getLobbyName())))
                .andExpect(jsonPath("$.players[0].id", is(toIntExact(lobby.getPlayers().iterator().next().getId()))))
                .andExpect(jsonPath("$.gameMode", is(lobby.getGameMode().toString())))
                .andExpect(status().isCreated());
    }

    /**
     * Tests post /lobbies
     * Valid input, but not allowed to view page (no Token / wrong token)
     */
    @Test
    public void createLobby_validInput_wrongToken_exceptionReturned() throws Exception {
        // given
        String exceptionMsg = "You are not allowed to access this page";

        LobbyPostDTO lobbyPostDTO = new LobbyPostDTO();
        lobbyPostDTO.setLobbyName("testName");
        lobbyPostDTO.setGameMode(0);
        lobbyPostDTO.setLanguage("EN");

        given(playerService.checkPlayerToken(Mockito.any())).willReturn(true);
        given(lobbyService.createLobby(Mockito.any())).willThrow(new UnauthorizedException(exceptionMsg));

        // when/then -> do the request + validate the result
        MockHttpServletRequestBuilder postRequest = post("/lobbies")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(lobbyPostDTO));

        // then
        mockMvc.perform(postRequest)
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$", is(exceptionMsg)))
                .andDo(print());
    }

    /**
     * Tests post /lobbies
     * Invalid Input, Conflict, throws error with Status Code 409
     */
    @Test
    public void createLobby_invalidInput_exceptionReturned() throws Exception {
        // given
        String exceptionMsg = "Conflict: same player has another lobby open, " +
                "or there already is a lobby with the same name";

        LobbyPostDTO lobbyPostDTO = new LobbyPostDTO();
        lobbyPostDTO.setLobbyName("testName");
        lobbyPostDTO.setGameMode(0);
        lobbyPostDTO.setLanguage("EN");

        given(playerService.checkPlayerToken(Mockito.any())).willReturn(true);
        given(lobbyService.createLobby(Mockito.any())).willThrow(new ConflictException(exceptionMsg));

        // when/then -> do the request + validate the result
        MockHttpServletRequestBuilder postRequest = post("/lobbies")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(lobbyPostDTO));

        // then
        mockMvc.perform(postRequest)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$", is(exceptionMsg)))
                .andDo(print());
    }

    @Test
    public void getAllLobbies_lobbiesReturned() throws Exception {
        // given
        Lobby lobby = new Lobby();
        lobby.setId(1L);
        lobby.setLobbyName("testName");
        Deck testDeck = new Deck();
        lobby.setDeck(testDeck);
        User testUser = new User();
        testUser.setId(1L);
        Player testPlayer = new Player(testUser);
        testPlayer.setRole(PlayerRole.GUESSER);
        lobby.addPlayer(testPlayer);
        lobby.setGameMode(GameModeStatus.HUMANS);
        lobby.setCreator(testPlayer);
        lobby.setLanguage(Language.DE);

        List<Lobby> allLobbies = Collections.singletonList(lobby);

        given(lobbyService.getLobbies()).willReturn(allLobbies);

        // when/then -> do the request + validate the result
        MockHttpServletRequestBuilder getRequest = get("/lobbies")
                .contentType(MediaType.APPLICATION_JSON);

        // then
        mockMvc.perform(getRequest)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is(lobby.getId().intValue())))
                .andExpect(jsonPath("$[0].lobbyName", is(lobby.getLobbyName())))
                .andExpect(jsonPath("$[0].players[0].id", is(toIntExact(lobby.getPlayers().iterator().next().getId()))))
                .andExpect(jsonPath("$[0].players[0].role", is((lobby.getPlayers().iterator().next().getRole().name()))))
                .andExpect(jsonPath("$[0].gameMode", is(lobby.getGameMode().toString())))
                .andExpect(jsonPath("$[0].language", is((lobby.getLanguage().toString()))))
        ;
    }

    /**
     * Tests getting lobbies/{lobbyId}
     * Valid Input, returns the Lobby data
     */
    @Test
    public void getSpecificLobby_validInput_lobbyReturned() throws Exception {
        // given
        Lobby lobby = new Lobby();
        lobby.setId(1L);
        Deck testDeck = new Deck();
        lobby.setDeck(testDeck);
        User testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testName");
        testUser.setToken("1");
        Player testPlayer = new Player(testUser);
        testPlayer.setRole(PlayerRole.GUESSER);
        lobby.addPlayer(testPlayer);
        lobby.setGameMode(GameModeStatus.HUMANS);
        lobby.setLobbyStatus(LobbyStatus.WAITING);
        lobby.setCreator(testPlayer);
        lobby.setLanguage(Language.DE);

        given(userService.checkUserToken(Mockito.anyString())).willReturn(testUser);
        given(playerService.checkPlayerToken(Mockito.anyString())).willReturn(true);
        given(lobbyService.isUsernameInLobby(Mockito.anyString(), Mockito.any())).willReturn(true);
        given(lobbyService.addPlayerToLobby(Mockito.any(), Mockito.any())).willReturn(lobby);
        given(lobbyService.getLobbyById(Mockito.anyLong())).willReturn(lobby);


        // make get Request to Lobby with id
        MockHttpServletRequestBuilder getRequest = get("/lobbies/" + lobby.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .header("Token", "1");

        // then
        mockMvc.perform(getRequest)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(lobby.getId().intValue())))
                .andExpect(jsonPath("$.lobbyName", is(lobby.getLobbyName())))
                .andExpect(jsonPath("$.players[0].id", is(toIntExact(lobby.getPlayers().iterator().next().getId()))))
                .andExpect(jsonPath("$.players[0].role", is(lobby.getPlayers().iterator().next().getRole().name())))
                .andExpect(jsonPath("$.gameMode", is(lobby.getGameMode().toString())))
                .andExpect(jsonPath("$.language", is((lobby.getLanguage().toString()))))
                ;
    }

    /**
     * Tests joining lobbies/{lobbyId}/join
     * Valid Input, adds the User that sends the request to the Lobby
     */
    @Test
    public void joinSpecificLobby_validInput_playerAdded() throws Exception {
        // given
        Lobby lobby = new Lobby();
        lobby.setId(1L);
        lobby.setLobbyName("testName");
        Deck testDeck = new Deck();
        lobby.setDeck(testDeck);
        User testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testUser");
        Player testPlayer = new Player(testUser);
        testPlayer.setRole(PlayerRole.GUESSER);
        lobby.addPlayer(testPlayer);
        lobby.setGameMode(GameModeStatus.HUMANS);
        lobby.setCreator(testPlayer);
        lobby.setLobbyStatus(LobbyStatus.WAITING);
        User testUser2 = new User();
        testUser2.setUsername("testUser2");
        testUser2.setToken("2");

        given(playerService.checkPlayerToken(Mockito.anyString())).willReturn(true);
        given(lobbyService.isUsernameInLobby(Mockito.anyString(), Mockito.any())).willReturn(true);
        given(lobbyService.addPlayerToLobby(Mockito.any(), Mockito.any())).willReturn(lobby);
        given(userService.checkUserToken(Mockito.anyString())).willReturn(testUser);
        given(lobbyService.getLobbyById(Mockito.anyLong())).willReturn(lobby);
        given(playerService.convertUserToPlayer(Mockito.any(),Mockito.any())).willReturn(testPlayer);
        Mockito.doNothing().when(gameService).addStats(Mockito.any(),Mockito.any());

        // make get Request to Lobby with id
        MockHttpServletRequestBuilder putRequest = put("/lobbies/" + lobby.getId() + "/join")
                .contentType(MediaType.APPLICATION_JSON)
                .header("token", testUser2.getToken());

        // then
        mockMvc.perform(putRequest)
                .andExpect(status().isNoContent())
                .andExpect(jsonPath("$").doesNotExist())
                .andDo(print());
    }

    /**
     * Tests GET /lobbies
     * Valid input, but not allowed to view page (no Token / wrong token)
     */
    @Test
    public void getLobbies_wrongToken_exceptionReturned() throws Exception {
        // given
        String exceptionMsg = "You are not allowed to access this page";

        Lobby lobby = new Lobby();
        lobby.setId(1L);
        lobby.setLobbyName("testName");
        Deck testDeck = new Deck();
        lobby.setDeck(testDeck);
        User testUser = new User();
        testUser.setId(1L);
        testUser.setToken("1");
        Player testPlayer = new Player(testUser);
        lobby.addPlayer(testPlayer);
        lobby.setCreator(testPlayer);
        lobby.setLobbyStatus(LobbyStatus.WAITING);

        given(playerService.checkPlayerToken(Mockito.anyString())).willReturn(true);
        given(userService.checkUserToken(Mockito.anyString())).willThrow(new UnauthorizedException(exceptionMsg));

        // when/then -> do the request + validate the result
        MockHttpServletRequestBuilder getRequest = get("/lobbies")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Token", "wrongToken");

        // then
        mockMvc.perform(getRequest)
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$", is(exceptionMsg)))
                .andDo(print());
    }

    /**
     * Helper Method to convert lobbyPostDTO into a JSON string such that the input can be processed
     * Input will look like this: {"name": "Test Lobby", "lobbyName": "testName"}
     * @param object
     * @return string
     */
    private String asJsonString(final Object object) {
        try {
            return new ObjectMapper().writeValueAsString(object);
        }
        catch (JsonProcessingException e) {
            throw new SopraServiceException(String.format("The request body could not be created.%s", e.toString()));
        }
    }

    /**
     * Test to get lobby statistics
     * */
    @Test
    public void getLobbyStatistics_validInput() throws Exception {
        // given
        Lobby lobby = new Lobby();
        lobby.setId(1L);
        lobby.setLobbyName("testName");
        User testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testUser");
        testUser.setToken("1");
        Player testPlayer = new Player(testUser);
        testPlayer.setRole(PlayerRole.GUESSER);
        lobby.addPlayer(testPlayer);
        lobby.setGameMode(GameModeStatus.HUMANS);
        lobby.setCreator(testPlayer);
        lobby.setLobbyStatus(LobbyStatus.WAITING);
        User testUser2 = new User();
        testUser2.setUsername("testUser2");
        testUser2.setToken("2");
        testUser2.setId(2L);
        Player testPlayer2 = new Player(testUser2);
        testPlayer2.setRole(PlayerRole.CLUE_CREATOR);
        lobby.addPlayer(testPlayer2);

        List<GameStats> gameStatsList = new ArrayList<>();
        GameStats gameStats = new GameStats(testPlayer.getId(), lobby.getId());
        gameStats.setGivenClues(3l);
        gameStats.setGoodClues(1l);
        gameStats.setTeamPoints(20l);
        gameStatsList.add(gameStats);

        given(gameService.getAllLobbyGameStats(lobby.getId())).willReturn(gameStatsList);
        given(playerService.getPlayerById(1L)).willReturn(testPlayer);
        given(playerService.getPlayerById(2L)).willReturn(testPlayer2);
        given(userService.checkUserToken(Mockito.anyString())).willReturn(testUser);
        given(lobbyService.getLobbyById(Mockito.anyLong())).willReturn(lobby);

        // make get Request to Lobby with id
        MockHttpServletRequestBuilder getRequest = get("/lobbies/" + lobby.getId() + "/stats")
                .contentType(MediaType.APPLICATION_JSON)
                .header("token", testUser.getToken());

        // then
        mockMvc.perform(getRequest)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").exists())
                .andExpect(jsonPath("$[0].playerId", is(testPlayer.getId().intValue())))
                .andExpect(jsonPath("$[0].givenClues", is(gameStats.getGivenClues().intValue())))
                .andExpect(jsonPath("$[0].goodClues", is(gameStats.getGoodClues().intValue())))
                .andExpect(jsonPath("$[0].teamPoints", is(gameStats.getTeamPoints().intValue())))
                .andDo(print());
    }

    /**
     * Test to go to next round
     * */
    @Test
    public void getNextRound_validInput() throws Exception {
        // given
        Lobby lobby = new Lobby();
        lobby.setId(1L);
        lobby.setLobbyName("testName");
        User testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testUser");
        testUser.setToken("1");
        Player testPlayer = new Player(testUser);
        testPlayer.setRole(PlayerRole.GUESSER);
        lobby.addPlayer(testPlayer);

        given(userService.checkUserToken(Mockito.anyString())).willReturn(testUser);
        doNothing().when(lobbyService).nextRound(Mockito.anyLong(), Mockito.anyString());


        // make get Request to Lobby with id
        MockHttpServletRequestBuilder putRequest = put("/lobbies/" + lobby.getId() + "/nextRound")
                .contentType(MediaType.APPLICATION_JSON)
                .header("token", testUser.getToken());

        // then
        mockMvc.perform(putRequest)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value("next Round"))
                .andDo(print());
    }

    /**
     * Test to get word definition
     * */
    @Test
    public void getWordDefinition_validInput() throws Exception {
        // given
        Lobby lobby = new Lobby();
        lobby.setId(1L);
        lobby.setLobbyName("testName");
        User testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testUser");
        testUser.setToken("1");
        Player testPlayer = new Player(testUser);
        testPlayer.setRole(PlayerRole.GUESSER);
        lobby.addPlayer(testPlayer);

        String word = "Star";

        given(userService.checkUserToken(Mockito.anyString())).willReturn(testUser);
        doNothing().when(lobbyService).nextRound(Mockito.anyLong(), Mockito.anyString());


        // make get Request to Lobby with id
        MockHttpServletRequestBuilder getRequest = get("/lobbies/" + lobby.getId() + "/definition/" + word)
                .contentType(MediaType.APPLICATION_JSON)
                .header("token", testUser.getToken());

        // then
        mockMvc.perform(getRequest)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").exists())
                .andDo(print());
    }

    /**
     * Test to guess correct mystery word
     * */
    @Test
    public void guessMysteryWord_validInput() throws Exception {
        // given
        Lobby lobby = new Lobby();
        lobby.setId(1L);
        lobby.setLobbyName("testName");
        User testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testUser");
        testUser.setToken("1");
        Player testPlayer = new Player(testUser);
        testPlayer.setRole(PlayerRole.GUESSER);
        lobby.addPlayer(testPlayer);
        lobby.setGameMode(GameModeStatus.HUMANS);
        lobby.setCreator(testPlayer);
        lobby.setLobbyStatus(LobbyStatus.WAITING);
        String guessedWord = "Moon";
        String mysteryWord = "Star";
        boolean success = true;
        int leftCards = 1;
        Long wonCards = 2l;
        int lostCards = 6;

        given(gameService.getGuess(lobby)).willReturn(guessedWord);
        given(gameService.getMysteryWord(lobby)).willReturn(mysteryWord);
        given(gameService.getGuessSuccess(lobby)).willReturn(success);
        given(gameService.getLeftCards(lobby)).willReturn(leftCards);
        given(gameService.getWonCards(lobby)).willReturn(wonCards);
        given(gameService.getLostCards(lobby)).willReturn(lostCards);

        given(playerService.getPlayerById(1L)).willReturn(testPlayer);
        given(userService.checkUserToken(Mockito.anyString())).willReturn(testUser);
        given(lobbyService.getLobbyById(Mockito.anyLong())).willReturn(lobby);

        // make get Request to Lobby with id
        MockHttpServletRequestBuilder getRequest = get("/lobbies/" + lobby.getId() + "/guess")
                .contentType(MediaType.APPLICATION_JSON)
                .header("token", testUser.getToken());

        // then
        mockMvc.perform(getRequest)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").exists())
                .andExpect(jsonPath("$.guess", is(guessedWord)))
                .andExpect(jsonPath("$.leftCards", is(leftCards)))
                .andExpect(jsonPath("$.wonCards", is(wonCards.intValue())))
                .andExpect(jsonPath("$.mysteryWord", is(mysteryWord)))
                .andDo(print());

    }

    /**
     * Tests creator kickPlayerOut lobbies/{lobbyId}/kick/{UserId}
     * Valid Input, adds the User that sends the request to the Lobby
     */
    @Test
    public void kickPlayerOut_validInput() throws Exception {
        // given
        Lobby lobby = new Lobby();
        lobby.setId(1L);
        lobby.setLobbyName("testName");
        User testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testUser");
        testUser.setToken("1");
        Player testPlayer = new Player(testUser);
        testPlayer.setRole(PlayerRole.GUESSER);
        lobby.addPlayer(testPlayer);
        lobby.setGameMode(GameModeStatus.HUMANS);
        lobby.setCreator(testPlayer);
        lobby.setLobbyStatus(LobbyStatus.WAITING);
        User testUser2 = new User();
        testUser2.setUsername("testUser2");
        testUser2.setToken("2");
        testUser2.setId(2L);
        Player testPlayer2 = new Player(testUser2);
        testPlayer2.setRole(PlayerRole.CLUE_CREATOR);
        lobby.addPlayer(testPlayer2);


        given(playerService.checkPlayerToken(Mockito.anyString())).willReturn(true);
        given(lobbyService.isUsernameInLobby(Mockito.anyString(), Mockito.any())).willReturn(true);
        given(lobbyService.kickOutPlayer(Mockito.any(), Mockito.any(),Mockito.any())).willReturn(true);
        given(userService.checkUserToken(Mockito.anyString())).willReturn(testUser);
        given(lobbyService.getLobbyById(Mockito.anyLong())).willReturn(lobby);
        given(lobbyService.isUserLobbyCreator(Mockito.any(),Mockito.any())).willReturn(true);

        // make get Request to Lobby with id
        MockHttpServletRequestBuilder putRequest = put("/lobbies/" + lobby.getId() + "/kick/" + testPlayer2.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .header("token", testUser.getToken());

        // then
        mockMvc.perform(putRequest)
                .andExpect(status().isNoContent())
                .andExpect(jsonPath("$").doesNotExist())
                .andDo(print());

        Assert.isNull(playerService.getPlayerById(2L));
    }

    /**
     * Tests inviteUserToLobby POST /lobbies/{lobbyId}/invite/{userId}
     * Valid Input, adds lobby to user's inviting lobbies
     */
    @Test
    public void inviteUserToLobby_validInput() throws Exception {
        // given
        // init lobby
        Lobby lobby = new Lobby();
        lobby.setId(2L);
        lobby.setLobbyName("testName");
        User testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testUser");
        testUser.setToken("1");
        Player testPlayer = new Player(testUser);
        testPlayer.setRole(PlayerRole.GUESSER);
        lobby.addPlayer(testPlayer);
        lobby.setGameMode(GameModeStatus.HUMANS);
        lobby.setCreator(testPlayer);
        lobby.setLobbyStatus(LobbyStatus.WAITING);
        // init user to be invited
        User testInvitedUser = new User();
        testInvitedUser.setUsername("testInvitedUser");
        testInvitedUser.setToken("3");
        testInvitedUser.setId(3L);
        testInvitedUser.setStatus(UserStatus.ONLINE);


        given(playerService.checkPlayerToken(Mockito.anyString())).willReturn(false);
        given(userService.checkUserToken(Mockito.anyString())).willReturn(testUser);
        given(lobbyService.isUserInLobby(testUser,2L)).willReturn(true);
        given(lobbyService.getLobbyById(Mockito.anyLong())).willReturn(lobby);
        given(userService.getUserByID(Mockito.anyLong())).willReturn(testInvitedUser);
        given(playerService.getPlayerById(Mockito.anyLong())).willThrow(new ForbiddenException("Player not found"));
        given(lobbyService.isUserInLobby(testInvitedUser,2L)).willReturn(false);
        doNothing().when(lobbyService).inviteUserToLobby(Mockito.any(), Mockito.any());

        // make post Request to Lobby with id & user with id
        MockHttpServletRequestBuilder postRequest = post("/lobbies/" + lobby.getId() + "/invite/" + testInvitedUser.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .header("token", testUser.getToken());

        // then
        mockMvc.perform(postRequest)
                .andExpect(status().isNoContent())
                .andExpect(jsonPath("$").doesNotExist())
                .andDo(print());
    }

    /**
     * Tests inviteUserToLobby POST /lobbies/{lobbyId}/invite/{userId}
     * Invalid token, throws UnauthorizedException
     */
    @Test
    public void inviteUserToLobby_invalidToken() throws Exception {
        // given
        // init lobby
        Lobby lobby = new Lobby();
        lobby.setId(2L);
        lobby.setLobbyName("testName");
        User testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testUser");
        testUser.setToken("1");
        Player testPlayer = new Player(testUser);
        testPlayer.setRole(PlayerRole.GUESSER);
        lobby.addPlayer(testPlayer);
        lobby.setGameMode(GameModeStatus.HUMANS);
        lobby.setCreator(testPlayer);
        lobby.setLobbyStatus(LobbyStatus.WAITING);
        // init user to be invited
        User testInvitedUser = new User();
        testInvitedUser.setUsername("testInvitedUser");
        testInvitedUser.setToken("3");
        testInvitedUser.setId(3L);
        testInvitedUser.setStatus(UserStatus.ONLINE);


        given(playerService.checkPlayerToken(Mockito.anyString())).willReturn(true);

        // make post Request to Lobby with id & user with id
        MockHttpServletRequestBuilder postRequest = post("/lobbies/" + lobby.getId() + "/invite/" + testInvitedUser.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .header("token", "5");

        // then
        mockMvc.perform(postRequest)
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$", is("Requesting player does not exist!")))
                .andDo(print());
    }

    /**
     * Tests inviteUserToLobby POST /lobbies/{lobbyId}/invite/{userId}
     * Lobby doesn't exist, throws NotFoundException
     */
    @Test
    public void inviteUserToLobby_lobbyNotFound() throws Exception {
        // given
        // init lobby
        User testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testUser");
        testUser.setToken("1");
        Player testPlayer = new Player(testUser);
        testPlayer.setRole(PlayerRole.GUESSER);
        // init user to be invited
        User testInvitedUser = new User();
        testInvitedUser.setUsername("testInvitedUser");
        testInvitedUser.setToken("3");
        testInvitedUser.setId(3L);
        testInvitedUser.setStatus(UserStatus.ONLINE);


        given(playerService.checkPlayerToken(Mockito.anyString())).willReturn(false);
        given(userService.checkUserToken(Mockito.anyString())).willReturn(testUser);
        given(lobbyService.isUserInLobby(testUser,2L)).willThrow(new NotFoundException("The requested Lobby does not exist."));
        given(lobbyService.getLobbyById(Mockito.anyLong())).willThrow(new NotFoundException("The requested Lobby does not exist."));

        // make post Request to Lobby with id & user with id
        MockHttpServletRequestBuilder postRequest = post("/lobbies/" + 2L + "/invite/" + testInvitedUser.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .header("token", testUser.getToken());

        // then
        mockMvc.perform(postRequest)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$", is("The requested Lobby does not exist.")))
                .andDo(print());
    }

    /**
     * Tests inviteUserToLobby POST /lobbies/{lobbyId}/invite/{userId}
     * invitedUser doesn't exist, throws NotFoundException
     */
    @Test
    public void inviteUserToLobby_invitedUserNotFound() throws Exception {
        // given
        // init lobby
        Lobby lobby = new Lobby();
        lobby.setId(2L);
        lobby.setLobbyName("testName");
        User testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testUser");
        testUser.setToken("1");
        Player testPlayer = new Player(testUser);
        testPlayer.setRole(PlayerRole.GUESSER);
        lobby.addPlayer(testPlayer);
        lobby.setGameMode(GameModeStatus.HUMANS);
        lobby.setCreator(testPlayer);
        lobby.setLobbyStatus(LobbyStatus.WAITING);


        given(playerService.checkPlayerToken(Mockito.anyString())).willReturn(false);
        given(userService.checkUserToken(Mockito.anyString())).willReturn(testUser);
        given(lobbyService.isUserInLobby(testUser,2L)).willReturn(true);
        given(lobbyService.getLobbyById(Mockito.anyLong())).willReturn(lobby);
        given(userService.getUserByID(Mockito.anyLong())).willThrow(new NotFoundException("User was not found"));

        // make post Request to Lobby with id & user with id
        MockHttpServletRequestBuilder postRequest = post("/lobbies/" + lobby.getId() + "/invite/" + 3L)
                .contentType(MediaType.APPLICATION_JSON)
                .header("token", testUser.getToken());

        // then
        mockMvc.perform(postRequest)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$", is("User was not found")))
                .andDo(print());
    }

    /**
     * Tests inviteUserToLobby POST /lobbies/{lobbyId}/invite/{userId}
     * invitedUser is offline, throws ForbiddenException
     */
    @Test
    public void inviteUserToLobby_invitedUserIsOffline() throws Exception {
        // given
        // init lobby
        Lobby lobby = new Lobby();
        lobby.setId(2L);
        lobby.setLobbyName("testName");
        User testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testUser");
        testUser.setToken("1");
        Player testPlayer = new Player(testUser);
        testPlayer.setRole(PlayerRole.GUESSER);
        lobby.addPlayer(testPlayer);
        lobby.setGameMode(GameModeStatus.HUMANS);
        lobby.setCreator(testPlayer);
        lobby.setLobbyStatus(LobbyStatus.WAITING);
        // init user to be invited
        User testInvitedUser = new User();
        testInvitedUser.setUsername("testInvitedUser");
        testInvitedUser.setToken("3");
        testInvitedUser.setId(3L);
        testInvitedUser.setStatus(UserStatus.OFFLINE);


        given(playerService.checkPlayerToken(Mockito.anyString())).willReturn(false);
        given(userService.checkUserToken(Mockito.anyString())).willReturn(testUser);
        given(lobbyService.isUserInLobby(testUser,2L)).willReturn(true);
        given(lobbyService.getLobbyById(Mockito.anyLong())).willReturn(lobby);
        given(userService.getUserByID(Mockito.anyLong())).willReturn(testInvitedUser);

        // make post Request to Lobby with id & user with id
        MockHttpServletRequestBuilder postRequest = post("/lobbies/" + lobby.getId() + "/invite/" + testInvitedUser.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .header("token", testUser.getToken());

        // then
        mockMvc.perform(postRequest)
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$", is("Requested User is offline!")))
                .andDo(print());
    }

    /**
     * Tests inviteUserToLobby POST /lobbies/{lobbyId}/invite/{userId}
     * invitedUser is already in another lobby, throws ForbiddenException
     */
    @Test
    public void inviteUserToLobby_invitedUserInOtherLobby() throws Exception {
        // given
        // init inviting lobby
        Lobby invLobby = new Lobby();
        invLobby.setId(2L);
        invLobby.setLobbyName("testName");
        User testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testUser");
        testUser.setToken("1");
        Player testPlayer = new Player(testUser);
        testPlayer.setRole(PlayerRole.GUESSER);
        invLobby.addPlayer(testPlayer);
        invLobby.setGameMode(GameModeStatus.HUMANS);
        invLobby.setCreator(testPlayer);
        invLobby.setLobbyStatus(LobbyStatus.WAITING);
        // init user to be invited
        User testInvitedUser = new User();
        testInvitedUser.setUsername("testInvitedUser");
        testInvitedUser.setToken("3");
        testInvitedUser.setId(3L);
        testInvitedUser.setStatus(UserStatus.ONLINE);
        // init other lobby
        Lobby otherLobby = new Lobby();
        otherLobby.setId(4L);
        otherLobby.setLobbyName("otherLobby");
        Player testInvitedPlayer = new Player(testInvitedUser);
        testInvitedPlayer.setRole(PlayerRole.GUESSER);
        otherLobby.addPlayer(testInvitedPlayer);
        otherLobby.setGameMode(GameModeStatus.HUMANS);
        otherLobby.setCreator(testInvitedPlayer);
        otherLobby.setLobbyStatus(LobbyStatus.WAITING);


        given(playerService.checkPlayerToken(Mockito.anyString())).willReturn(false);
        given(userService.checkUserToken(Mockito.anyString())).willReturn(testUser);
        given(lobbyService.isUserInLobby(testUser,2L)).willReturn(true);
        given(lobbyService.getLobbyById(Mockito.anyLong())).willReturn(invLobby);
        given(userService.getUserByID(Mockito.anyLong())).willReturn(testInvitedUser);
        given(playerService.getPlayerById(Mockito.anyLong())).willReturn(testInvitedPlayer);
        given(lobbyService.isUserInLobby(testInvitedUser,2L)).willReturn(false);
        // doNothing().when(lobbyService).inviteUserToLobby(Mockito.any(), Mockito.any());

        // make post Request to Lobby with id & user with id
        MockHttpServletRequestBuilder postRequest = post("/lobbies/" + invLobby.getId() + "/invite/" + testInvitedUser.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .header("token", testUser.getToken());

        // then
        mockMvc.perform(postRequest)
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$", is("Requested User is in another lobby!")))
                .andDo(print());
    }
}