package ch.uzh.ifi.seal.soprafs20.service;

import ch.uzh.ifi.seal.soprafs20.constant.UserStatus;
import ch.uzh.ifi.seal.soprafs20.entity.User;
import ch.uzh.ifi.seal.soprafs20.exceptions.*;
import ch.uzh.ifi.seal.soprafs20.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * User Service
 * This class is the "worker" and responsible for all functionality related to the user
 * (e.g., it creates, modifies, deletes, finds). The result will be passed back to the caller.
 */
@Service
@Transactional
public class UserService {

    private final Logger log = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;

    @Autowired
    public UserService(@Qualifier("userRepository") UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     *
     * @return A List of all Users
     */
    public List<User> getUsers() {
        return this.userRepository.findAll();
    }

    /**
     * Adds a New User to the userRepository if it doesn't already exist (by username)
     * @param newUser
     * @return USer
     */
    public User createUser(User newUser) {
        newUser.setToken(UUID.randomUUID().toString());
        newUser.setStatus(UserStatus.OFFLINE); // user will be set Online when Login in and to offline when logging out

        checkIfUserExists(newUser);

        // saves the given entity but data is only persisted in the database once flush() is called
        newUser = userRepository.save(newUser);
        userRepository.flush();

        log.debug("Created Information for User: {}", newUser);
        return newUser;
    }

    /**
     * Main Goal: Will Update the Info of a User (Profile)
     * First it Checks the Token (Does it belong to any User? Does the token belong to the user you're trying to edit?)
     * Checks the User ID (Does it even exist?)
     * Changes Username and birthday if it is different than the one in the database
     * @param user
     * @return User
     */
    public User updateUser(User user, String token, long id) {
        User userByToken = userRepository.findByToken(token);
        User userById = this.getUserByID(id);
        if (null == userByToken) {
            // authenticate token => not authenticated
            throw new UnauthorizedException("Token does not belong to any user");
        } else if(null == userById) {
            // Profile does not exist
            throw new NotFoundException("The provided User ID does not belong to any user");
        }

        if (!userById.equals(userByToken)) {
            // UserProfile should be the one from the User with the token
            throw new UnauthorizedException("You're not supposed to edit this user Profile");
        }

        if (!userByToken.getUsername().equals(user.getUsername())) {
            // username different. Try to change, if already in use throw exception
            if (null == userRepository.findByUsername(user.getUsername())) {
                userByToken.setUsername(user.getUsername());
            } else {
                throw new ConflictException("Username already in use!");
            }
        }

        // Change Birthday if edited
        if(((userByToken.getBirthday() == null) ^ (user.getBirthday() == null))
                || ((userByToken.getBirthday() != null
                && user.getBirthday() != null
                && !userByToken.getBirthday().equals(user.getBirthday())))
        ) {
            userByToken.setBirthday(user.getBirthday());
        }

        return userByToken;
    }

    /**
     * Finds User by Token and set their Status to Offline
     * @param user
     * @return User
     */
    public User logoutUser(User user) {
        User userByToken = userRepository.findByToken(user.getToken());
        if (null == userByToken) {
            throw new ConflictException("Token does not belong to any user");
        }
        userByToken.setStatus(UserStatus.OFFLINE);

        return userByToken;
    }

    /**
     * Checks if username and password are correct, sets User Status to Online and then returns that User
     * @param user
     * @return User
     */
    public User loginUser(User user) {
        // find user
        User userByUsername = userRepository.findByUsername(user.getUsername());
        if (userByUsername == null) {
            // user was not found
            throw new UnauthorizedException("User was not found");
        }

        // check if password is correct
        if (user.getPassword().equals(userByUsername.getPassword())) {
            userByUsername.setStatus(UserStatus.ONLINE);
            return userByUsername;
        } else {
            // throw exception if password wrong
            throw new UnauthorizedException("Password is not correct");
        }
    }

    /**
     * Finds and returns a User by ID
     * @param id
     * @return User
     */
    public User getUserByID(long id) {
        User userById = userRepository.findById(id);
        if (userById == null) {
            // user was not found
            throw new NotFoundException("User was not found");
        }
        return userById;
    }
    /*
    Delete User
    * @param token
    * @param id
    */
    public User authenticateDeletion(long id , String token, User toDeleteUser) {
        User userByToken = userRepository.findByToken(token);
        User userById = userRepository.findById(id);

        if (null == userById){
            // Profile does not exist
            throw new NotFoundException("The provided User ID does not belong to any user");
        }

        if (!userById.equals(userByToken)) {
            // UserProfile should be the one from the User with the token
            throw new ForbiddenException("You're not supposed to delete this user Profile");
        }  else if (!toDeleteUser.getPassword().equals(userById.getPassword())){
            //password should correct
            throw new ConflictException("Wrong Password");
        }

        return userByToken;
    }

    public void deleteUser(User user){
        userRepository.delete(user);
        userRepository.flush();
        //todo: check if flsuh needed
    }

    /**
     * This is a helper method that will check the uniqueness criteria of the username
     * defined in the User entity. The method will do nothing if the input is unique and throw an error otherwise.
     *
     * @param userToBeCreated
     * @throws ConflictException
     * @see User
     */
    private void checkIfUserExists(User userToBeCreated) {
        User userByUsername = userRepository.findByUsername(userToBeCreated.getUsername());
        if (userByUsername != null) {
            throw new ConflictException("The username provided is not unique. Therefore, the user could not be created!");
        }
    }

    public User checkUserToken(String token) {
        User userByToken = userRepository.findByToken(token);
        if (userByToken == null) {
            throw new UnauthorizedException("You are not allowed to access this page");
        }
        return userByToken;
    }

    /**
     * This method will add the achieved InGame Points of a Player to the Points balance of a User
     *
     * @param user,points - the to be updated User and the Points he earned at the time of the function call
     *
     * @return the new Balance of the Points of a User
     */
    public int updatePoints(User user, int points){
        return user.addPoints(points);
    }

}
