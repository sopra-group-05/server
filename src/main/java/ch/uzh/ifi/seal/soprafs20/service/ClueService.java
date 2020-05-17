
package ch.uzh.ifi.seal.soprafs20.service;


import ch.uzh.ifi.seal.soprafs20.bots.Bot;
import ch.uzh.ifi.seal.soprafs20.bots.FriendlyBot;
import ch.uzh.ifi.seal.soprafs20.bots.MalicousBot;
import ch.uzh.ifi.seal.soprafs20.constant.*;
import ch.uzh.ifi.seal.soprafs20.entity.*;
import ch.uzh.ifi.seal.soprafs20.service.GameService;
import ch.uzh.ifi.seal.soprafs20.exceptions.BadRequestException;
import ch.uzh.ifi.seal.soprafs20.exceptions.ForbiddenException;
import ch.uzh.ifi.seal.soprafs20.exceptions.SopraServiceException;
import ch.uzh.ifi.seal.soprafs20.exceptions.UnauthorizedException;
import ch.uzh.ifi.seal.soprafs20.repository.ClueRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

@Service
@Transactional
public class ClueService {
    private final Logger log = LoggerFactory.getLogger(ClueService.class);
    private final ClueRepository clueRepository;

    @Autowired
    private LobbyService lobbyService;

    @Autowired
    private PlayerService playerService;

    @Autowired
    private GameService gameService;
    

    public ClueService(@Qualifier("clueRepository") ClueRepository clueRepository) {
        this.clueRepository = clueRepository;
    }

    /*
     * add new clues
     * @param newClue - clue to annotate
     * @param lobby - lobby in which the game takes place
     * @param token - to check if player is allowed to create clues
     */
    public void addClue(Clue newClue, Lobby lobby, String token){
        playerIsInLobby(token, lobby);
        playerIsClueCreator(token);
        checkClue(newClue, lobby);
        Player player = playerService.getPlayerByToken(token);
        List<Clue> clues = lobby.getGame().getClues();
        for(Clue clue:clues){
            if((clue.getClueStatus().equals(ClueStatus.ACTIVE) | clue.getClueStatus().equals(ClueStatus.DISABLED)) && clue.getPlayer().equals(player)){
                throw new SopraServiceException("You already annotated a clue");
            }
        }
        newClue.setClueStatus(ClueStatus.ACTIVE);
        newClue.setPlayer(playerService.getPlayerByToken(token));
        newClue.setCard(lobby.getDeck().getActiveCard());
        newClue.setGame(lobby.getGame());
        newClue.setFlagCounter(0);
        clueRepository.save(newClue);
        clueRepository.flush();
        lobby.getGame().addClue(newClue);
        //TODO Set correct time for coming up whit the clue
        gameService.updateClueGeneratorStats(true, 0l, playerService.getPlayerByToken(token).getId(), lobby.getId());      
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
            if (mysteryWord.getStatus().equals(MysteryWordStatus.IN_USE)) {
                if (hint.toLowerCase().equals(mysteryWord.getWord().toLowerCase())) {
                    throw new SopraServiceException("Clue can not be the same as Mysteryword");
                }
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

    public void flagClue(long clueId, String token, Lobby lobby){
        playerIsInLobby(token, lobby);
        playerIsClueCreator(token);
        float numPlayersbyTwo = (float)this.getHumanPlayersExceptActivePlayer(lobby).size()/2;
        Clue clue = clueRepository.findClueById(clueId);
        if(clue == null){
            throw new BadRequestException("Clue not in Repository");
        }
        clue.setFlagCounter(1 + clue.getFlagCounter());
        if(clue.getClueStatus() == ClueStatus.ACTIVE && clue.getFlagCounter() >= numPlayersbyTwo){
            clue.setClueStatus(ClueStatus.DISABLED);
            gameService.reduceGoodClues(playerService.getPlayerByToken(token).getId(), lobby.getId());
        }
        clueRepository.save(clue);
    }

    /*
     * get Clues for Comparing or Guessing
     * @param lobby
     * @param token - token of the player that wants to get the clues, for identifying if he's clue creator or guesser
     * @return List<Clue> - the clues the player is allowed to see
     */

    public List<Clue> getClues(Lobby lobby, String token){
        playerIsInLobby(token, lobby);
        Player player = playerService.getPlayerByToken(token);
        if(player.getRole().equals(PlayerRole.GUESSER)) {
            return this.getCluesForGuessing(lobby);
        } else{
            return this.getCluesForComparing(lobby);
        }
    }

    /*
     * helper function to check if a player is allowed to annotate clues
     * @param token - token of the player to identify him
     */

    private void playerIsClueCreator(String token){
        Player player = playerService.getPlayerByToken(token);
        if (player.getRole() != PlayerRole.CLUE_CREATOR){
            throw new UnauthorizedException("Player is not Clue Creator");
        }
    }

    /*
     * helper function to check wheter the player is in the lobby
     * @param token - token of the player
     * @param lobby - lobby to check if the player is part of
     */
    private void playerIsInLobby(String token, Lobby lobby){
        Player player = playerService.getPlayerByToken(token);
        if (player == null) {
            throw new UnauthorizedException("User is not a Player");
        }
        if (!lobby.getPlayers().contains(player)){
            throw new UnauthorizedException("Player is not in Lobby");
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
            throw new BadRequestException("Comparing Clues not finished");
        }
    }

    /*
     * helper function to get all Clues for the players that are annotating clues
     * @param lobby - lobby for which the clues should be gotten
     * @return List<Clue> - list of clues that are annotated
     * is synchronized as the first time a player wants to get the clues, the clues for the bots are annotated
     */

    synchronized private List<Clue> getCluesForComparing(Lobby lobby){
        List<Clue> clues = lobby.getGame().getClues();
        List<Clue> activeClues = new ArrayList<>();
        if(!haveBotPlayersAnnotatedClues(lobby)){
            createBotClues(lobby);
        }
        for(Clue clue:clues){
            if (clue.getClueStatus().equals(ClueStatus.ACTIVE)) {
                activeClues.add(clue);
            }
        }
        if(activeClues.size() == lobby.getPlayers().size()-1) {
            return activeClues;
        } else{
            throw new BadRequestException("Not all Clues are annotated");
        }
    }

    /*
     * helper function to check if all Players have annotated their clues
     * @param lobby - lobby on which the check should be applied
     * @return boolean - true if players all players have annotated their clue
     */

    private boolean haveBotPlayersAnnotatedClues(Lobby lobby){
        boolean haveBotPlayersAnnotatedClues = true;
        List<Player> botPlayers = this.getBotPlayers(lobby);
        List<Player> playersWhoAnnotated = new ArrayList<>();
        List<Clue> clues = lobby.getGame().getClues();
        List<Clue> activeClues = new ArrayList<>();
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
        List<Player> botPlayers = this.getBotPlayers(lobby);
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
        Bot maliciousBot = new MalicousBot(language);
        HashMap<PlayerType, Bot> botHashMap = new HashMap<PlayerType, Bot>(){{
                put(PlayerType.FRIENDLYBOT, friendlyBot);
                put(PlayerType.MALICIOUSBOT, maliciousBot);
            }};
        for(Player player:botPlayers){
            Bot bot = botHashMap.get(player.getPlayerType());
            Clue botClue = new Clue();
            botClue.setHint(bot.getClue(activeMysteryWord));
            botClue.setPlayer(player);
            botClue.setClueStatus(ClueStatus.ACTIVE);
            botClue.setCard(lobby.getDeck().getActiveCard());
            botClue.setGame(lobby.getGame());
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
        int humanPlayersNotActive = this.getHumanPlayersExceptActivePlayer(lobby).size();
        return compared == humanPlayersNotActive;
    }

    /*
     * helper function to get all Players that are humans
     * @param lobby - lobby to check for human players
     * @return List<Player> - list of human players in the lobby
     */
    private List<Player> getHumanPlayersExceptActivePlayer(Lobby lobby){
        Set<Player> players= lobby.getPlayers();
        List<Player> humanPlayers= new ArrayList<>();
        for(Player player:players){
            if(player.getPlayerType().equals(PlayerType.HUMAN) && !player.getRole().equals(PlayerRole.GUESSER)){
                humanPlayers.add(player);
            }
        }
        return humanPlayers;
    }

    /*
    * helper function to get all Players that are bots
    * @param lobby - lobby to check for bot players
    * @return List<Player> - list of bot players
     */
    private List<Player> getBotPlayers(Lobby lobby){
        Set<Player> players= lobby.getPlayers();
        List<Player> humanPlayers= new ArrayList<>();
        for(Player player:players){
            if(player.getPlayerType().equals(PlayerType.FRIENDLYBOT) |  player.getPlayerType().equals(PlayerType.MALICIOUSBOT)){
                humanPlayers.add(player);
            }
        }
        return humanPlayers;
    }

}


