package ch.uzh.ifi.seal.soprafs20.repository;

import ch.uzh.ifi.seal.soprafs20.constant.UserStatus;
import ch.uzh.ifi.seal.soprafs20.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
public class UserRepositoryIntegrationTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;


    /**
     * Tests the find ByUsername Method.
     * Should succeed
     */
    @Test
    public void findByUsername_success() {
        // given
        User user = new User();
        user.setUsername("firstname@lastname");
        user.setPassword("pw");
        user.setStatus(UserStatus.OFFLINE);
        user.setToken("1");

        entityManager.persist(user);
        entityManager.flush();

        // when
        User found = userRepository.findByUsername(user.getUsername());

        // then
        assertNotNull(found);
        assertNotNull(found.getId());
        assertEquals(found.getUsername(), user.getUsername());
        assertEquals(found.getToken(), user.getToken());
        assertEquals(found.getStatus(), user.getStatus());
        assertEquals(found.getPassword(), user.getPassword());
    }

    /**
     * Tests the findByUsername Method.
     * Should not find user and assertNull
     */
    @Test
    public void findByUsername_fail() {
        // given a user
        User user = new User();
        user.setUsername("firstname@lastname");
        user.setPassword("pw");
        user.setStatus(UserStatus.OFFLINE);
        user.setToken("1");

        entityManager.persist(user);
        entityManager.flush();

        // when
        User found = userRepository.findByUsername("testname");

        // then
        assertNull(found);
    }

    /**
     * Tests find User By Token method
     * Should find correct user
     */
    @Test
    public void findByToken_success() {
        // given
        User user = new User();
        user.setUsername("firstname@lastname");
        user.setPassword("pw");
        user.setStatus(UserStatus.OFFLINE);
        user.setToken("1");

        entityManager.persist(user);
        entityManager.flush();

        // when
        User found = userRepository.findByToken(user.getToken());

        // then
        assertNotNull(found);
        assertNotNull(found.getId());
        assertEquals(found.getUsername(), user.getUsername());
        assertEquals(found.getToken(), user.getToken());
        assertEquals(found.getStatus(), user.getStatus());
        assertEquals(found.getPassword(), user.getPassword());
    }

    /**
     * Tests find User By Token method
     * Should not find user and assertNull
     */
    @Test
    public void findByToken_fail() {
        // given a user
        User user = new User();
        user.setUsername("firstname@lastname");
        user.setPassword("pw");
        user.setStatus(UserStatus.OFFLINE);
        user.setToken("1");

        entityManager.persist(user);
        entityManager.flush();

        // when
        User found = userRepository.findByToken("2");

        // then
        assertNull(found);
    }

    /**
     * Tests find User By Token method
     * Should not find user and assertNull
     */
    @Test
    public void findById_fail() {
        // given a user
        User user = new User();
        user.setUsername("firstname@lastname");
        user.setPassword("pw");
        user.setStatus(UserStatus.OFFLINE);
        user.setToken("1");

        entityManager.persist(user);
        entityManager.flush();

        // when
        User found = userRepository.findById(5);

        // then
        assertNull(found);
    }
}
