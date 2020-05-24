package ch.uzh.ifi.seal.soprafs20.service;

import ch.uzh.ifi.seal.soprafs20.constant.*;
import ch.uzh.ifi.seal.soprafs20.entity.*;
import ch.uzh.ifi.seal.soprafs20.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.List;
import java.util.Optional;

import static ch.uzh.ifi.seal.soprafs20.constant.PlayerStatus.*;
import static ch.uzh.ifi.seal.soprafs20.constant.PlayerType.FRIENDLYBOT;
import static ch.uzh.ifi.seal.soprafs20.constant.PlayerType.HUMAN;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

public class LobbyServiceTest {
    @Mock
    private LobbyRepository lobbyRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private PlayerRepository playerRepository;
    @Mock
    private GameRepository gameRepository;
    @Mock
    private StatsRepository statsRepository;
    @Mock
    private DeckRepository deckRepository;
    @Mock
    private CardRepository cardRepository;
    @Mock
    private MysteryWordRepository mysteryWordRepository;

    @InjectMocks
    private LobbyService lobbyService;
    @InjectMocks
    private GameService gameService;
    @MockBean
    private PlayerService playerService;
    @MockBean
    private UserService userService;
    @MockBean
    private ClueService clueService;
    @MockBean
    private MysteryWordService mysteryWordService;
    @MockBean
    private DeckService deckService;
    @MockBean
    private CardService cardService;

    private Lobby lobby;
    private Player testPlayer;
    private Player testPlayer2;
    private Player botPlayer1;
    private User testUser;
    private User testUser2;
    private User botUser1;


    /**
     * Is called before each test
     * creates a new lobby to e used
     * lobbyRepository.save will always return the dummy lobby
     */
    @BeforeEach
    public void setup() {
        MockitoAnnotations.initMocks(this);


        playerService = new PlayerService(playerRepository);
        userService = new UserService(userRepository);
        gameService = new GameService(gameRepository,statsRepository, userService, clueService);

        mysteryWordService = new MysteryWordService(mysteryWordRepository);
        deckService = new DeckService(deckRepository, cardService);
        cardService = new CardService(cardRepository, mysteryWordService);
        lobbyService = new LobbyService(lobbyRepository, userService, playerService, deckService, cardService, gameService, mysteryWordService);

        // given
        lobby = new Lobby();
        lobby.setId(1L);
        lobby.setLobbyName("testName");
        lobby.setDeck(prepareDeck());

        testUser = new User();
        testUser.setId(1L);
        testUser.setToken("1");
        testUser2 = new User();
        testUser2.setId(2L);
        botUser1 = new User();
        botUser1.setId(11L);

        testPlayer = new Player(testUser);
        testPlayer.setRole(PlayerRole.GUESSER);
        testPlayer.setPlayerType(HUMAN);
        lobby.setCreator(testPlayer);
        lobby.addPlayer(testPlayer);

        testPlayer2 = new Player(testUser2);
        testPlayer2.setRole(PlayerRole.CLUE_CREATOR);
        testPlayer2.setPlayerType(HUMAN);
        testPlayer.setToken("1");
        lobby.addPlayer(testPlayer2);

        botPlayer1 = new Player(botUser1);
        botPlayer1.setRole(PlayerRole.CLUE_CREATOR);
        botPlayer1.setPlayerType(FRIENDLYBOT);
        lobby.addPlayer(botPlayer1);

        lobby.setGameMode(GameModeStatus.BOTS);
        lobby.setCreator(testPlayer);
        lobby.setLanguage(Language.EN);

        GameStats gameStats = new GameStats(testPlayer2.getId(), lobby.getId());

        // when -> any object is being save in the lobbyRepository -> return the dummy lobby
        Mockito.when(lobbyRepository.save(any())).thenReturn(lobby);
        Mockito.when(lobbyRepository.findById(any())).thenReturn(Optional.of(lobby));
        Mockito.when(lobbyRepository.findByLobbyId(any())).thenReturn(lobby);
        Mockito.when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
        Mockito.when(userRepository.findById(testUser2.getId())).thenReturn(Optional.of(testUser2));
        Mockito.when(playerRepository.findById(testPlayer.getId())).thenReturn(Optional.of(testPlayer));
        Mockito.when(playerRepository.findById(testPlayer2.getId())).thenReturn(Optional.of(testPlayer2));
        when(playerRepository.saveAll(anyList()))
                .thenAnswer(invocation -> invocation.getArgument(0, List.class));
        when(mysteryWordRepository.save(Mockito.any())).then(AdditionalAnswers.returnsFirstArg());

        doNothing().when(lobbyRepository).delete(any());
        doNothing().when(gameRepository).delete(any());
        doNothing().when(playerRepository).delete(any());
        doNothing().when(statsRepository).delete(any());
        doNothing().when(playerRepository).deleteAll(any());

        Mockito.when(statsRepository.findByPlayerIdAndLobbyId(testPlayer2.getId(), lobby.getId())).thenReturn(gameStats);
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

    //removePlayerFromLobby full cases
    //isGuesserOfLobby partial cases
    //checkIfLobbyExists partial cases

    /**
     * removes player from lobby and if less than 3 players left then ends the lobby
     */
    @Test
    public void removePlayerFromLobby_endLobby() {
        lobby.setLobbyStatus(LobbyStatus.RUNNING);
        Game game = gameService.createNewGame(lobby);
        lobby.setGame(game);
        // test with three players and end the game
        lobbyService.removePlayerFromLobby(lobby.getId(), testPlayer2.getId());

        //the lobby creator has been overridden
        assertEquals(lobby.getCreator().getId(), testPlayer2.getId());
    }

    /**
     * removes player from lobby and delete leaving player's clues and leave the lobby
     */
    @Test
    public void removePlayerFromLobby_DeleteClues() {
        Clue clue1 = new Clue();
        clue1.setClueStatus(ClueStatus.ACTIVE);
        clue1.setHint("UnitTest");
        clue1.setPlayer(testPlayer2);
        testPlayer2.setClue(clue1);

        User testUser4 = new User();
        testUser4.setId(4L);

        Player testPlayer4 = new Player(testUser4);
        testPlayer4.setRole(PlayerRole.CLUE_CREATOR);
        testPlayer4.setPlayerType(HUMAN);
        lobby.addPlayer(testPlayer4);

        Mockito.when(userRepository.findById(testUser4.getId())).thenReturn(Optional.of(testUser4));
        Mockito.when(playerRepository.findById(testPlayer4.getId())).thenReturn(Optional.of(testPlayer4));

        lobby.setLobbyStatus(LobbyStatus.RUNNING);
        Game game = gameService.createNewGame(lobby);
        lobby.setGame(game);
        // test with three players and end the game
        lobbyService.removePlayerFromLobby(lobby.getId(), testPlayer2.getId());

        //the lobby creator has been overridden
        assertNull(clue1.getPlayer());
        assertTrue(game.getClues().isEmpty());
        assertFalse(lobby.getPlayers().contains(testPlayer2));
    }

    /**
     * removes player from lobby and delete leaving player's clues, leave the lobby make the first player GUESSER
     */
    @Test
    public void removePlayerFromLobby_DeleteClues_ChangeGuesser() {
        testPlayer.setRole(PlayerRole.CLUE_CREATOR);
        testPlayer2.setRole(PlayerRole.GUESSER);

        Clue clue1 = new Clue();
        clue1.setClueStatus(ClueStatus.ACTIVE);
        clue1.setHint("UnitTest");
        clue1.setPlayer(testPlayer2);
        testPlayer2.setClue(clue1);

        User testUser4 = new User();
        testUser4.setId(4L);

        Player testPlayer4 = new Player(testUser4);
        testPlayer4.setRole(PlayerRole.CLUE_CREATOR);
        testPlayer4.setPlayerType(HUMAN);
        lobby.addPlayer(testPlayer4);

        Mockito.when(userRepository.findById(testUser4.getId())).thenReturn(Optional.of(testUser4));
        Mockito.when(playerRepository.findById(testPlayer4.getId())).thenReturn(Optional.of(testPlayer4));

        lobby.setLobbyStatus(LobbyStatus.RUNNING);
        Game game = gameService.createNewGame(lobby);
        lobby.setGame(game);
        // test with three players and end the game
        lobbyService.removePlayerFromLobby(lobby.getId(), testPlayer2.getId());

        //the lobby creator has been overridden
        assertNull(clue1.getPlayer());
        assertFalse(lobby.getPlayers().contains(testPlayer2));
    }

    /**
     * removes player from lobby and delete leaving player's clues, leave the lobby make the first player GUESSER
     */
    @Test
    public void removePlayerFromLobby_CreatorLeavesLobby() {

        Clue clue1 = new Clue();
        clue1.setClueStatus(ClueStatus.ACTIVE);
        clue1.setHint("UnitTest");
        clue1.setPlayer(testPlayer);
        testPlayer.setClue(clue1);

        User testUser4 = new User();
        testUser4.setId(4L);

        Player testPlayer4 = new Player(testUser4);
        testPlayer4.setRole(PlayerRole.CLUE_CREATOR);
        testPlayer4.setPlayerType(HUMAN);
        lobby.addPlayer(testPlayer4);

        Mockito.when(userRepository.findById(testUser4.getId())).thenReturn(Optional.of(testUser4));
        Mockito.when(playerRepository.findById(testPlayer4.getId())).thenReturn(Optional.of(testPlayer4));

        lobby.setLobbyStatus(LobbyStatus.RUNNING);
        Game game = gameService.createNewGame(lobby);
        lobby.setGame(game);
        // test with three players and end the game
        lobbyService.removePlayerFromLobby(lobby.getId(), testPlayer.getId());

        //the lobby creator has been overridden
        assertNotEquals(lobby.getCreator().getId(), testPlayer.getId());
        assertNull(clue1.getPlayer());
        assertFalse(lobby.getPlayers().contains(testPlayer));
    }

    /**
     * accepts MysteryWord and change playerStatus
     */
    @Test
    public void acceptMysteryWord_InLobby() {

        Clue clue1 = new Clue();
        clue1.setClueStatus(ClueStatus.ACTIVE);
        clue1.setHint("UnitTest");
        clue1.setPlayer(testPlayer);
        testPlayer.setClue(clue1);

        Mockito.when(playerRepository.findByToken(testUser.getToken())).thenReturn(testPlayer);

        lobby.setLobbyStatus(LobbyStatus.RUNNING);
        Game game = gameService.createNewGame(lobby);
        lobby.setGame(game);
        lobbyService.acceptOrDeclineMysteryWord(testUser, lobby, Boolean.TRUE);

        //check bot player's status
        assertEquals(botPlayer1.getStatus(), WAITING_TO_ACCEPT_MYSTERY_WORD);

        testPlayer.setStatus(WAITING_TO_ACCEPT_MYSTERY_WORD);
        testPlayer2.setStatus(WAITING_TO_ACCEPT_MYSTERY_WORD);
        lobbyService.acceptOrDeclineMysteryWord(testUser, lobby, Boolean.TRUE);
        // check human and bot player status
        assertEquals(botPlayer1.getStatus(), WRITING_CLUES);
        assertEquals(testPlayer2.getStatus(), WRITING_CLUES);
        assertEquals(testPlayer.getStatus(), WAITING_FOR_CLUES);
    }

    /**
     * declines MysteryWord and change playerStatus
     */
    @Test
    public void declineMysteryWord_InLobby() {

        Clue clue1 = new Clue();
        clue1.setClueStatus(ClueStatus.ACTIVE);
        clue1.setHint("UnitTest");
        clue1.setPlayer(testPlayer);
        testPlayer.setClue(clue1);

        Mockito.when(playerRepository.findByToken(testUser.getToken())).thenReturn(testPlayer);
        /*MysteryWord mysteryWord1 = createMysteryWord(1L, "Sun", 1);
        MysteryWord mysteryWord2 = createMysteryWord(2L, "Moon", 2);
        when(mysteryWordRepository.save(mysteryWord1)).thenReturn(mysteryWord1);
        when(mysteryWordRepository.save(mysteryWord2)).thenReturn(mysteryWord2);*/

        lobby.setLobbyStatus(LobbyStatus.RUNNING);
        Game game = gameService.createNewGame(lobby);
        lobby.setGame(game);
        // test with three players and end the game
        lobbyService.acceptOrDeclineMysteryWord(testUser, lobby, Boolean.FALSE);

        // check human and bot player status
        assertEquals(botPlayer1.getStatus(), WAITING_FOR_NUMBER);
        assertEquals(testPlayer2.getStatus(), WAITING_FOR_NUMBER);
        assertEquals(testPlayer.getStatus(), PICKING_NUMBER);
    }

}
