package ch.uzh.ifi.seal.soprafs20.controller;

import ch.uzh.ifi.seal.soprafs20.entity.User;
import ch.uzh.ifi.seal.soprafs20.exceptions.ConflictException;
import ch.uzh.ifi.seal.soprafs20.exceptions.NotFoundException;
import ch.uzh.ifi.seal.soprafs20.exceptions.SopraServiceException;
import ch.uzh.ifi.seal.soprafs20.exceptions.UnauthorizedException;
import ch.uzh.ifi.seal.soprafs20.rest.dto.LobbyPostDTO;
import ch.uzh.ifi.seal.soprafs20.rest.dto.LobbyGetDTO;
import ch.uzh.ifi.seal.soprafs20.rest.dto.LobbyPutDTO;
import ch.uzh.ifi.seal.soprafs20.service.LobbyService;
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

    /**
     * Tests post /lobbies
     * Valid Input, returns lobby data
     */
    @Test
    public void createLobby_validInput_lobbyCreated() throws Exception {
        // given
        Lobby lobby = new Lobby();
        lobby.setId(1L);
        lobby.setLobbyname("testName");
        lobby.setDeck(testDeck);
        lobby.addPlayer(2L);
        lobby.setGamemode(GameModeStatus.HUMANS);
        lobby.setCreator(2L);

        LobbyPostDTO lobbyPostDTO = new LobbyPostDTO();
        lobbyPostDTO.setLobbyname("testName");
        lobbyPostDTO.setGamemode(GameModeStatus.HUMANS);
        lobbyPostDTO.setPlayerId(2L);

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
                .andExpect(jsonPath("$.deck", is(lobby.getDeck())))
                .andExpect(jsonPath("$.players[0]", is(lobby.getPlayers()[0])))
                .andExpect(jsonPath("$.gamemode", is(lobby.getGamemode().toString())))
                .andExpect(jsonPath("$.creator", is(lobby.getCreator())))
                ;
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
        lobbyPostDTO.setLobbyname("testName");
        lobbyPostDTO.setGamemode(GameModeStatus.HUMANS);
        lobbyPostDTO.setPlayerId(2L);

        given(lobbyService.checkToken(Mockito.anyString())).willThrow(new UnauthorizedException(exceptionMsg));

        // when/then -> do the request + validate the result
        MockHttpServletRequestBuilder postRequest = post("/lobbies")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(lobbyPostDTO));


        // then
        mockMvc.perform(getRequest)
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$", is(exceptionMsg)))
                .andDo(print());
    }

    /**
     * Tests post /lobbies
     * Valid input, but not allowed to view page (no Token / wrong token)
     */
    @Test
    public void createLobby_invalidInput_exceptionReturned() throws Exception {
        // given
        String exceptionMsg = "Conflict: same player has another lobby open.";

        LobbyPostDTO lobbyPostDTO = new LobbyPostDTO();
        lobbyPostDTO.setLobbyname("testName");
        lobbyPostDTO.setGamemode(GameModeStatus.HUMANS);
        lobbyPostDTO.setPlayerId(2L);

        given(lobbyService.checkCreator(Mockito.anyString())).willThrow(new ConflictException(exceptionMsg));

        // when/then -> do the request + validate the result
        MockHttpServletRequestBuilder postRequest = post("/lobbies")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(lobbyPostDTO));


        // then
        mockMvc.perform(getRequest)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$", is(exceptionMsg)))
                .andDo(print());
    }

    /**
     * Helper Method to convert lobbyPostDTO into a JSON string such that the input can be processed
     * Input will look like this: {"name": "Test LObby", "lobbyName": "testName"}
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