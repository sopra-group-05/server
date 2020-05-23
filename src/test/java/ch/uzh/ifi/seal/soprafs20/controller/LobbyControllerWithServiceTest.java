package ch.uzh.ifi.seal.soprafs20.controller;

import ch.uzh.ifi.seal.soprafs20.constant.GameModeStatus;
import ch.uzh.ifi.seal.soprafs20.constant.LobbyStatus;
import ch.uzh.ifi.seal.soprafs20.constant.PlayerRole;
import ch.uzh.ifi.seal.soprafs20.constant.UserStatus;
import ch.uzh.ifi.seal.soprafs20.entity.*;
import ch.uzh.ifi.seal.soprafs20.repository.*;
import ch.uzh.ifi.seal.soprafs20.service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.AdditionalAnswers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.util.Assert;

import java.util.Optional;

import static org.hamcrest.Matchers.is;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(LobbyController.class)
class LobbyControllerWithServiceTest {


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
    private MysteryWordService mysteryWordService;

    @MockBean
    private PlayerRepository playerRepository;
    @MockBean
    private LobbyRepository lobbyRepository;
    @MockBean
    private UserRepository userRepository;
    @MockBean
    private DeckRepository deckRepository;
    @MockBean
    private CardRepository cardRepository;
    @MockBean
    private GameRepository gameRepository;
    @MockBean
    private StatsRepository statsRepository;
    @MockBean
    private MysteryWordRepository mysteryWordRepository;
    @MockBean
    private ClueService clueService;

    @BeforeEach
    public void setup() {
        playerService = new PlayerService(playerRepository);
        userService = new UserService(userRepository);
        mysteryWordService = new MysteryWordService(mysteryWordRepository);
        deckService = new DeckService(deckRepository, cardService);
        cardService = new CardService(cardRepository, mysteryWordService);
        gameService = new GameService(gameRepository,statsRepository, userService, clueService);
        lobbyService = new LobbyService(lobbyRepository, userService, playerService, deckService, cardService, gameService, mysteryWordService);
        LobbyController lc = new LobbyController(userService, lobbyService, playerService, clueService, gameService);
        mockMvc = MockMvcBuilders.standaloneSetup(lc).build();
    }

    /**
     * Tests creator kickPlayerOut lobbies/{lobbyId}/kick/{UserId}
     * Valid Input, adds the User that sends the request to the Lobby
     */
    @Test
    void kickPlayerOut_validInput() throws Exception {
        // given
        Lobby lobby = createLobby();
        User testUser = createUser(1L, "testUser", "1");
        Player testPlayer = new Player(testUser);
        testPlayer.setRole(PlayerRole.GUESSER);
        lobby.addPlayer(testPlayer);
        lobby.setGameMode(GameModeStatus.HUMANS);
        lobby.setCreator(testPlayer);
        lobby.setLobbyStatus(LobbyStatus.WAITING);
        User testUser2 = createUser(2L, "testUser2", "2");
        Player testPlayer2 = new Player(testUser2);
        testPlayer2.setRole(PlayerRole.CLUE_CREATOR);
        lobby.addPlayer(testPlayer2);

        createRepositoryMock(lobby, testUser, testPlayer, testUser2, testPlayer2);

        // make get Request to Lobby with id
        MockHttpServletRequestBuilder putRequest = put("/lobbies/" + lobby.getId() + "/kick/" + testPlayer2.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .header("token", testUser.getToken());

        // then
        mockMvc.perform(putRequest)
                .andExpect(status().isNoContent())
                .andExpect(jsonPath("$").doesNotExist())
                .andDo(print());

        Assert.isTrue(lobby.getPlayers().size() == 1);
        Assert.isTrue(!lobby.getPlayers().contains(testPlayer2));
        Assert.isTrue(lobby.getPlayers().contains(testPlayer));
    }

    /**
     * Tests player leaves the lobby lobbies/{lobbyId}/leave
     */
    @Test
    void leaveFromLobby_validInput() throws Exception {
        // given
        Lobby lobby = createLobby();
        User testUser = createUser(1L, "testUser", "1");
        Player testPlayer = new Player(testUser);
        testPlayer.setRole(PlayerRole.GUESSER);
        lobby.addPlayer(testPlayer);
        lobby.setGameMode(GameModeStatus.HUMANS);
        lobby.setCreator(testPlayer);
        lobby.setLobbyStatus(LobbyStatus.WAITING);
        User testUser2 = createUser(2L, "testUser2", "2");
        Player testPlayer2 = new Player(testUser2);
        testPlayer2.setRole(PlayerRole.CLUE_CREATOR);
        lobby.addPlayer(testPlayer2);

        createRepositoryMock(lobby, testUser, testPlayer, testUser2, testPlayer2);

        // make get Request to Lobby with id
        MockHttpServletRequestBuilder putRequest = put("/lobbies/" + lobby.getId() + "/leave")
                .contentType(MediaType.APPLICATION_JSON)
                .header("token", testUser2.getToken());

        // then
        mockMvc.perform(putRequest)
                .andExpect(status().isNoContent())
                .andExpect(jsonPath("$").doesNotExist())
                .andDo(print());

        Assert.isTrue(lobby.getPlayers().size() == 1);
        Assert.isTrue(!lobby.getPlayers().contains(testPlayer2));
        Assert.isTrue(lobby.getPlayers().contains(testPlayer));
    }

    /**
     * Tests lobby creator leaves the lobby lobbies/{lobbyId}/terminate
     */
    @Test
    void creatorLeavesFromLobby_validInput() throws Exception {
        // given
        Lobby lobby = createLobby();
        User testUser = createUser(1L, "testUser", "1");
        Player testPlayer = new Player(testUser);
        testPlayer.setRole(PlayerRole.GUESSER);
        lobby.addPlayer(testPlayer);
        lobby.setGameMode(GameModeStatus.HUMANS);
        lobby.setCreator(testPlayer);
        lobby.setLobbyStatus(LobbyStatus.WAITING);
        User testUser2 = createUser(2L, "testUser2", "2");
        Player testPlayer2 = new Player(testUser2);
        testPlayer2.setRole(PlayerRole.CLUE_CREATOR);
        lobby.addPlayer(testPlayer2);

        createRepositoryMock(lobby, testUser, testPlayer, testUser2, testPlayer2);

        // make get Request to Lobby with id
        MockHttpServletRequestBuilder putRequest = put("/lobbies/" + lobby.getId() + "/terminate")
                .contentType(MediaType.APPLICATION_JSON)
                .header("token", testUser.getToken());

        // then
        mockMvc.perform(putRequest)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").doesNotExist())
                .andDo(print());

    }


    /**
     * Tests get list of mystery words the lobby /lobbies/{lobbyId}/card
     */
    @Test
    void getMysteryWordsFromCard_validInput() throws Exception {
        // given
        Lobby lobby = createLobby();
        User testUser = createUser(1L, "testUser", "1");
        Player testPlayer = new Player(testUser);
        testPlayer.setRole(PlayerRole.GUESSER);
        lobby.addPlayer(testPlayer);
        lobby.setGameMode(GameModeStatus.HUMANS);
        lobby.setCreator(testPlayer);
        lobby.setLobbyStatus(LobbyStatus.WAITING);
        User testUser2 = createUser(2L, "testUser2", "2");
        Player testPlayer2 = new Player(testUser2);
        testPlayer2.setRole(PlayerRole.CLUE_CREATOR);
        lobby.addPlayer(testPlayer2);
        lobby.setDeck(prepareDeck());

        createRepositoryMock(lobby, testUser, testPlayer, testUser2, testPlayer2);

        // make get Request to Lobby with id
        MockHttpServletRequestBuilder getRequest = get("/lobbies/" + lobby.getId() + "/card")
                .contentType(MediaType.APPLICATION_JSON)
                .header("token", testUser2.getToken());

        // then

        ResultActions result = mockMvc.perform(getRequest);
        MysteryWord mysteryWord = lobby.getDeck().getActiveCard().getMysteryWords().get(0);
        MysteryWord mysteryWord2 = lobby.getDeck().getActiveCard().getMysteryWords().get(1);
                result.andExpect(status().isOk())
                        .andExpect(jsonPath("$.[0].id", is(mysteryWord.getId().intValue())))
                        .andExpect(jsonPath("$.[0].word", is(mysteryWord.getWord())))
                        .andExpect(jsonPath("$.[0].status", is(mysteryWord.getStatus().toString())))
                        .andExpect(jsonPath("$.[1].id", is(mysteryWord2.getId().intValue())))
                        .andExpect(jsonPath("$.[1].word", is(mysteryWord2.getWord())))
                        .andExpect(jsonPath("$.[1].status", is(mysteryWord2.getStatus().toString())))
                .andDo(print());

        /*.andExpect(jsonPath("$.id", is(lobby.getDeck().getActiveCard().getMysteryWords().getId().intValue())))
                .andExpect(jsonPath("$.lobbyName", is(lobby.getLobbyName())))
                .andExpect(jsonPath("$.deck.deckId", is(lobby.getDeck().getDeckId())))
                .andExpect(jsonPath("$.players[0].id", is(toIntExact(lobby.getPlayers().iterator().next().getId()))))
                .andExpect(jsonPath("$.players[0].role", is(lobby.getPlayers().iterator().next().getRole().name())))
                .andExpect(jsonPath("$.gameMode", is(lobby.getGameMode().toString())))
                .andExpect(jsonPath("$.language", is((lobby.getLanguage().toString()))))*/

    }

    /**
     * Tests inviteUserToLobby POST /lobbies/{lobbyId}/invite/{userId}
     * Valid Input, adds lobby to user's inviting lobbies (204)
     */
    @Test
    void inviteUserToLobby_validInput() throws Exception {
        // given
        // init lobby
        Lobby lobby = new Lobby();
        lobby.setId(2L);
        lobby.setLobbyName("testName");
        User testUser = createUser(1L, "testUser", "1");
        Player testPlayer = new Player(testUser);
        testPlayer.setRole(PlayerRole.GUESSER);
        lobby.addPlayer(testPlayer);
        lobby.setGameMode(GameModeStatus.HUMANS);
        lobby.setCreator(testPlayer);
        lobby.setLobbyStatus(LobbyStatus.WAITING);
        // init user to be invited
        User testInvitedUser = createUser(3L, "testInvitedUser", "3");
        testInvitedUser.setStatus(UserStatus.ONLINE);

        given(playerRepository.findByToken(testUser.getToken())).willReturn(testPlayer);
        given(userRepository.findByToken(testUser.getToken())).willReturn(testUser);
        given(playerRepository.findById(testUser.getId())).willReturn(Optional.of(testPlayer));
        given(lobbyRepository.findByLobbyId(lobby.getId())).willReturn(lobby);
        given(userRepository.findById(testInvitedUser.getId())).willReturn(Optional.of(testInvitedUser));
        given(playerRepository.findById(testInvitedUser.getId())).willReturn(Optional.empty());
        given(userRepository.saveAndFlush(testInvitedUser)).willReturn(testInvitedUser);

        // make post Request to Lobby with id & user with id
        MockHttpServletRequestBuilder postRequest = post("/lobbies/" + lobby.getId() + "/invite/" + testInvitedUser.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .header("token", testUser.getToken());

        // then
        mockMvc.perform(postRequest)
                .andExpect(status().isNoContent())
                .andExpect(jsonPath("$").doesNotExist())
                .andDo(print());

        Assert.notEmpty(testInvitedUser.getInvitingLobbies(), "lobby not added to inviting");
    }

    private void createRepositoryMock(Lobby lobby, User testUser, Player testPlayer, User testUser2, Player testPlayer2) {
        given(playerRepository.findByToken(testPlayer.getToken())).willReturn(testPlayer);
        given(playerRepository.findByToken(testPlayer2.getToken())).willReturn(testPlayer2);
        given(playerRepository.findById(testPlayer.getId())).willReturn(Optional.of(testPlayer));
        given(playerRepository.findById(testPlayer2.getId())).willReturn(Optional.of(testPlayer2));
        doNothing().when(playerRepository).delete(Mockito.any());
        doNothing().when(playerRepository).deleteAll(Mockito.any());

        given(userRepository.findByToken(testUser.getToken())).willReturn(testUser);
        given(userRepository.findByToken(testUser2.getToken())).willReturn(testUser2);
        given(lobbyRepository.findByLobbyId(lobby.getId())).willReturn(lobby);
        given(lobbyRepository.save(lobby)).willReturn(lobby);
        doNothing().when(lobbyRepository).delete(Mockito.any());
        doNothing().when(lobbyRepository).flush();

        when(deckRepository.save(Mockito.any())).then(AdditionalAnswers.returnsFirstArg());
        when(cardRepository.save(Mockito.any())).then(AdditionalAnswers.returnsFirstArg());

    }

    private Lobby createLobby() {
        Lobby lobby = new Lobby();
        lobby.setId(1L);
        lobby.setLobbyName("testName");
        return lobby;
    }

    private User createUser(Long id, String username, String token) {
        User testUser = new User();
        testUser.setId(id);
        testUser.setUsername(username);
        testUser.setToken(token);
        return testUser;
    }

    private Deck prepareDeck() {
        Deck deck = new Deck();
        deck.setDeckId(1L);
        Card card = Card.getInstance();
        card.addMysteryWord(createMysteryWord(1L, "Sun", 1));
        card.addMysteryWord(createMysteryWord(2L, "Moon", 2));
        deck.addCard(card);
        Card card2 = Card.getInstance();
        card2.addMysteryWord(createMysteryWord(3L, "Flower", 1));
        card2.addMysteryWord(createMysteryWord(4L, "Tree", 2));
        deck.addCard(card2);

        return deck;
    }

    private MysteryWord createMysteryWord(Long id, String word, int number) {
        MysteryWord mysteryWord = new MysteryWord();
        mysteryWord.setId(id);
        mysteryWord.setWord(word);
        mysteryWord.setNumber(number);
        return mysteryWord;
    }
}
