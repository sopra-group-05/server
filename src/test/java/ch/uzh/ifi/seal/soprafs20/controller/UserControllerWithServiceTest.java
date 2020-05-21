package ch.uzh.ifi.seal.soprafs20.controller;

import ch.uzh.ifi.seal.soprafs20.constant.UserStatus;
import ch.uzh.ifi.seal.soprafs20.entity.Lobby;
import ch.uzh.ifi.seal.soprafs20.entity.User;
import ch.uzh.ifi.seal.soprafs20.exceptions.SopraServiceException;
import ch.uzh.ifi.seal.soprafs20.repository.*;
import ch.uzh.ifi.seal.soprafs20.rest.dto.UserPostDTO;
import ch.uzh.ifi.seal.soprafs20.service.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@WebMvcTest(UserController.class)
public class UserControllerWithServiceTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private LobbyService lobbyService;
    @MockBean
    private UserService userService;
    @MockBean
    private PlayerService playerService;
    @MockBean
    private DeckService deckService;
    @MockBean
    private CardService cardService;
    @MockBean
    private GameService gameService;


    @MockBean
    private PlayerRepository playerRepository;
    @MockBean
    private LobbyRepository lobbyRepository;
    @MockBean
    private UserRepository userRepository;
    @MockBean
    private GameRepository gameRepository;
    @MockBean
    private StatsRepository statsRepository;


    @BeforeEach
    public void setup() {
        userService = new UserService(userRepository);
        lobbyService = new LobbyService(lobbyRepository, userService, playerService, deckService, cardService, gameService);
        playerService = new PlayerService(playerRepository);
        gameService = new GameService(gameRepository,statsRepository, userService);
        UserController uc = new UserController(userService, lobbyService, playerService, gameService);
        mockMvc = MockMvcBuilders.standaloneSetup(uc).build();
    }

    @Test
    public void givenUsers_whenGetUsers_thenReturnJsonArray() throws Exception {
        // given
        User user = new User();
        user.setUsername("firstname@lastname");
        user.setPassword("abc");
        user.setStatus(UserStatus.OFFLINE);
        user.setToken("1");

        List<User> allUsers = Collections.singletonList(user);

        given(userRepository.findAll()).willReturn(allUsers);
        given(userRepository.findByToken(user.getToken())).willReturn(user);

        // when
        MockHttpServletRequestBuilder getRequest = get("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Token", user.getToken());

        // then
        mockMvc.perform(getRequest).andExpect(status().isOk())
                .andDo(print())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].username", is(user.getUsername())))
                .andExpect(jsonPath("$[0].status", is(user.getStatus().toString())));
    }

    @Test
    public void createUser_validInput_userCreated() throws Exception {
        // given
        User user = new User();
        user.setId(1L);
        user.setUsername("testUsername");
        user.setToken("1");
        user.setPassword("pw");
        user.setStatus(UserStatus.OFFLINE);

        UserPostDTO userPostDTO = new UserPostDTO();
        userPostDTO.setUsername("testUsername");
        userPostDTO.setPassword("pw");

        given(userRepository.findByUsername(anyString())).willReturn(null);
        given(userRepository.save(ArgumentMatchers.any())).willReturn(user);
        Mockito.doNothing().when(userRepository).flush();

        // when/then -> do the request + validate the result
        MockHttpServletRequestBuilder postRequest = post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(userPostDTO));

        // then
        mockMvc.perform(postRequest)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(user.getId().intValue())))
                .andExpect(jsonPath("$.username", is(user.getUsername())))
                .andExpect(jsonPath("$.status", is(user.getStatus().toString())))
                .andExpect(header().string("Location", "/users/"+user.getId().intValue()))
        ;
    }

    /**
     * Tests getting user/{id}
     * Valid Input, returns User data
     */
    @Test
    public void getSpecificUser_validInput_userReturned() throws Exception {
        // given
        User user = new User();
        user.setId(1L);
        user.setUsername("testUsername");
        user.setToken("1");
        user.setPassword("pw");
        user.setStatus(UserStatus.ONLINE);

        given(userRepository.findByToken(anyString())).willReturn(user);
        given(userRepository.findById(1L)).willReturn(java.util.Optional.of(user));


        // make get Request to user with id
        MockHttpServletRequestBuilder getRequest = get("/users/" + user.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .header("Token",user.getId());

        // then
        mockMvc.perform(getRequest)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").exists())
                .andExpect(jsonPath("$.username", is(user.getUsername())))
                .andExpect(jsonPath("$.status", is(user.getStatus().toString())))
                .andExpect(jsonPath("$.birthday", is(nullValue())))
                .andDo(print());
    }

    /**
     * Tests put for user/{id}
     * Valid Input, returns no content
     */
    @Test
    public void putSpecificUser_validInput_noContent() throws Exception {
        // given a user
        User user = new User();
        user.setId(1L);
        user.setUsername("testUsername");
        user.setToken("1");
        user.setPassword("pw");
        user.setStatus(UserStatus.ONLINE);

        given(userRepository.findByToken(anyString())).willReturn(user);
        given(userRepository.findById(anyLong())).willReturn(Optional.of(user));
        given(userRepository.findByUsername(user.getUsername())).willReturn(user);


        // generate put Request
        MockHttpServletRequestBuilder putRequest = put("/users/" + user.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .header("token", user.getToken())
                .content(asJsonString(user));

        // then
        mockMvc.perform(putRequest)
                .andExpect(status().isNoContent())
                .andExpect(jsonPath("$").doesNotExist())
                .andDo(print());
    }

    /**
     * Tests Login
     * Should return OK Status and ID, Token of user
     */
    @Test
    public void loginUserTest() throws Exception {
        // given
        User user = new User();
        user.setId(1L);
        user.setUsername("testUsername");
        user.setToken("1");
        user.setPassword("pw");
        user.setStatus(UserStatus.ONLINE);

        UserPostDTO userPostDTO = new UserPostDTO();
        userPostDTO.setUsername("testUsername");
        userPostDTO.setPassword("pw");

        given(userRepository.findByUsername(ArgumentMatchers.any())).willReturn(user);


        // when/then -> do the request + validate the result
        MockHttpServletRequestBuilder putRequest = put("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(userPostDTO));

        // then
        mockMvc.perform(putRequest)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(user.getId().intValue())))
                .andExpect(jsonPath("$.token", is(user.getToken())))
                .andDo(print())
        ;
    }

    /**
     * Tests Get /userId/invitations
     * Should return 200 and list of inviting lobbies
     */
    @Test
    public void getInvitations_validInput_lobbiesReturned() throws Exception{
        // given
        User invitedUser = new User();
        invitedUser.setId(1L);
        invitedUser.setToken("1");
        invitedUser.setStatus(UserStatus.ONLINE);
        invitedUser.setUsername("Invited User");
        Lobby lobby = new Lobby();
        lobby.setId(2L);
        lobby.setLobbyName("Test Lobby");
        invitedUser.addInvitingLobby(lobby);

        given(userRepository.findByToken(anyString())).willReturn(invitedUser);
        given(userRepository.findById(anyLong())).willReturn(Optional.of(invitedUser));


        // when
        MockHttpServletRequestBuilder getRequest = get("/users/" + invitedUser.getId().toString() + "/invitations/")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Token", invitedUser.getToken());

        // then
        mockMvc.perform(getRequest).andExpect(status().isOk())
                .andDo(print())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(lobby.getId().intValue())))
                .andExpect(jsonPath("$[0].lobbyName", is(lobby.getLobbyName())));
    }

    /**
     * Helper Method to convert userPostDTO into a JSON string such that the input can be processed
     * Input will look like this: {"name": "Test User", "username": "testUsername"}
     * @param object - object to be mapped
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
