package ch.uzh.ifi.seal.soprafs20.rest.mapper;

import ch.uzh.ifi.seal.soprafs20.constant.GameModeStatus;
import ch.uzh.ifi.seal.soprafs20.constant.LobbyStatus;
import ch.uzh.ifi.seal.soprafs20.constant.UserStatus;
import ch.uzh.ifi.seal.soprafs20.entity.Lobby;
import ch.uzh.ifi.seal.soprafs20.entity.User;
import ch.uzh.ifi.seal.soprafs20.rest.dto.*;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * DTOMapperTest
 * Tests if the mapping between the internal and the external/API representation works.
 */
public class DTOMapperTest {
    @Test
    public void testCreateUser_fromUserPostDTO_toUser_success() {
        // create UserPostDTO
        UserPostDTO userPostDTO = new UserPostDTO();
        userPostDTO.setUsername("username");
        userPostDTO.setPassword("pw");

        // MAP -> Create user
        User user = DTOMapper.INSTANCE.convertUserPostDTOtoEntity(userPostDTO);

        // check content
        assertEquals(userPostDTO.getUsername(), user.getUsername());
        assertEquals(userPostDTO.getPassword(), user.getPassword());
    }

    @Test
    public void testGetUser_fromUser_toUserGetDTO_success() {
        // create User
        User user = new User();
        user.setUsername("firstname@lastname");
        user.setStatus(UserStatus.OFFLINE);
        user.setToken("1");
        user.setBirthday("22.12.2000");
        user.setCreated(LocalDateTime.now());

        // MAP -> Create UserGetDTO
        UserGetDTO userGetDTO = DTOMapper.INSTANCE.convertEntityToUserGetDTO(user);

        // check content
        assertEquals(user.getId(), userGetDTO.getId());
        assertEquals(user.getUsername(), userGetDTO.getUsername());
        assertEquals(user.getStatus(), userGetDTO.getStatus());
        assertEquals(user.getBirthday(), userGetDTO.getBirthday());
        assertEquals(user.getCreated(), userGetDTO.getCreated());
    }

    @Test
    public void testLoginGetUser_fromUser_toUserGetDTO_success() {
        // create User
        User user = new User();
        user.setToken("1");

        // MAP -> Create UserGetDTO
        UserLoginGetDTO userLoginGetDTO = DTOMapper.INSTANCE.convertEntityOfLoggedInUserGetDTO(user);

        // check content
        assertEquals(user.getId(), userLoginGetDTO.getId());
        assertEquals(user.getToken(), userLoginGetDTO.getToken());
    }

    @Test
    public void testPutUser_fromUser_toUserPutDTO_success() {
        // create User
        UserPutDTO userPutDTO = new UserPutDTO();
        userPutDTO.setUsername("firstname@lastname");
        userPutDTO.setToken("1");
        userPutDTO.setBirthday("22.12.2000");
        userPutDTO.setId(999);

        // MAP -> Create UserGetDTO
        User user = DTOMapper.INSTANCE.convertUserPutDTOtoEntity(userPutDTO);

        // check content
        assertEquals(user.getId(), userPutDTO.getId());
        assertEquals(user.getUsername(), userPutDTO.getUsername());
        assertEquals(user.getBirthday(), userPutDTO.getBirthday());
        assertEquals(user.getToken(), userPutDTO.getToken());
    }

    @Test
    public void testCreateLobby_fromUserPostDTO_toLobby_success() {
        // create LobbyPostDTO
        LobbyPostDTO lobbyPostDTO = new LobbyPostDTO();
        lobbyPostDTO.setLobbyName("test-lobby");
        lobbyPostDTO.setGameMode(0);

        // MAP -> Create user
        Lobby lobby = DTOMapper.INSTANCE.convertLobbyPostDTOtoEntity(lobbyPostDTO);

        // check content
        assertEquals(lobbyPostDTO.getLobbyName(), lobby.getLobbyName());
        assertEquals(GameModeStatus.HUMANS, lobby.getGameMode());
    }
}
