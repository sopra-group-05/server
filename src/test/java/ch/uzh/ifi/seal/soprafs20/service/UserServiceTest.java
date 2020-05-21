package ch.uzh.ifi.seal.soprafs20.service;

import ch.uzh.ifi.seal.soprafs20.constant.UserStatus;
import ch.uzh.ifi.seal.soprafs20.entity.User;
import ch.uzh.ifi.seal.soprafs20.exceptions.*;
import ch.uzh.ifi.seal.soprafs20.repository.UserRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private User testUser;

    /**
     * Is called before each test
     * creates a new testUser to e used
     * userRepository.save will always return the dummy testUser
     */
    @BeforeEach
    public void setup() {
        MockitoAnnotations.initMocks(this);

        // given
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("username");
        testUser.setPassword("pw");

        // when -> any object is being save in the userRepository -> return the dummy testUser
        Mockito.when(userRepository.save(Mockito.any())).thenReturn(testUser);
    }

    /**
     * Checks create User function with correct input
     */
    @Test
    public void createUser_validInputs_success() {
        // when -> any object is being save in the userRepository -> return the dummy testUser
        User createdUser = userService.createUser(testUser);

        // then
        Mockito.verify(userRepository, Mockito.times(1)).save(Mockito.any());

        assertEquals(testUser.getId(), createdUser.getId());
        assertEquals(testUser.getUsername(), createdUser.getUsername());
        assertNotNull(createdUser.getToken());
        assertEquals(UserStatus.OFFLINE, createdUser.getStatus()); // since newly created users are saved as offline before login
        assertNull(createdUser.getBirthday());
        assertNotNull(createdUser.getCreated());
    }


    /**
     * Checks if correct Exception is thrown when trying to create a user with the same username twice
     */
    @Test
    public void createUser_duplicateUsername_throwsException() {
        // given -> a first user has already been created
        userService.createUser(testUser);
        List<User> testUserList = new ArrayList<>();
        testUserList.add(testUser);

        // when -> setup additional mocks for UserRepository
        Mockito.when(userRepository.findAll()).thenReturn(testUserList);

        // then -> attempt to create second user with same user -> check that an error is thrown
        String exceptionMessage = "The username provided is not unique. Therefore, the user could not be created!";
        ConflictException exception = assertThrows(ConflictException.class, () -> userService.createUser(testUser), exceptionMessage);
        assertEquals(exceptionMessage, exception.getMessage());
    }

    /**
     * Tries to get a user via ID and checks if correct NotFOundException is thrown for an invalid user
     */
    @Test
    public void getUserByID_throwsException() {
        // when trying to find user by id return null
        Mockito.when(userRepository.findById(Mockito.anyLong())).thenReturn(Optional.empty());

        // then -> attempt to find a user with an invalid id -> check that correct error is thrown
        String exceptionMessage = "User was not found";
        NotFoundException exception = assertThrows(NotFoundException.class, () -> userService.getUserByID(Mockito.anyLong()), exceptionMessage);
        assertEquals(exceptionMessage, exception.getMessage());
    }

    /**
     * Tries to get a user via ID and checks if that is the correct user
     */
    @Test
    public void getUserByID_validInput() {
        // when trying to find user, return user
        Mockito.when(userRepository.findById(Mockito.anyLong())).thenReturn(Optional.of(testUser));


        User foundUser = userService.getUserByID(testUser.getId());

        assertEquals(testUser.getId(), foundUser.getId());
        assertEquals(testUser.getUsername(), foundUser.getUsername());
    }

    /**
     * Tests login function with valid inputs (password and username)
     */
    @Test
    public void loginUser_validInputs_success() {
        // when trying to find user, return user
        Mockito.when(userRepository.findByUsername(Mockito.anyString())).thenReturn(testUser);

        // try login
        User loggedInUser = userService.loginUser(testUser);

        // asserts
        assertEquals(testUser.getId(), loggedInUser.getId());
        assertEquals(UserStatus.ONLINE, loggedInUser.getStatus()); // logged in user should be ONLINE
    }

    /**
     * Tests what happens when a wrong password is given
     */
    @Test
    public void loginUser_wrongPassword_throwsException() {
        // when trying to find user, return user
        Mockito.when(userRepository.findByUsername(Mockito.anyString())).thenReturn(testUser);

        // given a login attempt with the wrong password
        User testUserTwo = new User();
        testUserTwo.setId(1L);
        testUserTwo.setUsername("username");
        testUserTwo.setPassword("wrong-pw");

        // then -> attempt to login user with wrong password -> assert correct Exception
        String exceptionMessage = "Password is not correct";
        UnauthorizedException exception = assertThrows(UnauthorizedException.class, () -> userService.loginUser(testUserTwo), exceptionMessage);
        assertEquals(exceptionMessage, exception.getMessage());
    }

    /**
     * Tests what happens when you input a wrong username into the login function (user does not exist)
     */
    @Test
    public void loginUser_wrongUsername_throwsException() {
        // when trying to find user, return user
        Mockito.when(userRepository.findByUsername(Mockito.anyString())).thenReturn(null);

        // given a login attempt with the wrong password
        User testUserTwo = new User();
        testUserTwo.setId(1L);
        testUserTwo.setUsername("wrong-username");
        testUserTwo.setPassword("pw");

        // then -> attempt to login user with wrong password -> assert correct Exception
        String exceptionMessage = "User was not found";
        UnauthorizedException exception = assertThrows(UnauthorizedException.class, () -> userService.loginUser(testUserTwo), exceptionMessage);
        assertEquals(exceptionMessage, exception.getMessage());
    }

    /**
     * Tests if getUsers works correctly
     */
    @Test
    public void getUsers_success() {
        // when getting all users from userRepository, return list with testuser in it
        List<User> users = new ArrayList<>();
        users.add(testUser);
        Mockito.when(userRepository.findAll()).thenReturn(users);

        // then make request and compare if lists are the same
        List<User> foundUsers = userService.getUsers();
        assertEquals(foundUsers, users);
    }

    /**
     * Tests Update Profile function with valid inputs (username, birthday, token)
     */
    @Test
    public void updateUser_validInputs_success() {
        // when trying to find user via token or id, return user
        testUser.setToken("12345");
        testUser.setBirthday("24.11.1996");
        Mockito.when(userRepository.findById(Mockito.anyLong())).thenReturn(Optional.of(testUser));
        Mockito.when(userRepository.findByToken(Mockito.anyString())).thenReturn(testUser);

        // try changing username and birthday
        testUser.setUsername("flo");
        testUser.setBirthday("01.01.2020");
        User updatedUser = userService.updateUser(testUser, testUser.getToken(), testUser.getId());

        // asserts
        assertEquals(testUser.getId(), updatedUser.getId());
        assertEquals(testUser.getUsername(), updatedUser.getUsername());
        assertEquals(testUser.getBirthday(), updatedUser.getBirthday());
    }

    /**
     * Tests Update Profile function with wrong Token
     */
    @Test
    public void updateUser_wrongToken_throwsException() {
        // when trying to find user via token or id, return user.
        // But since token wrong, not for findByToken
        testUser.setToken("12345");
        testUser.setBirthday("24.11.1996");
        Mockito.when(userRepository.findById(Mockito.anyLong())).thenReturn(Optional.of(testUser));
        Mockito.when(userRepository.findByToken(Mockito.anyString())).thenReturn(null);

        // try changing username and birthday
        testUser.setUsername("flo");
        testUser.setBirthday("01.01.2020");

        String exceptionMessage = "Token does not belong to any user";
        UnauthorizedException exception = assertThrows(UnauthorizedException.class, () -> userService.updateUser(testUser, Mockito.anyString(), testUser.getId()), exceptionMessage);
        assertEquals(exceptionMessage, exception.getMessage());
    }

    /**
     * Tests Update Profile function with correct token,
     * but token does not belong to the user that should be changed
     */
    @Test
    public void updateUser_wrongID_throwsException() {
        // when trying to find user via token or id, return user
        User testUserTwo = new User();
        testUserTwo.setId(2L);
        testUserTwo.setUsername("TestUserTwo");
        testUserTwo.setPassword("pw");
        testUserTwo.setToken("12345");
        testUserTwo.setBirthday("24.11.1996");

        testUser.setToken("12345");
        testUser.setBirthday("24.11.1996");
        Mockito.when(userRepository.findById(Mockito.anyLong())).thenReturn(Optional.of(testUserTwo));
        Mockito.when(userRepository.findByToken(Mockito.anyString())).thenReturn(testUser);

        // try changing username and birthday
        testUser.setUsername("flo");
        testUser.setBirthday("01.01.2020");

        String exceptionMessage = "You're not supposed to edit this user Profile";
        UnauthorizedException exception = assertThrows(UnauthorizedException.class, () -> userService.updateUser(testUser, testUser.getToken(), testUser.getId()), exceptionMessage);
        assertEquals(exceptionMessage, exception.getMessage());
    }

    @Test
    public void authenticateUsertoDeleteWrongPasswordTest() {
        testUser.setToken("12345");

        User testUser2 = new User();
        testUser2.setPassword("wrong");

        Mockito.when(userRepository.findById(Mockito.anyLong())).thenReturn(Optional.of(testUser));
        Mockito.when(userRepository.findByToken(Mockito.any())).thenReturn(testUser);
        String exceptionMessage = "Wrong Password";
        ConflictException exception = assertThrows(ConflictException.class, () -> userService.authenticateDeletion(1L, "12345", testUser2), exceptionMessage);
        assertEquals(exceptionMessage, exception.getMessage());
    }

    @Test
    public void authenticateUsertoDeleteWrongUserTokenTest() {
        testUser.setToken("12345");

        User testUser2 = new User();
        testUser2.setToken("wrong");

        Mockito.when(userRepository.findById(Mockito.anyLong())).thenReturn(Optional.of(testUser));
        Mockito.when(userRepository.findByToken(Mockito.any())).thenReturn(testUser2);
        String exceptionMessage = "You're not supposed to delete this user Profile";
        ForbiddenException exception = assertThrows(ForbiddenException.class, () -> userService.authenticateDeletion(1L, "wrong", testUser), exceptionMessage);
        assertEquals(exceptionMessage, exception.getMessage());
    }

    @Test
    public void authenticateUsertoDeleteWrongUserId() {
        testUser.setToken("12345");

        Mockito.when(userRepository.findById(Mockito.anyLong())).thenReturn(Optional.empty());
        Mockito.when(userRepository.findByToken(Mockito.any())).thenReturn(testUser);
        String exceptionMessage = "The provided User ID does not belong to any user";
        NotFoundException exception = assertThrows(NotFoundException.class, () -> userService.authenticateDeletion(2L, "12345", testUser), exceptionMessage);
        assertEquals(exceptionMessage, exception.getMessage());
    }

}
