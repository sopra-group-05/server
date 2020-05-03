
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

    //todo: add Mysteryword for rule violation
    //Add new Clues
    public void addClue(Clue newClue, Lobby lobby, String token){
        playerIsInLobby(token, lobby);
        playerIsClueCreator(token);
        checkClue(newClue, lobby);
        newClue.setClueStatus(ClueStatus.ACTIVE);
        newClue.setPlayer(playerService.getPlayerByToken(token));
        newClue.setCard(lobby.getDeck().getActiveCard());
        newClue.setGame(lobby.getGame());
        newClue.setFlagCounter(0);
        clueRepository.save(newClue);
        clueRepository.flush();
        lobby.getGame().addClue(newClue);
        gameService.updateClueGeneratorStats(true, 15l, playerService.getPlayerByToken(token).getId(), lobby.getId());      
    }

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

    public List<Clue> getClues(Lobby lobby, String token){
        playerIsInLobby(token, lobby);
        Player player = playerService.getPlayerByToken(token);
        if(player.getRole().equals(PlayerRole.GUESSER)) {
            return this.getCluesForGuessing(lobby);
        } else{
            return this.getCluesForComparing(lobby);
        }
    }

    private void playerIsClueCreator(String token){
        Player player = playerService.getPlayerByToken(token);
        if (player.getRole() != PlayerRole.CLUE_CREATOR){
            throw new UnauthorizedException("Player is not Clue Creator");
        }
    }

    private void playerIsInLobby(String token, Lobby lobby){
        Player player = playerService.getPlayerByToken(token);
        if (player == null) {
            throw new UnauthorizedException("User is not a Player");
        }
        if (!lobby.getPlayers().contains(player)){
            throw new UnauthorizedException("Player is not in Lobby");
        }

    }

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


    private boolean comparingFinished(Lobby lobby){
        Game game = lobby.getGame();
        int compared = game.getComparingGuessCounter();
        int humanPlayersNotActive = this.getHumanPlayersExceptActivePlayer(lobby).size();
        return compared == humanPlayersNotActive;
    }

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

    public void deleteCluesForPlayer(Player player){
        List<Clue> clues= player.getClues();
        clueRepository.deleteAll(clues);
    }

}


