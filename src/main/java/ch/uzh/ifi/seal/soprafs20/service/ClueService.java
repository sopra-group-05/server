
package ch.uzh.ifi.seal.soprafs20.service;


import ch.uzh.ifi.seal.soprafs20.bots.Bot;
import ch.uzh.ifi.seal.soprafs20.bots.CrazyBot;
import ch.uzh.ifi.seal.soprafs20.bots.FriendlyBot;
import ch.uzh.ifi.seal.soprafs20.bots.MaliciousBot;
import ch.uzh.ifi.seal.soprafs20.constant.*;
import ch.uzh.ifi.seal.soprafs20.entity.*;
import ch.uzh.ifi.seal.soprafs20.exceptions.SopraServiceException;
import ch.uzh.ifi.seal.soprafs20.repository.ClueRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@Transactional
public class ClueService {
    private final Logger log = LoggerFactory.getLogger(ClueService.class);
    private final ClueRepository clueRepository;
    private final PlayerService playerService;
    private final GameService gameService;

    @Autowired
    public ClueService(@Qualifier("clueRepository") ClueRepository clueRepository, PlayerService playerService, GameService gameService) {
        this.clueRepository = clueRepository;
        this.playerService = playerService;
        this.gameService = gameService;
    }

    /*
     * add new clues
     * @param newClue - clue to annotate
     * @param lobby - lobby in which the game takes place
     * @param token - to check if player is allowed to create clues
     */
    public Clue addClue(Clue newClue, Lobby lobby, String token){
        playerService.playerIsInLobby(token, lobby);
        playerService.playerIsClueCreator(token);
        Player player = playerService.getPlayerByToken(token);
        checkClue(newClue, lobby);
        List<Clue> clues = lobby.getGame().getClues();
        for(Clue clue:clues){
            if ((clue.getClueStatus().equals(ClueStatus.ACTIVE) || clue.getClueStatus().equals(ClueStatus.DISABLED))
                    && clue.getPlayer().equals(player) && lobby.getPlayers().size() != 3) {
                throw new SopraServiceException("You already annotated a clue");
            }
        }
        newClue.setClueStatus(ClueStatus.ACTIVE);
        newClue.setPlayer(player);
        newClue.setCard(lobby.getDeck().getActiveCard());
        newClue.setFlagCounter(0);
        clueRepository.save(newClue);
        clueRepository.flush();
        lobby.getGame().addClue(newClue);
        //TODO Set correct time for coming up whit the clue
        gameService.updateClueGeneratorStats(true, newClue.getTimeForClue(), player.getId(), lobby.getId());
        return newClue;
    }

    /*
     * helper function to check, wheter a clue is allowed to be annotated
     * @param clue - the clue about to be annotated
     * @param lobby - lobby in which the game takes place
     */
    private void checkClue(Clue clue, Lobby lobby){
        String hint = clue.getHint();
        List<MysteryWord> mysteryWords = lobby.getDeck().getActiveCard().getMysteryWords();
        int len = hint.length();
        for (int i = 0; i < len; i++){
            if(Character.isWhitespace(hint.charAt(i))){
                throw new SopraServiceException("Clue can not contain any white spaces");
            }
        }
        for(MysteryWord mysteryWord:mysteryWords) {
            if (mysteryWord.getStatus().equals(MysteryWordStatus.IN_USE) && hint.equalsIgnoreCase(mysteryWord.getWord())) {
                throw new SopraServiceException("Clue can not be the same as Mysteryword");
            }
        }
    }

    /*
    *flag a clue and deactivate if >=50% of human players think the clue is illegal
    * @param clueId - identify the clue to flag
    * @param token - to check if player is allowed to flag
    * @param lobby - lobby in which the players play
    * todo: check if player can flag twice
     */

    public boolean flagClue(long clueId, String token, Lobby lobby){
        boolean bool = false;
        playerService.playerIsInLobby(token, lobby);
        playerService.playerIsClueCreator(token);
        float numPlayersbyTwo = (float)playerService.getHumanPlayersExceptActivePlayer(lobby).size()/2;
        Clue clue = clueRepository.findClueById(clueId);
        if(clue == null){
            throw new SopraServiceException("Clue not in Repository");
        }
        clue.setFlagCounter(1 + clue.getFlagCounter());
        if(clue.getClueStatus() == ClueStatus.ACTIVE && clue.getFlagCounter() >= numPlayersbyTwo){
            clue.setClueStatus(ClueStatus.DISABLED);
            if(clue.getPlayer().getPlayerType()==PlayerType.HUMAN)
            {
            	gameService.reduceGoodClues(clue.getPlayer().getId(), lobby.getId());
            	
            }
            bool = true;
        }
        clueRepository.save(clue);
        return bool;
    }

    /*
     * get Clues for Comparing or Guessing
     * @param lobby
     * @param token - token of the player that wants to get the clues, for identifying if he's clue creator or guesser
     * @return List<Clue> - the clues the player is allowed to see
     */

    public List<Clue> getClues(Lobby lobby, String token){
        playerService.playerIsInLobby(token, lobby);
        Player player = playerService.getPlayerByToken(token);
        if(player.getRole().equals(PlayerRole.GUESSER)) {
            return this.getCluesForGuessing(lobby);
        } else{
            return this.getCluesForComparing(lobby);
        }
    }



    /*
     * helper function to get all Clues for the Player that is guessing
     * @param lobby - lobby where from which the clues should be gotten
     * @return List<Clue> - clues for the guessing player to see
     */

    private List<Clue> getCluesForGuessing(Lobby lobby){
        if(comparingFinished(lobby)) {
            List<Clue> clues = lobby.getGame().getClues();
            List<Clue> activeClues = new ArrayList<>();
            for (Clue clue : clues) {
                if (clue.getClueStatus().equals(ClueStatus.ACTIVE)) {
                    activeClues.add(clue);
                }
            }
            return activeClues;
        } else{
            throw new SopraServiceException("Comparing Clues not finished");
        }
    }

    /*
     * helper function to get all Clues for the players that are annotating clues
     * @param lobby - lobby for which the clues should be gotten
     * @return List<Clue> - list of clues that are annotated
     * is synchronized as the first time a player wants to get the clues, the clues for the bots are annotated
     */

    private synchronized List<Clue> getCluesForComparing(Lobby lobby){
        List<Clue> clues = lobby.getGame().getClues();
        List<Clue> activeClues = new ArrayList<>();
        Set<Player> players = lobby.getPlayers();
        if(!haveBotPlayersAnnotatedClues(lobby)){
            createBotClues(lobby);
        }
        for(Clue clue:clues){
            if (clue.getClueStatus().equals(ClueStatus.ACTIVE)) {
                activeClues.add(clue);
            }
        }
        for (Player player:players) {
            if (!player.getStatus().equals(PlayerStatus.REVIEWING_CLUES) && !player.getRole().equals(PlayerRole.GUESSER)) {
                throw new SopraServiceException("Not all Clues are annotated");
            }
        }
        return activeClues;

    }

    /*
     * helper function to check if all Players have annotated their clues
     * @param lobby - lobby on which the check should be applied
     * @return boolean - true if players all players have annotated their clue
     */

    private boolean haveBotPlayersAnnotatedClues(Lobby lobby){
        boolean haveBotPlayersAnnotatedClues = true;
        List<Player> botPlayers = playerService.getBotPlayers(lobby);
        List<Player> playersWhoAnnotated = new ArrayList<>();
        List<Clue> clues = lobby.getGame().getClues();
        for (Clue clue : clues) {
            if (clue.getClueStatus().equals(ClueStatus.ACTIVE)) {
                playersWhoAnnotated.add(clue.getPlayer());
            }
        }
        for(Player player:botPlayers){
            if(!playersWhoAnnotated.contains(player)){
                haveBotPlayersAnnotatedClues = false;
                break;
            }
        }
        return haveBotPlayersAnnotatedClues;

    }

    /*
    * helper function that annotates the clues for all players that are bots
    * @param lobby - lobby in which the bots should annotate clues
     */

    private void createBotClues(Lobby lobby){
        List<Player> botPlayers = playerService.getBotPlayers(lobby);
        Language language = lobby.getLanguage();
        MysteryWord activeMysteryWord = null;
        List<MysteryWord> mysteryWords = lobby.getDeck().getActiveCard().getMysteryWords();
        for(MysteryWord mysteryWord:mysteryWords){
            if(mysteryWord.getStatus().equals(MysteryWordStatus.IN_USE)){
                activeMysteryWord = mysteryWord;
                break;
            }
        }
        Bot friendlyBot = new FriendlyBot(language);
        Bot maliciousBot = new MaliciousBot(language);
        Bot crazyBot = new CrazyBot(language);
        EnumMap<PlayerType, Bot> botEnumMap = new EnumMap<>(PlayerType.class);
        botEnumMap.put(PlayerType.FRIENDLYBOT, friendlyBot);
        botEnumMap.put(PlayerType.MALICIOUSBOT, maliciousBot);
        botEnumMap.put(PlayerType.CRAZYBOT, crazyBot);

        for(Player player:botPlayers){
            Bot bot = botEnumMap.get(player.getPlayerType());
            Clue botClue = new Clue();
            botClue.setHint(bot.getClue(activeMysteryWord));
            botClue.setPlayer(player);
            botClue.setClueStatus(ClueStatus.ACTIVE);
            botClue.setCard(lobby.getDeck().getActiveCard());
            clueRepository.save(botClue);
            clueRepository.flush();
            lobby.getGame().addClue(botClue);
            player.setStatus(PlayerStatus.REVIEWING_CLUES);

        }
    }

    /*
     * helper function to check that all human Players have compared the clues
     * @param lobby - lobby for which the check is
     * @return boolean - boolean wheter the comparing has finished
     */

    private boolean comparingFinished(Lobby lobby){
        Game game = lobby.getGame();
        int compared = game.getComparingGuessCounter();
        int humanPlayersNotActive = playerService.getHumanPlayersExceptActivePlayer(lobby).size();
        return compared == humanPlayersNotActive;
    }
}


