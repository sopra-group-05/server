package ch.uzh.ifi.seal.soprafs20.service;

import ch.uzh.ifi.seal.soprafs20.bots.FriendlyBot;
import ch.uzh.ifi.seal.soprafs20.bots.MaliciousBotTest;
import ch.uzh.ifi.seal.soprafs20.bots.MalicousBot;
import ch.uzh.ifi.seal.soprafs20.constant.*;
import ch.uzh.ifi.seal.soprafs20.entity.*;
import ch.uzh.ifi.seal.soprafs20.exceptions.ConflictException;
import ch.uzh.ifi.seal.soprafs20.exceptions.SopraServiceException;
import ch.uzh.ifi.seal.soprafs20.repository.ClueRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

public class ClueServiceTest {
    @Mock
    private ClueRepository clueRepository;

    @Mock
    private PlayerService playerService;
    @Mock
    private GameService gameService;
    @InjectMocks
    private ClueService clueService;


    private Lobby lobby;

    private Player player1;
    private Player player2;
    private Player player3;
    private Player player4;
    private MysteryWord mysteryWord;
    private Card card;
    private Deck deck;
    private Game game;



    @BeforeEach
    public void setup(){
        MockitoAnnotations.initMocks(this);
        player1 = new Player();
        player1.setUsername("p1");
        player1.setToken("a");
        player1.setId(1L);
        player1.setPlayerType(PlayerType.HUMAN);
        player1.setRole(PlayerRole.CLUE_CREATOR);
        player1.setStatus(PlayerStatus.WRITING_CLUES);
        player2 = new Player();
        player2.setUsername("p2");
        player2.setToken("b");
        player2.setId(2L);
        player2.setPlayerType(PlayerType.HUMAN);
        player2.setRole(PlayerRole.CLUE_CREATOR);
        player2.setStatus(PlayerStatus.WRITING_CLUES);
        player3 = new Player();
        player3.setUsername("p3");
        player3.setToken("c");
        player3.setId(3L);
        player3.setPlayerType(PlayerType.HUMAN);
        player3.setRole(PlayerRole.CLUE_CREATOR);
        player3.setStatus(PlayerStatus.WRITING_CLUES);
        player4 = new Player();
        player4.setUsername("p4");
        player4.setToken("d");
        player4.setId(4L);
        player4.setPlayerType(PlayerType.HUMAN);
        player4.setRole(PlayerRole.GUESSER);
        player4.setStatus(PlayerStatus.WAITING_FOR_CLUES);
        mysteryWord = new MysteryWord();
        mysteryWord.setWord("test");
        mysteryWord.setStatus(MysteryWordStatus.IN_USE);
        card = new Card();
        List<MysteryWord> mysterywords = new ArrayList();
        mysterywords.add(mysteryWord);
        card.setMysteryWords(mysterywords);
        deck = new Deck();
        deck.setActiveCard(card);
        game = new Game();
        lobby = new Lobby();
        lobby.addPlayer(player1);
        lobby.addPlayer(player2);
        lobby.addPlayer(player3);
        lobby.addPlayer(player4);
        lobby.setDeck(deck);
        lobby.setGame(game);
        playerService = mock(PlayerService.class);
        gameService = mock(GameService.class);
        doNothing().when(gameService).updateClueGeneratorStats(Mockito.anyBoolean(), Mockito.anyLong(), Mockito.anyLong(), Mockito.anyLong());
        doNothing().when(gameService).reduceGoodClues(Mockito.anyLong(), Mockito.anyLong());

    }

   @Test
    public void addClueValidInputs(){
        Clue clue = new Clue();
        clue.setHint("hint");
        clue.setTimeForClue(2L);
        when(clueRepository.save(clue)).thenReturn(clue);
        Clue savedClue = clueService.addClue(clue, lobby, player1.getToken());
       Assertions.assertEquals(clue.getHint(), savedClue.getHint());
       Assertions.assertEquals(ClueStatus.ACTIVE, savedClue.getClueStatus());
       Assertions.assertEquals(player1, savedClue.getPlayer());
       Assertions.assertNotNull(savedClue.getFlagCounter());
   }

   @Test
    public void addClueEmptySpace(){
       Clue clue = new Clue();
       clue.setHint("hi nt");
       clue.setTimeForClue(2L);
       when(clueRepository.save(clue)).thenReturn(clue);
       String exceptionMessage = "Clue can not contain any white spaces";
       SopraServiceException exception=  assertThrows(SopraServiceException.class, () ->clueService.addClue(clue, lobby, player1.getToken()), exceptionMessage);
       Assertions.assertEquals(exceptionMessage, exception.getMessage());
   }


   @Test
   public void addClueSameAsMisteryWord(){
       Clue clue = new Clue();
       clue.setHint("test");
       when(clueRepository.save(clue)).thenReturn(clue);
       String exceptionMessage = "Clue can not be the same as Mysteryword";
       SopraServiceException exception=  assertThrows(SopraServiceException.class, () ->clueService.addClue(clue, lobby, player1.getToken()), exceptionMessage);
       Assertions.assertEquals(exceptionMessage, exception.getMessage());
   }



   @Test
   public void addClueAnnotateTwice(){
       Clue clue = new Clue();
       clue.setHint("hint");
       when(clueRepository.save(clue)).thenReturn(clue);
       Clue savedClue = clueService.addClue(clue, lobby, player1.getToken());
       String exceptionMessage = "You already annotated a clue";
       SopraServiceException exception = Assertions.assertThrows(SopraServiceException.class, () -> clueService.addClue(clue, lobby, player1.getToken()), exceptionMessage);
       Assertions.assertEquals(exceptionMessage, exception.getMessage());
   }

   @Test
    public void flagClueValidInputs(){
       Clue clue = new Clue();
       clue.setHint("hint");
       when(clueRepository.save(clue)).thenReturn(clue);
       Clue savedClue = clueService.addClue(clue, lobby, player1.getToken());
       when(clueRepository.findClueById(Mockito.anyLong())).thenReturn(savedClue);
       lobby.addClue(savedClue);
       clueService.flagClue(savedClue.getId(),player1.getToken(),lobby);
       Assertions.assertEquals(1, savedClue.getFlagCounter());
   }


   @Test
   public void flagClueIllegalClueId(){
       Clue clue = new Clue();
       clue.setHint("hint");
       when(clueRepository.save(clue)).thenReturn(clue);
       Clue savedClue = clueService.addClue(clue, lobby, player1.getToken());
       lobby.addClue(savedClue);
       when(clueRepository.findClueById(Mockito.anyLong())).thenReturn(null);
       String exceptionMessage = "Clue not in Repository";
       SopraServiceException exception = Assertions.assertThrows(SopraServiceException.class, () -> clueService.flagClue(savedClue.getId(),player1.getToken(),lobby), exceptionMessage);
       Assertions.assertEquals(exceptionMessage, exception.getMessage());
   }
/*todo

   @Test
   public void flagClueTwice(){

   }


 */
    @Test
    public void flagAndDisableClue(){
        Clue clue = new Clue();
        clue.setHint("hint");
        when(clueRepository.save(clue)).thenReturn(clue);
        Clue savedClue = clueService.addClue(clue, lobby, player1.getToken());
        when(clueRepository.findClueById(Mockito.anyLong())).thenReturn(savedClue);
        lobby.addClue(savedClue);
        savedClue.setFlagCounter(1);
        clueService.flagClue(savedClue.getId(),player1.getToken(),lobby);
        Assertions.assertEquals(ClueStatus.DISABLED, savedClue.getClueStatus());
    }

   @Test
   public void getCluesForComparingValid(){
        List<Player> players = new ArrayList<>();
        players.add(player1);
        players.add(player2);
        players.add(player3);
        players.add(player4);
        when(playerService.getPlayerByToken(Mockito.anyString())).thenReturn(player1);
        when(playerService.getBotPlayers(lobby)).thenReturn(players);
       Clue clue1 = new Clue();
       clue1.setClueStatus(ClueStatus.ACTIVE);
       Clue clue2 = new Clue();
       clue2.setClueStatus(ClueStatus.ACTIVE);
       Clue clue3 = new Clue();
       clue3.setClueStatus(ClueStatus.ACTIVE);
       lobby.addClue(clue1);
       lobby.addClue(clue2);
       lobby.addClue(clue3);
       player1.setStatus(PlayerStatus.REVIEWING_CLUES);
       player2.setStatus(PlayerStatus.REVIEWING_CLUES);
       player3.setStatus(PlayerStatus.REVIEWING_CLUES);
       game.addClue(clue1);
       game.addClue(clue2);
       game.addClue(clue3);
       List<Clue> clues = clueService.getClues(lobby, player1.getToken());
       Assertions.assertTrue(clues.contains(clue1));
       Assertions.assertTrue(clues.contains(clue2));
       Assertions.assertTrue(clues.contains(clue3));
   }
   @Test
   public void getCluesForGuessingValid(){
       List<Player> players = new ArrayList<>();
       players.add(player1);
       players.add(player2);
       players.add(player3);
       players.add(player4);
       Mockito.when(playerService.getBotPlayers(lobby)).thenReturn(players);
       Mockito.when(playerService.getPlayerByToken(Mockito.any())).thenReturn(player1);
       Clue clue1 = new Clue();
       clue1.setClueStatus(ClueStatus.ACTIVE);
       Clue clue2 = new Clue();
       clue2.setClueStatus(ClueStatus.ACTIVE);
       Clue clue3 = new Clue();
       clue3.setClueStatus(ClueStatus.ACTIVE);
       lobby.addClue(clue1);
       lobby.addClue(clue2);
       lobby.addClue(clue3);
       player1.setStatus(PlayerStatus.WAITING_FOR_GUESS);
       player2.setStatus(PlayerStatus.WAITING_FOR_GUESS);
       player3.setStatus(PlayerStatus.WAITING_FOR_GUESS);
       game.addClue(clue1);
       game.addClue(clue2);
       game.addClue(clue3);
       game.setComparingGuessCounter(3);
       List<Clue> clues = clueService.getClues(lobby, player4.getToken());
       Assertions.assertTrue(clues.contains(clue1));
       Assertions.assertTrue(clues.contains(clue2));
       Assertions.assertTrue(clues.contains(clue3));
   }

   @Test
   public void getClueClueCreatorAnnotatingNotFinished(){
       List<Player> players = new ArrayList<>();
       players.add(player1);
       players.add(player2);
       players.add(player3);
       players.add(player4);
       when(playerService.getPlayerByToken(Mockito.anyString())).thenReturn(player1);
       when(playerService.getBotPlayers(lobby)).thenReturn(players);
       Clue clue1 = new Clue();
       clue1.setClueStatus(ClueStatus.ACTIVE);
       Clue clue2 = new Clue();
       clue2.setClueStatus(ClueStatus.ACTIVE);
       Clue clue3 = new Clue();
       clue3.setClueStatus(ClueStatus.ACTIVE);
       lobby.addClue(clue1);
       lobby.addClue(clue2);
       lobby.addClue(clue3);
       player1.setStatus(PlayerStatus.REVIEWING_CLUES);
       player2.setStatus(PlayerStatus.WRITING_CLUES);
       player3.setStatus(PlayerStatus.REVIEWING_CLUES);
       game.addClue(clue1);
       game.addClue(clue2);
       game.addClue(clue3);
       List<Clue> clues = clueService.getClues(lobby, player1.getToken());
       String exceptionMessage = "Not all Clues are annotated";
       SopraServiceException exception = Assertions.assertThrows(SopraServiceException.class, () -> clueService.getClues(lobby, player1.getToken()), exceptionMessage);
       Assertions.assertEquals(exceptionMessage, exception.getMessage());
   }

   @Test
   public void getClueCluesGuesserComparingNotFinished(){
       List<Player> players = new ArrayList<>();
       players.add(player1);
       players.add(player2);
       players.add(player3);
       players.add(player4);
       Mockito.when(playerService.getBotPlayers(lobby)).thenReturn(players);
       Mockito.when(playerService.getPlayerByToken(Mockito.any())).thenReturn(player1);
       Clue clue1 = new Clue();
       clue1.setClueStatus(ClueStatus.ACTIVE);
       Clue clue2 = new Clue();
       clue2.setClueStatus(ClueStatus.ACTIVE);
       Clue clue3 = new Clue();
       clue3.setClueStatus(ClueStatus.ACTIVE);
       lobby.addClue(clue1);
       lobby.addClue(clue2);
       lobby.addClue(clue3);
       player1.setStatus(PlayerStatus.WAITING_FOR_GUESS);
       player2.setStatus(PlayerStatus.WAITING_FOR_GUESS);
       player3.setStatus(PlayerStatus.WAITING_FOR_GUESS);
       game.addClue(clue1);
       game.addClue(clue2);
       game.addClue(clue3);
       game.setComparingGuessCounter(2);
       String exceptionMessage = "Comparing Clues not finished";
       SopraServiceException exception = Assertions.assertThrows(SopraServiceException.class, () -> clueService.getClues(lobby, player4.getToken()), exceptionMessage);
       Assertions.assertEquals(exceptionMessage, exception.getMessage());
   }

   @Test
   public void botAnnotateCluesValid(){

   }

   @Test
   public void botAnnotateClueAlreadyDone(){

   }
}
