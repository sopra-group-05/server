package ch.uzh.ifi.seal.soprafs20.repository;

import ch.uzh.ifi.seal.soprafs20.constant.*;
import ch.uzh.ifi.seal.soprafs20.entity.Lobby;
import ch.uzh.ifi.seal.soprafs20.entity.Player;
import ch.uzh.ifi.seal.soprafs20.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class LobbyRepositoryIntegrationTest {

    @Autowired
    private TestEntityManager entityManager;

    @Qualifier("lobbyRepository")
    @Autowired
    private LobbyRepository lobbyRepository;


    /**
     * Tests the find ByUsername Method.
     * Should succeed
     */
    @Test
    void findByLobbyName_success() {
        // given
        Lobby lobby = createLobby("Testcase_Lobby");

        entityManager.persist(lobby);
        entityManager.flush();

        // when
        Lobby found = lobbyRepository.findByLobbyName(lobby.getLobbyName());

        // then
        assertNotNull(found);
        assertNotNull(found.getId());
        assertEquals(found.getLobbyName(), lobby.getLobbyName());
    }

    /**
     * Tests the findByLobbyname Method.
     * Should not find lobby and assertNull
     */
    @Test
    void findByLobbyName_fail() {
        // given
        Lobby lobby = createLobby("Testcase_Lobby");

        entityManager.persist(lobby);
        entityManager.flush();

        // when
        Lobby found = lobbyRepository.findByLobbyName("Testcase_lobbynotfound");

        // then
        assertNull(found);
    }

    /**
     * Tests find Lobby By Token method
     * Should find correct lobby
     */
    @Test
    void findByCreator_success() {
        User user = createUser("testUser", "1");
        Player player = new Player(user);
        player.setRole(PlayerRole.GUESSER);
        entityManager.persist(player);
        // given
        Lobby lobby = createLobby("Testcase_Lobby");

        lobby.setCreator(player);
        entityManager.persist(lobby);
        entityManager.flush();

        // when
        Lobby found = lobbyRepository.findByCreator(player);

        // then
        assertNotNull(found);
        assertNotNull(found.getId());
        assertEquals(found.getLobbyName(), lobby.getLobbyName());
    }


    /**
     * Tests find Lobby By Creator method
     * Should not find lobby and assertNull
     */
    @Test
    void findByCreator_fail() {
        User user = createUser("testUser", "1");
        Player player = new Player(user);
        player.setRole(PlayerRole.GUESSER);
        entityManager.persist(player);
        // given
        Lobby lobby = createLobby("Testcase_Lobby");

        lobby.setCreator(player);
        entityManager.persist(lobby);
        entityManager.flush();

        user = createUser("testUser2", "2");
        player = new Player(user);
        player.setRole(PlayerRole.CLUE_CREATOR);
        entityManager.persist(player);

        // when
        Lobby found = lobbyRepository.findByCreator(player);

        // then
        assertNull(found);
    }

    /**
     * Tests find Lobby By Token method
     * Should not find lobby and assertNull
     */
    @Test
    void findById_fail() {
        // given
        Lobby lobby = createLobby("Testcase_Lobby");


        entityManager.persist(lobby);
        entityManager.flush();

        // when
        Lobby found = lobbyRepository.findByLobbyId(5L);

        // then
        assertNull(found);
    }

    private User createUser(String userName, String token) {
        User user = new User();
        user.setId(Long.valueOf(token));
        user.setUsername(userName);
        user.setToken(token);
        user.setStatus(UserStatus.OFFLINE);
        return user;
    }

    private Lobby createLobby(String lobbyName) {

        Lobby lobby = new Lobby();
        lobby.setLobbyName(lobbyName);
        lobby.setGameMode(GameModeStatus.HUMANS);
        lobby.setLanguage(Language.DE);
        lobby.setLobbyStatus(LobbyStatus.WAITING);
        return lobby;
    }

}
