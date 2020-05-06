package ch.uzh.ifi.seal.soprafs20.controller;

import ch.uzh.ifi.seal.soprafs20.constant.LobbyStatus;
import ch.uzh.ifi.seal.soprafs20.constant.PlayerStatus;
import ch.uzh.ifi.seal.soprafs20.entity.*;
import ch.uzh.ifi.seal.soprafs20.exceptions.ForbiddenException;
import ch.uzh.ifi.seal.soprafs20.exceptions.SopraServiceException;
import ch.uzh.ifi.seal.soprafs20.repository.ClueRepository;
import ch.uzh.ifi.seal.soprafs20.repository.LobbyRepository;
import ch.uzh.ifi.seal.soprafs20.repository.UserRepository;
import ch.uzh.ifi.seal.soprafs20.rest.dto.CluePostDTO;
import ch.uzh.ifi.seal.soprafs20.rest.dto.GuessPostDTO;
import ch.uzh.ifi.seal.soprafs20.rest.dto.LobbyPostDTO;
import ch.uzh.ifi.seal.soprafs20.rest.dto.UserPostDTO;
import ch.uzh.ifi.seal.soprafs20.service.ClueService;
import ch.uzh.ifi.seal.soprafs20.service.LobbyService;
import ch.uzh.ifi.seal.soprafs20.service.PlayerService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.result.JsonPathResultMatchers;

import java.util.ArrayList;
import java.util.List;

import static ch.uzh.ifi.seal.soprafs20.constant.GameModeStatus.HUMANS;
import static java.lang.Math.toIntExact;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.junit.jupiter.api.Assertions.*;

//@DataJpaTest
//@WebMvcTest
@SpringBootTest
@AutoConfigureMockMvc
//@EnableAutoConfiguration
//@SpringBootConfiguration
@TestPropertySource(locations = "/application-test.properties")
public class LobbyControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LobbyRepository lobbyRepository;

    @Autowired
    private LobbyService lobbyService;

    @Autowired
    private PlayerService playerService;

    @Autowired
    private ClueRepository clueRepository;

    /**
     * Integration test to create user, login, create lobby
     * */
    @Test
    void registerAndStartLobbyTests() throws Exception {
        UserPostDTO userPostDTO = getUserPostDTO("user1", "pwd1");
        // register user1
        MockHttpServletRequestBuilder requestBuilder = getMockHttpServletRequestBuilderForPost(userPostDTO, "/users");
        httpPostRequest(userPostDTO, requestBuilder);

        User userEntity = userRepository.findByUsername(userPostDTO.getUsername());
        assertNotNull(userEntity);

        //register user2
        UserPostDTO userPostDTO2 = getUserPostDTO("user2", "pwd2");
        requestBuilder = getMockHttpServletRequestBuilderForPost(userPostDTO2, "/users");
        httpPostRequest(userPostDTO2, requestBuilder);
        User userEntity2 = userRepository.findByUsername(userPostDTO2.getUsername());
        assertNotNull(userEntity2);

        // register user 3
        UserPostDTO userPostDTO3 = getUserPostDTO("user3", "pwd3");
        requestBuilder = getMockHttpServletRequestBuilderForPost(userPostDTO3, "/users");
        httpPostRequest(userPostDTO3, requestBuilder);
        User userEntity3 = userRepository.findByUsername(userPostDTO3.getUsername());
        assertNotNull(userEntity3);

        //login user1
        requestBuilder = getMockHttpRequestBuilderForPut(userPostDTO, "/login");
        httpPutRequest(userEntity, requestBuilder);
        //login user2
        requestBuilder = getMockHttpRequestBuilderForPut(userPostDTO2, "/login");
        httpPutRequest(userEntity2, requestBuilder);
        //login user3
        requestBuilder = getMockHttpRequestBuilderForPut(userPostDTO3, "/login");
        httpPutRequest(userEntity3, requestBuilder);

        //user1 creates Lobby
        LobbyPostDTO lobbyPostDTO = new LobbyPostDTO();
        lobbyPostDTO.setLobbyName("testName");
        lobbyPostDTO.setGameMode(0);
        lobbyPostDTO.setLanguage("EN");

        requestBuilder = getMockHttpServletRequestBuilderForPost(lobbyPostDTO, "/lobbies");
        requestBuilder.header("Token", userEntity.getToken());

        // then
        mockMvc.perform(requestBuilder)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.lobbyName", is(lobbyPostDTO.getLobbyName())))
                .andExpect(jsonPath("$.players[0].id", is(toIntExact(userEntity.getId()))))
                .andExpect(jsonPath("$.gameMode", is(HUMANS.toString())))
                .andExpect(status().isCreated());
        Lobby lobby = lobbyRepository.findByLobbyName(lobbyPostDTO.getLobbyName());
        assertNotNull(lobby);

        //user2 joins lobby
        requestBuilder = getMockHttpRequestBuilderForPut(null, "/lobbies/"+ lobby.getId() +"/join");
        requestBuilder.header("Token", userEntity2.getToken());
        // then
        mockMvc.perform(requestBuilder)
                .andExpect(status().isNoContent())
                .andExpect(jsonPath("$").doesNotExist())
                .andDo(print());

        lobby = lobbyService.getLobbyById(lobby.getId());
        assertNotNull(lobby);
        assertEquals(2, lobby.getPlayers().size());

        // user 3 joins lobby
        requestBuilder = getMockHttpRequestBuilderForPut(null, "/lobbies/"+ lobby.getId() +"/join");
        requestBuilder.header("Token", userEntity3.getToken());
        // then
        mockMvc.perform(requestBuilder)
                .andExpect(status().isNoContent())
                .andExpect(jsonPath("$").doesNotExist())
                .andDo(print());

        lobby = lobbyService.getLobbyById(lobby.getId());
        assertNotNull(lobby);
        assertEquals(3, lobby.getPlayers().size());

        //user1 sets status ready
        requestBuilder = getMockHttpRequestBuilderForPut(null, "/lobbies/"+ lobby.getId() +"/ready");
        requestBuilder.header("Token", userEntity.getToken());
        // then
        mockMvc.perform(requestBuilder)
                .andExpect(status().isNoContent())
                .andExpect(jsonPath("$").isBoolean())
                .andDo(print());

        Player player = playerService.getPlayerById(userEntity.getId());
        assertNotNull(player);
        assertEquals(PlayerStatus.READY, player.getStatus());

        //user2 sets status ready
        requestBuilder = getMockHttpRequestBuilderForPut(null, "/lobbies/"+ lobby.getId() +"/ready");
        requestBuilder.header("Token", userEntity2.getToken());
        // then
        mockMvc.perform(requestBuilder)
                .andExpect(status().isNoContent())
                .andExpect(jsonPath("$").isBoolean())
                .andDo(print());

        player = playerService.getPlayerById(userEntity2.getId());
        assertNotNull(player);
        assertEquals(PlayerStatus.READY, player.getStatus());

        //user3 sets status ready
        requestBuilder = getMockHttpRequestBuilderForPut(null, "/lobbies/"+ lobby.getId() +"/ready");
        requestBuilder.header("Token", userEntity3.getToken());
        // then
        mockMvc.perform(requestBuilder)
                .andExpect(status().isNoContent())
                .andExpect(jsonPath("$").isBoolean())
                .andDo(print());

        player = playerService.getPlayerById(userEntity3.getId());
        assertNotNull(player);
        assertEquals(PlayerStatus.READY, player.getStatus());

        //creator (user1) starts lobby
        requestBuilder = getMockHttpRequestBuilderForPut(null, "/lobbies/"+ lobby.getId() +"/start");
        requestBuilder.header("Token", userEntity.getToken());

        // then
        mockMvc.perform(requestBuilder)
                .andExpect(status().isNoContent())
                .andExpect(jsonPath("$").doesNotExist())
                .andDo(print());

        lobby = lobbyService.getLobbyById(lobby.getId());
        assertEquals(LobbyStatus.RUNNING, lobby.getLobbyStatus());

        //user2 gets mysterywords
        callCardAPI(lobby.getId(), userEntity2.getToken());
        lobby = lobbyService.getLobbyById(lobby.getId());
        Card activeCard = lobby.getDeck().getActiveCard();

        //user1 picks number
        requestBuilder = getMockHttpServletRequestBuilderForPost(2, "/lobbies/"+ lobby.getId() +"/number");
        requestBuilder.header("Token", userEntity.getToken());
        // then
        mockMvc.perform(requestBuilder)
                .andExpect(status().isNoContent())
                .andExpect(jsonPath("$").doesNotExist());

        //user2 gives clue
        CluePostDTO cluePostDTO = new CluePostDTO();
        cluePostDTO.setHint("Star");
        requestBuilder = getMockHttpServletRequestBuilderForPost(cluePostDTO, "/lobbies/"+ lobby.getId() +"/clues");
        requestBuilder.header("Token", userEntity2.getToken());
        // then
        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").doesNotExist());

        //user3 gives clue
        CluePostDTO cluePostDTO3 = new CluePostDTO();
        cluePostDTO3.setHint("Star");
        requestBuilder = getMockHttpServletRequestBuilderForPost(cluePostDTO3, "/lobbies/"+ lobby.getId() +"/clues");
        requestBuilder.header("Token", userEntity3.getToken());
        // then
        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").doesNotExist());

        //user2 gets clues
        MockHttpServletRequestBuilder getRequest = get("/lobbies/" + lobby.getId() +"/clues")
                .contentType(MediaType.APPLICATION_JSON).header("Token", userEntity2.getToken());
        ResultActions result = mockMvc.perform(getRequest);
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.[0].id").exists())
                .andExpect(jsonPath("$.[0].hint").exists());

        List<Clue> clueList = clueRepository.findAll();
        // Flagging Clues
        //creator (user1) starts lobby
        List<Long> ids = new ArrayList();
        ids.add(clueList.get(0).getId());

        //user2 flags clue
        requestBuilder = getMockHttpRequestBuilderForPut(ids, "/lobbies/"+ lobby.getId() +"/clues/flag");
        requestBuilder.header("Token", userEntity2.getToken());
        // then
        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").doesNotExist())
                .andDo(print());

        //user3 flags clue
        requestBuilder = getMockHttpRequestBuilderForPut(ids, "/lobbies/"+ lobby.getId() +"/clues/flag");
        requestBuilder.header("Token", userEntity3.getToken());
        // then
        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").doesNotExist())
                .andDo(print());



        //user1 gets clues
        getRequest = get("/lobbies/" + lobby.getId() +"/clues")
                .contentType(MediaType.APPLICATION_JSON).header("Token", userEntity.getToken());
        result = mockMvc.perform(getRequest);
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
                /*.andExpect(jsonPath("$.[0].id").exists())
                .andExpect(jsonPath("$.[0].hint").exists());*/

        //user1 guesses
        GuessPostDTO guessPostDTO = new GuessPostDTO();
        guessPostDTO.setGuess(activeCard.getMysteryWords().get(1).getWord());
        requestBuilder = getMockHttpServletRequestBuilderForPost(guessPostDTO, "/lobbies/"+ lobby.getId() +"/guess");
        requestBuilder.header("Token", userEntity.getToken());
        // then
        mockMvc.perform(requestBuilder)
                .andExpect(status().isNoContent())
                .andExpect(jsonPath("$").doesNotExist());

        //user2 leaves lobby
        MockHttpServletRequestBuilder requestBuilderError = getMockHttpRequestBuilderForPut(null, "/lobbies/"+ lobby.getId() +"/stop");
        requestBuilderError.header("Token", userEntity2.getToken());

        //todo: work in progress as stopping lobby is not fully implemented
        // then
        mockMvc.perform(requestBuilder)
                .andExpect(status().isNoContent());
                /*.andExpect(jsonPath("$.id", is(lobby.getId())))
                .andExpect(jsonPath("$.lobbyName", is(lobbyPostDTO.getLobbyName())))
                .andExpect(jsonPath("$.lobbyStatus", is(LobbyStatus.STOPPED)))
                .andDo(print());*/

    }

    private void callCardAPI(Long lobbyId, String token) throws Exception {
        MockHttpServletRequestBuilder getRequest = get("/lobbies/" + lobbyId +"/card")
                .contentType(MediaType.APPLICATION_JSON).header("Token", token);
        ResultActions result = mockMvc.perform(getRequest);
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.[0].id").exists())
                .andExpect(jsonPath("$.[0].word").exists())
                .andExpect(jsonPath("$.[0].status").exists())
                .andDo(print());
    }

    private UserPostDTO getUserPostDTO(String username, String password) {
        UserPostDTO userPostDTO = new UserPostDTO();
        userPostDTO.setUsername(username);
        userPostDTO.setPassword(password);
        return userPostDTO;
    }

    private void  httpPostRequest(UserPostDTO userPostDTO, MockHttpServletRequestBuilder postRequest) throws Exception {
        // then
        mockMvc.perform(postRequest)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.status", is("OFFLINE")))
                .andExpect(jsonPath("$.username", is(userPostDTO.getUsername())))
                .andExpect(status().isCreated());
        //.andExpect(header().string("Location", "/users/"+user.getId().intValue()))
    }

    private void httpPutRequest(User user, MockHttpServletRequestBuilder putRequest) throws Exception {
        // then
        mockMvc.perform(putRequest)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(user.getId().intValue())))
                .andExpect(jsonPath("$.token", is(user.getToken())));
    }

    private MockHttpServletRequestBuilder getMockHttpServletRequestBuilderForPost(Object postDto, String urlTemplate) {
        // when/then -> do the request + validate the result
        return post(urlTemplate)
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(postDto));
    }

    private MockHttpServletRequestBuilder getMockHttpRequestBuilderForPut(Object postDTO, String urlTemplate) {
        // when/then -> do the request + validate the result
        MockHttpServletRequestBuilder builder = put(urlTemplate)
                .contentType(MediaType.APPLICATION_JSON);
        if(postDTO != null) {
            builder.content(asJsonString(postDTO));
        }
        return builder;
    }

    /**
     * Helper Method to convert userPostDTO into a JSON string such that the input can be processed
     * Input will look like this: {"name": "Test User", "username": "testUsername"}
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
