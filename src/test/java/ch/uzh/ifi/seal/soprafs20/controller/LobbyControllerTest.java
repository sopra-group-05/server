package ch.uzh.ifi.seal.soprafs20.controller;

import ch.uzh.ifi.seal.soprafs20.constant.GameModeStatus;
import ch.uzh.ifi.seal.soprafs20.constant.Language;
import ch.uzh.ifi.seal.soprafs20.constant.LobbyStatus;
import ch.uzh.ifi.seal.soprafs20.constant.PlayerRole;
import ch.uzh.ifi.seal.soprafs20.entity.Lobby;
import ch.uzh.ifi.seal.soprafs20.entity.Player;
import ch.uzh.ifi.seal.soprafs20.entity.User;
import ch.uzh.ifi.seal.soprafs20.exceptions.ConflictException;
import ch.uzh.ifi.seal.soprafs20.exceptions.SopraServiceException;
import ch.uzh.ifi.seal.soprafs20.exceptions.UnauthorizedException;
import ch.uzh.ifi.seal.soprafs20.entity.Deck;
import static java.lang.Math.toIntExact;
import ch.uzh.ifi.seal.soprafs20.rest.dto.LobbyPostDTO;
//import ch.uzh.ifi.seal.soprafs20.rest.dto.LobbyPutDTO;
import ch.uzh.ifi.seal.soprafs20.service.LobbyService;
import ch.uzh.ifi.seal.soprafs20.service.PlayerService;
import ch.uzh.ifi.seal.soprafs20.service.UserService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.BDDMockito.given;
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
        lobby.addPlayer(testPlayer);
        lobby.setGameMode(GameModeStatus.HUMANS);
        lobby.setCreator(testPlayer);

        LobbyPostDTO lobbyPostDTO = new LobbyPostDTO();
        lobbyPostDTO.setLobbyName("testName");
        lobbyPostDTO.setGameMode(0);
        lobbyPostDTO.setLanguage("EN");

        given(playerService.checkPlayerToken(Mockito.any())).willReturn(true);
        given(userService.createUser(Mockito.any())).willReturn(testUser);
        given(lobbyService.createLobby(Mockito.any())).willReturn(lobby);


        // when/then -> do the request + validate the result
        MockHttpServletRequestBuilder postRequest = post("/lobbies")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(lobbyPostDTO));

        // then
        mockMvc.perform(postRequest)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(lobby.getId().intValue())))
                .andExpect(jsonPath("$.lobbyName", is(lobby.getLobbyName())))
                .andExpect(jsonPath("$.deck.deckId", is(lobby.getDeck().getDeckId())))
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
                .andExpect(jsonPath("$[0].deck.deckId", is(lobby.getDeck().getDeckId())))
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
                .andExpect(jsonPath("$.deck.deckId", is(lobby.getDeck().getDeckId())))
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
}