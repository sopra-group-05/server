package ch.uzh.ifi.seal.soprafs20.service;


import ch.uzh.ifi.seal.soprafs20.bots.Bot;
import ch.uzh.ifi.seal.soprafs20.bots.FriendlyBot;
import ch.uzh.ifi.seal.soprafs20.bots.MalicousBot;
import ch.uzh.ifi.seal.soprafs20.constant.ClueStatus;
import ch.uzh.ifi.seal.soprafs20.constant.Language;
import ch.uzh.ifi.seal.soprafs20.constant.PlayerRole;
import ch.uzh.ifi.seal.soprafs20.constant.PlayerType;
import ch.uzh.ifi.seal.soprafs20.entity.*;
import ch.uzh.ifi.seal.soprafs20.exceptions.BadRequestException;
import ch.uzh.ifi.seal.soprafs20.exceptions.ForbiddenException;
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

    public ClueService(@Qualifier("clueRepository") ClueRepository clueRepository) {
        this.clueRepository = clueRepository;
    }

    //todo: add Mysteryword for rule violation
    //Add new Clues
    public void addClue(Clue newClue, Lobby lobby, String token){
        playerIsInLobby(token, lobby);
        playerIsClueCreator(token);
        checkClue(newClue);
        newClue.setClueStatus(ClueStatus.ACTIVE);
        newClue.setPlayer(playerService.getPlayerByToken(token));
        //newClue.setMysteryword(mysteryWord);
        newClue.setGame(lobby.getGame());
        clueRepository.save(newClue);
        clueRepository.flush();
        lobby.getGame().addClue(newClue);
    }

    private void checkClue(Clue clue){
        String hint = clue.getHint();
        int len = hint.length();
        for (int i = 0; i < len; i++){
            if(!Character.isLetter(hint.charAt(i))){
                throw new BadRequestException("Clue is not a single word of only letters");
            }
        }
        /*
        if(hint.toLowerCase().equals(clue.getMysteryword().getWord().toLowerCase())){
            throw new BadRequestException("Clue can not be the same as Mysteryword");
        }
         */
    }

    public void flagClue(long clueId, String token, Lobby lobby){
        playerIsInLobby(token, lobby);
        playerIsClueCreator(token);
        int numPlayers = lobby.getPlayers().size();
        Clue clue = clueRepository.findClueById(clueId);
        if(clue == null){
            throw new BadRequestException("Clue not in Repository");
        }
        clue.setFlagCounter(1 + clue.getFlagCounter());
        if(clue.getClueStatus() == ClueStatus.ACTIVE && clue.getFlagCounter() >= numPlayers/2){
            clue.setClueStatus(ClueStatus.DISABLED);
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
                    clue.setClueStatus(ClueStatus.INACTIVE);
                }
            }
            return activeClues;
        } else{
            throw new BadRequestException("Comparing Clues not finished");
        }
    }

    private List<Clue> getCluesForComparing(Lobby lobby){
        List<Clue> clues = lobby.getGame().getClues();
        List<Clue> activeClues = new ArrayList<>();
        for(Clue clue:clues){
            if (clue.getClueStatus().equals(ClueStatus.ACTIVE)) {
                activeClues.add(clue);
            }
        }
        if(activeClues.size() == this.getHumanPlayersExceptAvtivePlayer(lobby).size()) {
            return activeClues;
        } else{
            throw new BadRequestException("Not all Clues are annotated");
        }
    }

    /*
    private void createBotClues(Lobby lobby){
        List<Player> botPlayers = this.getBotPlayers(lobby);
        Language language = lobby.getLanguage();
        Bot friendlyBot = new FriendlyBot(language);
        Bot maliciousBot = new MalicousBot(language);
        HashMap<PlayerType, Bot> botHashMap = new HashMap<PlayerType, Bot>(){{
                put(PlayerType.FRIENDLYBOT, friendlyBot);
                put(PlayerType.MALICIOUSBOT, maliciousBot);
            }};
        for(Player player:botPlayers){
            Bot bot = botHashMap.get(player.getPlayerType());
            Clue botClue = new Clue();
            botClue.setHint(bot.getClue());
            player.setClue(;
        }
    }
    */

    private boolean comparingFinished(Lobby lobby){
        Game game = lobby.getGame();
        int compared = game.getComparingGuessCounter();
        int humanPlayersNotActive = this.getHumanPlayersExceptAvtivePlayer(lobby).size();
        return compared == humanPlayersNotActive;
    }

    private List<Player> getHumanPlayersExceptAvtivePlayer(Lobby lobby){
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

}


