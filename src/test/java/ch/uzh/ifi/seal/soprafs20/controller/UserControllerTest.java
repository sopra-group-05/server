package ch.uzh.ifi.seal.soprafs20.controller;

import ch.uzh.ifi.seal.soprafs20.constant.LobbyStatus;
import ch.uzh.ifi.seal.soprafs20.constant.PlayerRole;
import ch.uzh.ifi.seal.soprafs20.constant.PlayerType;
import ch.uzh.ifi.seal.soprafs20.constant.UserStatus;
import ch.uzh.ifi.seal.soprafs20.entity.Lobby;
import ch.uzh.ifi.seal.soprafs20.entity.Player;
import ch.uzh.ifi.seal.soprafs20.entity.User;
import ch.uzh.ifi.seal.soprafs20.exceptions.NotFoundException;
import ch.uzh.ifi.seal.soprafs20.exceptions.SopraServiceException;
import ch.uzh.ifi.seal.soprafs20.exceptions.UnauthorizedException;
import ch.uzh.ifi.seal.soprafs20.rest.dto.UserPostDTO;
import ch.uzh.ifi.seal.soprafs20.service.GameService;
import ch.uzh.ifi.seal.soprafs20.service.LobbyService;
import ch.uzh.ifi.seal.soprafs20.service.PlayerService;
import ch.uzh.ifi.seal.soprafs20.service.UserService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
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
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * UserControllerTest
 * This is a WebMvcTest which allows to test the UserController i.e. GET/POST request without actually sending them over the network.
 * This tests if the UserController works.
 */
@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;
    @MockBean
    private LobbyService lobbyService;
    @MockBean
    private PlayerService playerService;
    @MockBean
    private GameService gameService;

    @Test
    void givenUsers_whenGetUsers_thenReturnJsonArray() throws Exception {
        // given
        User user = new User();
        user.setUsername("firstname@lastname");
        user.setPassword("abc");
        user.setStatus(UserStatus.OFFLINE);

        List<User> allUsers = Collections.singletonList(user);

        // this mocks the UserService -> we define above what the userService should return when getUsers() is called
        given(userService.getUsers()).willReturn(allUsers);
        given(userService.checkUserToken(Mockito.anyString())).willReturn(user);

        // when
        MockHttpServletRequestBuilder getRequest = get("/users").contentType(MediaType.APPLICATION_JSON);

        // then
        mockMvc.perform(getRequest).andExpect(status().isOk())
                .andDo(print())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].username", is(user.getUsername())))
                .andExpect(jsonPath("$[0].status", is(user.getStatus().toString())));
    }

    @Test
    void createUser_validInput_userCreated() throws Exception {
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

        given(userService.createUser(Mockito.any())).willReturn(user);

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
    void getSpecificUser_validInput_userReturned() throws Exception {
        // given
        User user = new User();
        user.setId(1L);
        user.setUsername("testUsername");
        user.setToken("1");
        user.setPassword("pw");
        user.setStatus(UserStatus.ONLINE);

        given(userService.checkUserToken(Mockito.anyString())).willReturn(user);
        given(userService.getUserByID(Mockito.anyLong())).willReturn(user);

        // make get Request to user with id
        MockHttpServletRequestBuilder getRequest = get("/users/" + user.getId()).contentType(MediaType.APPLICATION_JSON);

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
     * Tests getting user/{id}
     * User ID not found, returns 404 error
     */
    @Test
    void getSpecificUser_wrongInput_notFoundStatusReturned() throws Exception {
        // make get Request to user with id
        String exceptionMsg = "User was not found";
        MockHttpServletRequestBuilder getRequest = get("/users/100")
                .contentType(MediaType.APPLICATION_JSON)
                .header("token", 1);

        //given(userService.getUserByID(Mockito.anyLong())).willThrow(NotFoundException);
       given(userService.getUserByID(Mockito.anyLong())).willThrow(new NotFoundException(exceptionMsg));

        // then
        mockMvc.perform(getRequest)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$", is(exceptionMsg)))
                .andDo(print());
    }

    /**
     * Tests getting user/{id}
     * Valid UserID, but not allowed to view page (no Token / wrong token)
     */
    @Test
    void getSpecificUser_validInput_wrongToken_userReturned() throws Exception {
        // given
        User user = new User();
        user.setId(1L);
        user.setUsername("testUsername");
        user.setToken("1");
        user.setPassword("pw");
        user.setStatus(UserStatus.ONLINE);
        String exceptionMsg = "You are not allowed to access this page";

        given(userService.checkUserToken(Mockito.anyString())).willThrow(new UnauthorizedException(exceptionMsg));

        // make get Request to user with id
        MockHttpServletRequestBuilder getRequest = get("/users/" + user.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .header("Token", "wrongToken");

        // then
        mockMvc.perform(getRequest)
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$", is(exceptionMsg)))
                .andDo(print());
    }

    /**
     * Tests put for user/{id}
     * Valid Input, returns no content
     */
    @Test
    void putSpecificUser_validInput_noContent() throws Exception {
        // given a user
        User user = new User();
        user.setId(1L);
        user.setUsername("testUsername");
        user.setToken("1");
        user.setPassword("pw");
        user.setStatus(UserStatus.ONLINE);
        given(userService.updateUser(Mockito.any(), Mockito.anyString(), Mockito.anyLong())).willReturn(user);

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
     * Tests put for user/{id}
     * User Not Found
     */
    @Test
    void putSpecificUser_userNotFound_throwsNotFoundException() throws Exception {
        // given a user
        User user = new User();
        user.setId(1L);
        user.setUsername("testUsername");
        user.setToken("1");
        user.setPassword("pw");
        user.setStatus(UserStatus.ONLINE);

        // given updateUser throws a NotFound error and checkUserToken is valid
        String exceptionMsg = "User not found";
        given(userService.updateUser(Mockito.any(), Mockito.anyString(), Mockito.anyLong())).willThrow(new NotFoundException(exceptionMsg));
        given(userService.checkUserToken(Mockito.anyString())).willReturn(new User());

        // generate put Request
        MockHttpServletRequestBuilder putRequest = put("/users/2")
                .contentType(MediaType.APPLICATION_JSON)
                .header("token", user.getToken())
                .content(asJsonString(user));

        // then
        mockMvc.perform(putRequest)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$", is(exceptionMsg)))
                .andDo(print());
    }

    /**
     * Tests put for user/{id}
     * Wrong Token, returns unauthorized exception
     */
    @Test
    void putSpecificUser_wrongToken_unauthorizedException() throws Exception {
        // given a user
        String exceptionMsg = "You are not allowed to access this page";
        User user = new User();
        user.setId(1L);
        user.setUsername("testUsername");
        user.setToken("1");
        user.setPassword("pw");
        user.setStatus(UserStatus.ONLINE);
        given(userService.checkUserToken(Mockito.anyString())).willThrow(new UnauthorizedException(exceptionMsg));

        // generate put Request
        MockHttpServletRequestBuilder putRequest = put("/users/" + user.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .header("Token", user.getToken())
                .content(asJsonString(user));

        // then
        mockMvc.perform(putRequest)
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$", is(exceptionMsg)))
                .andDo(print());
    }

    /**
     * Tests Login
     * Should return OK Status and ID, Token of user
     */
    @Test
    void loginUserTest() throws Exception {
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

        given(userService.loginUser(Mockito.any())).willReturn(user);

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
     * Tests Login
     * Should return OK Status and ID, Token of user
     */
    @Test
    void loginUserTest_wrongPassword() throws Exception {
        // given
        String exceptionMsg = "Password is not correct";
        User user = new User();
        user.setId(1L);
        user.setUsername("testUsername");
        user.setToken("1");
        user.setPassword("pw");
        user.setStatus(UserStatus.ONLINE);

        UserPostDTO userPostDTO = new UserPostDTO();
        userPostDTO.setUsername("testUsername");
        userPostDTO.setPassword("pw22");

        given(userService.loginUser(Mockito.any())).willThrow(new UnauthorizedException(exceptionMsg));

        // when/then -> do the request + validate the result
        MockHttpServletRequestBuilder putRequest = put("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(userPostDTO));

        // then
        mockMvc.perform(putRequest)
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$", is(exceptionMsg)))
                .andDo(print())
        ;
    }

    /**
     * Tests Get /userId/invitations
     * Should return 200 and list of inviting lobbies
     */
    @Test
    void getInvitations_validInput_lobbiesReturned() throws Exception{
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

        given(userService.checkUserToken(anyString())).willReturn(invitedUser);
        given(userService.getUserByID(anyLong())).willReturn(invitedUser);

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
     * Tests Get /userId/invitations
     * Should return error with status 401
     */
    @Test
    void getInvitations_wrongToken_throwsUnauthorized() throws Exception{
        // given
        User invitedUser = new User();
        invitedUser.setId(1L);
        invitedUser.setToken("1");
        invitedUser.setStatus(UserStatus.ONLINE);
        invitedUser.setUsername("Invited User");

        given(userService.checkUserToken(anyString())).willThrow(new UnauthorizedException("You are not allowed to access this page"));

        // when
        MockHttpServletRequestBuilder getRequest = get("/users/" + invitedUser.getId().toString() + "/invitations/")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Token", invitedUser.getToken());

        // then
        mockMvc.perform(getRequest).andExpect(status().isUnauthorized())
                .andDo(print())
                .andExpect(jsonPath("$", is("You are not allowed to access this page")));

    }

    /**
     * Tests Get /userId/invitations
     * Should return error with 404
     */
    @Test
    void getInvitations_wrongId_throwsNotFound() throws Exception{
        // given
        User invitedUser = new User();
        invitedUser.setId(1L);
        invitedUser.setToken("1");
        invitedUser.setStatus(UserStatus.ONLINE);
        invitedUser.setUsername("Invited User");

        given(userService.checkUserToken(anyString())).willReturn(invitedUser);
        given(userService.getUserByID(anyLong())).willThrow(new NotFoundException("User was not found"));

        // when
        MockHttpServletRequestBuilder getRequest = get("/users/5/invitations/")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Token", invitedUser.getToken());

        // then
        mockMvc.perform(getRequest).andExpect(status().isNotFound())
                .andDo(print())
                .andExpect(jsonPath("$", is("User was not found")));
    }

    /**
     * Tests Get /userId/invitations
     * Should return error 403
     */
    @Test
    void getInvitations_wrongUserId_throwsForbidden() throws Exception{
        // given
        User invitedUser = new User();
        invitedUser.setId(1L);
        invitedUser.setToken("1");
        invitedUser.setStatus(UserStatus.ONLINE);
        invitedUser.setUsername("Invited User");
        User requestingUser = new User();
        requestingUser.setId(5L);
        requestingUser.setStatus(UserStatus.ONLINE);
        requestingUser.setUsername("Requesting User");
        requestingUser.setToken("5");


        given(userService.checkUserToken(anyString())).willReturn(requestingUser);
        given(userService.getUserByID(anyLong())).willReturn(invitedUser);

        // when
        MockHttpServletRequestBuilder getRequest = get("/users/" + invitedUser.getId().toString() + "/invitations/")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Token", requestingUser.getToken());

        // then
        mockMvc.perform(getRequest).andExpect(status().isForbidden())
                .andDo(print())
                .andExpect(jsonPath("$", is("Wrong user sent request!")));
    }

    /**
     * Tests PUT /userId/invitations/inviteId/accept
     * Valid input and returns 204
     */
    @Test
    void acceptInvitation_validInput_returnsNoContent() throws Exception{
        // given
        User invitedUser = new User();
        invitedUser.setId(1L);
        invitedUser.setToken("1");
        invitedUser.setStatus(UserStatus.ONLINE);
        invitedUser.setUsername("Invited User");
        Lobby lobby = new Lobby();
        lobby.setId(2L);
        lobby.setLobbyName("Inviting Lobby");
        lobby.setLobbyStatus(LobbyStatus.WAITING);
        invitedUser.addInvitingLobby(lobby);
        Player player = new Player();
        player.setId(1L);
        player.setRole(PlayerRole.CLUE_CREATOR);
        player.setPlayerType(PlayerType.HUMAN);
        Lobby lobbyAdded = new Lobby();
        lobbyAdded.setId(2L);
        lobbyAdded.setLobbyName("Inviting Lobby");
        lobbyAdded.setLobbyStatus(LobbyStatus.WAITING);
        lobbyAdded.addPlayer(player);

        given(userService.checkUserToken(anyString())).willReturn(invitedUser);
        given(userService.getUserByID(anyLong())).willReturn(invitedUser);
        given(lobbyService.getLobbyById(anyLong())).willReturn(lobby);
        given(playerService.convertUserToPlayer(ArgumentMatchers.any(), ArgumentMatchers.any())).willReturn(player);
        given(lobbyService.addPlayerToLobby(ArgumentMatchers.any(),ArgumentMatchers.any())).willReturn(lobbyAdded);
        doNothing().when(gameService).addStats(anyLong(),anyLong());
        doNothing().when(userService).removeFromInvitingLobbies(anyLong(),ArgumentMatchers.any());


        // when
        MockHttpServletRequestBuilder putRequest = put("/users/" + invitedUser.getId()
                + "/invitations/" + lobby.getId() + "/accept/")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Token", invitedUser.getToken());

        // then
        mockMvc.perform(putRequest).andExpect(status().isNoContent())
                .andDo(print())
                .andExpect(jsonPath("$").doesNotExist());
    }

    /**
     * Tests PUT /userId/invitations/inviteId/accept
     * Bad Token - throws 401
     */
    @Test
    void acceptInvitation_badToken_throwsUnauthorized() throws Exception{
        // given
        User invitedUser = new User();
        invitedUser.setId(1L);
        invitedUser.setToken("1");
        invitedUser.setStatus(UserStatus.ONLINE);
        invitedUser.setUsername("Invited User");

        given(userService.checkUserToken(anyString())).willThrow(new UnauthorizedException("You are not allowed to access this page"));


        // when
        MockHttpServletRequestBuilder putRequest = put("/users/" + invitedUser.getId()
                + "/invitations/" + 2L + "/accept/")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Token", "3");

        // then
        mockMvc.perform(putRequest).andExpect(status().isUnauthorized())
                .andDo(print())
                .andExpect(jsonPath("$",is("You are not allowed to access this page")));
    }

    /**
     * Tests PUT /userId/invitations/inviteId/accept
     * Non-existent userId - Throws 404
     */
    @Test
    void acceptInvitation_nonexistentUserId_throwsNotFound() throws Exception{
        // given
        User invitedUser = new User();
        invitedUser.setId(1L);
        invitedUser.setToken("1");
        invitedUser.setStatus(UserStatus.ONLINE);
        invitedUser.setUsername("Invited User");

        given(userService.checkUserToken(anyString())).willReturn(invitedUser);
        given(userService.getUserByID(anyLong())).willThrow(new NotFoundException("User was not found"));


        // when
        MockHttpServletRequestBuilder putRequest = put("/users/" + 3L
                + "/invitations/" + 2L + "/accept/")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Token", invitedUser.getToken());

        // then
        mockMvc.perform(putRequest).andExpect(status().isNotFound())
                .andDo(print())
                .andExpect(jsonPath("$", is("User was not found")));
    }

    /**
     * Tests PUT /userId/invitations/inviteId/accept
     * Wrong user requesting - throws 403
     */
    @Test
    void acceptInvitation_wrongUser_throwsForbidden() throws Exception{
        // given
        User invitedUser = new User();
        invitedUser.setId(1L);
        invitedUser.setToken("1");
        invitedUser.setStatus(UserStatus.ONLINE);
        invitedUser.setUsername("Invited User");
        User requestingUser = new User();
        requestingUser.setToken("3");
        requestingUser.setUsername("Requesting User");
        requestingUser.setId(3L);

        given(userService.checkUserToken(anyString())).willReturn(invitedUser);
        given(userService.getUserByID(anyLong())).willReturn(requestingUser);


        // when
        MockHttpServletRequestBuilder putRequest = put("/users/" + requestingUser.getId()
                + "/invitations/" + 2L + "/accept/")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Token", invitedUser.getToken());

        // then
        mockMvc.perform(putRequest).andExpect(status().isForbidden())
                .andDo(print())
                .andExpect(jsonPath("$",is("Wrong user sent request!")));
    }

    /**
     * Tests PUT /userId/invitations/inviteId/accept
     * Non-existent lobby - throw 404
     */
    @Test
    void acceptInvitation_nonexistentLobby_throwsNotFound() throws Exception{
        // given
        User invitedUser = new User();
        invitedUser.setId(1L);
        invitedUser.setToken("1");
        invitedUser.setStatus(UserStatus.ONLINE);
        invitedUser.setUsername("Invited User");

        given(userService.checkUserToken(anyString())).willReturn(invitedUser);
        given(userService.getUserByID(anyLong())).willReturn(invitedUser);
        given(lobbyService.getLobbyById(anyLong())).willThrow(new NotFoundException("The requested Lobby does not exist."));


        // when
        MockHttpServletRequestBuilder putRequest = put("/users/" + invitedUser.getId()
                + "/invitations/" + 2L + "/accept/")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Token", invitedUser.getToken());

        // then
        mockMvc.perform(putRequest).andExpect(status().isNotFound())
                .andDo(print())
                .andExpect(jsonPath("$",is("The requested Lobby does not exist.")));
    }

    /**
     * Tests PUT /userId/invitations/inviteId/accept
     * Lobby is not waiting - throws 409
     */
    @Test
    void acceptInvitation_lobbyIsNotWaiting_throwsConflict() throws Exception{
        // given
        User invitedUser = new User();
        invitedUser.setId(1L);
        invitedUser.setToken("1");
        invitedUser.setStatus(UserStatus.ONLINE);
        invitedUser.setUsername("Invited User");
        Lobby lobby = new Lobby();
        lobby.setId(2L);
        lobby.setLobbyName("Inviting Lobby");
        lobby.setLobbyStatus(LobbyStatus.RUNNING);

        given(userService.checkUserToken(invitedUser.getToken())).willReturn(invitedUser);
        given(userService.getUserByID(invitedUser.getId())).willReturn(invitedUser);
        given(lobbyService.getLobbyById(lobby.getId())).willReturn(lobby);


        // when
        MockHttpServletRequestBuilder putRequest = put("/users/" + invitedUser.getId()
                + "/invitations/" + lobby.getId() + "/accept/")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Token", invitedUser.getToken());

        // then
        mockMvc.perform(putRequest).andExpect(status().isConflict())
                .andDo(print())
                .andExpect(jsonPath("$",is("Lobby is already playing or has already stopped!")));
    }

    /**
     * Tests PUT /userId/invitations/inviteId/decline
     * Valid input and returns 204
     */
    @Test
    void declineInvitation_validInput_returnsNoContent() throws Exception{
        // given
        User invitedUser = new User();
        invitedUser.setId(1L);
        invitedUser.setToken("1");
        invitedUser.setStatus(UserStatus.ONLINE);
        invitedUser.setUsername("Invited User");
        Lobby lobby = new Lobby();
        lobby.setId(2L);
        lobby.setLobbyName("Inviting Lobby");
        invitedUser.addInvitingLobby(lobby);

        given(userService.checkUserToken(anyString())).willReturn(invitedUser);
        given(userService.getUserByID(anyLong())).willReturn(invitedUser);
        given(lobbyService.getLobbyById(anyLong())).willReturn(lobby);
        doNothing().when(userService).removeFromInvitingLobbies(anyLong(),ArgumentMatchers.any());


        // when
        MockHttpServletRequestBuilder putRequest = put("/users/" + invitedUser.getId()
                + "/invitations/" + lobby.getId() + "/decline/")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Token", invitedUser.getToken());

        // then
        mockMvc.perform(putRequest).andExpect(status().isNoContent())
                .andDo(print())
                .andExpect(jsonPath("$").doesNotExist());
    }

    /**
     * Tests PUT /userId/invitations/inviteId/decline
     * Bad Token - throws 401
     */
    @Test
    void declineInvitation_badToken_throwsUnauthorized() throws Exception{
        // given
        User invitedUser = new User();
        invitedUser.setId(1L);
        invitedUser.setToken("1");
        invitedUser.setStatus(UserStatus.ONLINE);
        invitedUser.setUsername("Invited User");

        given(userService.checkUserToken(anyString())).willThrow(new UnauthorizedException("You are not allowed to access this page"));


        // when
        MockHttpServletRequestBuilder putRequest = put("/users/" + invitedUser.getId()
                + "/invitations/" + 2L + "/decline/")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Token", "3");

        // then
        mockMvc.perform(putRequest).andExpect(status().isUnauthorized())
                .andDo(print())
                .andExpect(jsonPath("$",is("You are not allowed to access this page")));
    }

    /**
     * Tests PUT /userId/invitations/inviteId/decline
     * Non-existent userId - Throws 404
     */
    @Test
    void declineInvitation_nonexistentUserId_throwsNotFound() throws Exception{
        // given
        User invitedUser = new User();
        invitedUser.setId(1L);
        invitedUser.setToken("1");
        invitedUser.setStatus(UserStatus.ONLINE);
        invitedUser.setUsername("Invited User");

        given(userService.checkUserToken(anyString())).willReturn(invitedUser);
        given(userService.getUserByID(anyLong())).willThrow(new NotFoundException("User was not found"));


        // when
        MockHttpServletRequestBuilder putRequest = put("/users/" + 3L
                + "/invitations/" + 2L + "/decline/")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Token", invitedUser.getToken());

        // then
        mockMvc.perform(putRequest).andExpect(status().isNotFound())
                .andDo(print())
                .andExpect(jsonPath("$", is("User was not found")));
    }

    /**
     * Tests PUT /userId/invitations/inviteId/decline
     * Wrong user requesting - throws 403
     */
    @Test
    void declineInvitation_wrongUser_throwsForbidden() throws Exception{
        // given
        User invitedUser = new User();
        invitedUser.setId(1L);
        invitedUser.setToken("1");
        invitedUser.setStatus(UserStatus.ONLINE);
        invitedUser.setUsername("Invited User");
        User requestingUser = new User();
        requestingUser.setToken("3");
        requestingUser.setUsername("Requesting User");
        requestingUser.setId(3L);

        given(userService.checkUserToken(anyString())).willReturn(invitedUser);
        given(userService.getUserByID(anyLong())).willReturn(requestingUser);


        // when
        MockHttpServletRequestBuilder putRequest = put("/users/" + requestingUser.getId()
                + "/invitations/" + 2L + "/decline/")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Token", invitedUser.getToken());

        // then
        mockMvc.perform(putRequest).andExpect(status().isForbidden())
                .andDo(print())
                .andExpect(jsonPath("$",is("Wrong user sent request!")));
    }

    /**
     * Tests PUT /userId/invitations/inviteId/decline
     * Non-existent lobby - throw 404
     */
    @Test
    void declineInvitation_nonexistentLobby_throwsNotFound() throws Exception{
        // given
        User invitedUser = new User();
        invitedUser.setId(1L);
        invitedUser.setToken("1");
        invitedUser.setStatus(UserStatus.ONLINE);
        invitedUser.setUsername("Invited User");
        Lobby lobby = new Lobby();
        lobby.setId(2L);
        lobby.setLobbyName("Inviting Lobby");
        invitedUser.addInvitingLobby(lobby);

        given(userService.checkUserToken(anyString())).willReturn(invitedUser);
        given(userService.getUserByID(anyLong())).willReturn(invitedUser);
        given(lobbyService.getLobbyById(anyLong())).willThrow(new NotFoundException("The requested Lobby does not exist."));


        // when
        MockHttpServletRequestBuilder putRequest = put("/users/" + invitedUser.getId()
                + "/invitations/" + 2L + "/decline/")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Token", invitedUser.getToken());

        // then
        mockMvc.perform(putRequest).andExpect(status().isNotFound())
                .andDo(print())
                .andExpect(jsonPath("$",is("The requested Lobby does not exist.")));
    }

    /**
     * Tests PUT /userId/invitations/inviteId/decline
     * Player already in lobby - throws 409
     */
    @Test
    void declineInvitation_userAlreadyInLobby_throwsConflict() throws Exception{
        // given
        User invitedUser = new User();
        invitedUser.setId(1L);
        invitedUser.setToken("1");
        invitedUser.setStatus(UserStatus.ONLINE);
        invitedUser.setUsername("Invited User");
        Lobby lobby = new Lobby();
        lobby.setId(2L);
        lobby.setLobbyName("Inviting Lobby");
        invitedUser.addInvitingLobby(lobby);
        Player player = new Player(invitedUser);
        lobby.addPlayer(player);

        given(userService.checkUserToken(anyString())).willReturn(invitedUser);
        given(userService.getUserByID(anyLong())).willReturn(invitedUser);
        given(lobbyService.getLobbyById(anyLong())).willReturn(lobby);


        // when
        MockHttpServletRequestBuilder putRequest = put("/users/" + invitedUser.getId()
                + "/invitations/" + lobby.getId() + "/decline/")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Token", invitedUser.getToken());

        // then
        mockMvc.perform(putRequest).andExpect(status().isConflict())
                .andDo(print())
                .andExpect(jsonPath("$",is("User is already in the lobby!")));
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