package ch.uzh.ifi.seal.soprafs20.controller;

import ch.uzh.ifi.seal.soprafs20.constant.RankingOrderBy;
import ch.uzh.ifi.seal.soprafs20.entity.Lobby;
import ch.uzh.ifi.seal.soprafs20.entity.User;
import ch.uzh.ifi.seal.soprafs20.exceptions.ForbiddenException;
import ch.uzh.ifi.seal.soprafs20.exceptions.UnauthorizedException;
import ch.uzh.ifi.seal.soprafs20.rest.dto.*;
import ch.uzh.ifi.seal.soprafs20.rest.mapper.DTOMapper;
import ch.uzh.ifi.seal.soprafs20.service.LobbyService;
import ch.uzh.ifi.seal.soprafs20.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
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
public class UserController {

    private final UserService userService;
    private final LobbyService lobbyService;

    @Autowired
    UserController(UserService userService, LobbyService lobbyService) {
        this.userService = userService;
        this.lobbyService = lobbyService;
    }

    /**
     *
     * @return List of all created Users
     */
    @GetMapping("/users")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public List<UserGetDTO> getAllUsers(@RequestHeader(name = "Token", required = false) String token) {
        userService.checkUserToken(token);

        // fetch all users in the internal representation
        List<User> users = userService.getUsers();
        List<UserGetDTO> userGetDTOs = new ArrayList<>();

        // convert each user to the API representation
        for (User user : users) {
            userGetDTOs.add(DTOMapper.INSTANCE.convertEntityToUserGetDTO(user));
        }
        return userGetDTOs;
    }

    /**
     * Get Profile of One User
     * @param userID
     * @return Status Code 200 "OK" with Profile Data of User (id, username, creation_date, status, birthday)
     */
    @GetMapping("/users/{userID}")
    @ResponseBody
    public ResponseEntity getUserByID(@RequestHeader(name = "Token", required = false) String token, @PathVariable long userID) {

        userService.checkUserToken(token);

        // find user for Profile
        User user = userService.getUserByID(userID);

        // convert internal representation of user back to API
        UserGetDTO userGetDTO = DTOMapper.INSTANCE.convertEntityToUserGetDTO(user);

        return ResponseEntity.ok(userGetDTO);

    }

    /**
     * Update Profile of User (username and birthday)
     * @param userPutDTO
     */
    @PutMapping("/users/{userId}")
    @ResponseBody
    public ResponseEntity updateUser(@RequestHeader(name = "Token", required = false) String token, @PathVariable long userId,  @RequestBody UserPutDTO userPutDTO) {
        userService.checkUserToken(token);

        // convert API input to internal representation
        User userInput = DTOMapper.INSTANCE.convertUserPutDTOtoEntity(userPutDTO);

        // update user in database
        userService.updateUser(userInput, token, userId);

        return ResponseEntity.noContent().build(); // status code 204 noContent
    }

    /**
     * Delete Profile of User (username and birthday)
     * @param
     */
    @DeleteMapping("/users/{userId}")
    @ResponseBody
    public ResponseEntity deleteUser(@RequestHeader(name = "Token", required = false) String token, @PathVariable long userId, @RequestBody UserDeleteDTO userDeleteDTO) {
        userService.checkUserToken(token);

        User toDeleteUser = DTOMapper.INSTANCE.convertUserDeleteDTOToEntity(userDeleteDTO);
        // delete User
        User user = userService.authenticateDeletion(userId, token, toDeleteUser);
        lobbyService.removeFromLobbyAndDeletePlayer(user);
        userService.deleteUser(user);

        return ResponseEntity.noContent().build(); // status code 204 noContent
    }

    /**
     * Set internal Status of User to Logout
     * @param userPutDTO
     */
    @PutMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT) // Status Code 204
    @ResponseBody
    public void logoutUser(@RequestBody UserPutDTO userPutDTO) {
        // convert API input to internal representation
        User userInput = DTOMapper.INSTANCE.convertUserPutDTOtoEntity(userPutDTO);

        // update user in database
        userService.logoutUser(userInput);
    }

    /**
     * REGISTER a new User
     * @param userPostDTO
     * @return Status Code 201 and the Location of the newly created User (/users/{id}) and the created user info (without token)
     */
    @PostMapping("/users")
    @ResponseBody
    public ResponseEntity createUser(@RequestBody UserPostDTO userPostDTO) {
        // convert API user to internal representation
        User userInput = DTOMapper.INSTANCE.convertUserPostDTOtoEntity(userPostDTO);

        // create user
        User createdUser = userService.createUser(userInput);

        // build location header
        URI location = UriComponentsBuilder.newInstance().path("/users/" + createdUser.getId()).build().toUri();

        // convert internal representation of user back to API
        UserGetDTO userGetDTO = DTOMapper.INSTANCE.convertEntityToUserGetDTO(createdUser);

        // return with status code 201 created the Location and user object
        return ResponseEntity.created(location).body(userGetDTO);
    }

    /**
     * LOGIN User
     * @param userPostDTO
     * @return Status Code 200 "OK" with User and its token to save in the frontend
     */
    @PutMapping("/login")
    @ResponseBody
    public ResponseEntity loginUser(@RequestBody UserPostDTO userPostDTO) {
        // convert API user to internal representation
        User userInput = DTOMapper.INSTANCE.convertUserPostDTOtoEntity(userPostDTO);

        // validate password, store User if user found and password correct
        User validatedUser = userService.loginUser(userInput);

        // convert internal representation of user back to API
        UserLoginGetDTO userLoginGetDTO =  DTOMapper.INSTANCE.convertEntityOfLoggedInUserGetDTO(validatedUser);

        // Return Status Code 200 OK with User and Token (!)
        return ResponseEntity.ok(userLoginGetDTO);
    }

    /**
     * Overall Game ranking for all users
     *
     */
    @GetMapping("/users/ranking/{orderBy}")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public List<OverallRankDTO> getOverallPlayerRanking(@RequestHeader(name = "Token", required = false) String token, @PathVariable String orderBy) {
        User user = userService.checkUserToken(token);

        List<User> users = userService.getAllUsersOrderBy(RankingOrderBy.valueOf(orderBy));

        List<OverallRankDTO> result = new ArrayList<>();
        for(User userRank : users) {
            result.add(DTOMapper.INSTANCE.convertEntityToOverallRankDTO(userRank));
        }

        return result;
    }

    @GetMapping("/users/{userId}/invitations")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public List<LobbyGetDTO> getInvitations(@RequestHeader(name = "Token", required = false) String token, @PathVariable long userId){
        // check that token belongs to userId
        User tokenUser = userService.checkUserToken(token); // can throw 401
        User idUser = userService.getUserByID(userId); // can throw 404
        // return a 403 Forbidden
        if (!tokenUser.equals(idUser))
            throw new ForbiddenException("Wrong user sent request!");

        // get invitingLobbies
        Set<Lobby> lobbies = idUser.getInvitingLobbies();
        // convert to LobbyGetDTO
        List<LobbyGetDTO> dtoLobbies = new ArrayList<>();
        for (Lobby lobby: lobbies) {
            dtoLobbies.add(DTOMapper.INSTANCE.convertEntityToLobbyGetDTO(lobby));
        }
        // send 200 with list of inviting lobbies
        return dtoLobbies;
    }

    @PutMapping("/users/{userId}/invitations/{lobbyId}/accept")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @ResponseBody
    public void acceptInvitation(@RequestHeader(name = "Token", required = false) String token, @PathVariable long userId, @PathVariable long lobbyId) {
        // check that token belongs to userId
        User tokenUser = userService.checkUserToken(token); // can throw 401
        User idUser = userService.getUserByID(userId); // can throw 404
        // return a 403 Forbidden
        if (!tokenUser.equals(idUser))
            throw new ForbiddenException("Wrong user sent request!");
        // TODO: 15/05/2020
        /*
        204	Accept invite of user(userId) to lobby(lobbyId)
        404 lobby not found
        409	Error	Conflict: Lobby is already playing
        */
    }

    @PutMapping("/users/{userId}/invitations/{lobbyId}/decline")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void declineInvitation(@RequestHeader(name = "Token", required = false) String token, @PathVariable long userId, @PathVariable long lobbyId){
        // check that token belongs to userId
        User tokenUser = userService.checkUserToken(token); // can throw 401
        User idUser = userService.getUserByID(userId); // can throw 404
        // return a 403 Forbidden
        if (!tokenUser.equals(idUser))
            throw new ForbiddenException("Wrong user sent request!");
        // TODO: 15/05/2020
        /*
        204	-	Decline invite of user(userId) to lobby(lobbyId)
        401	Error	Unauthorized (Invalid Token)
        404 not found lobby not found
        409	Error	Conflict: User already in this lobby
        */
    }

}
