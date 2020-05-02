package ch.uzh.ifi.seal.soprafs20.controller;

import ch.uzh.ifi.seal.soprafs20.entity.Lobby;
import ch.uzh.ifi.seal.soprafs20.entity.User;
import ch.uzh.ifi.seal.soprafs20.exceptions.SopraServiceException;
import ch.uzh.ifi.seal.soprafs20.repository.LobbyRepository;
import ch.uzh.ifi.seal.soprafs20.repository.UserRepository;
import ch.uzh.ifi.seal.soprafs20.rest.dto.LobbyPostDTO;
import ch.uzh.ifi.seal.soprafs20.rest.dto.UserPostDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static ch.uzh.ifi.seal.soprafs20.constant.GameModeStatus.HUMANS;
import static java.lang.Math.toIntExact;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.junit.jupiter.api.Assertions.*;

//@DataJpaTest
//@WebMvcTest
@SpringBootTest
@AutoConfigureMockMvc
//@EnableAutoConfiguration
//@SpringBootConfiguration
public class LobbyControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LobbyRepository lobbyRepository;

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
        //login user1
        requestBuilder = getMockHttpRequestBuilderForPut(userPostDTO, "/login");
        httpPutRequest(userEntity, requestBuilder);
        //login user2
        requestBuilder = getMockHttpRequestBuilderForPut(userPostDTO, "/login");
        httpPutRequest(userEntity, requestBuilder);

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

        lobby = lobbyRepository.findByLobbyId(lobby.getId());
        assertNotNull(lobby);
        assertEquals(2, lobby.getPlayers().size());

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
