package ch.uzh.ifi.seal.soprafs20.service;


import ch.uzh.ifi.seal.soprafs20.constant.ClueStatus;
import ch.uzh.ifi.seal.soprafs20.constant.PlayerRole;
import ch.uzh.ifi.seal.soprafs20.entity.Lobby;
import ch.uzh.ifi.seal.soprafs20.entity.Player;
import ch.uzh.ifi.seal.soprafs20.entity.User;
import ch.uzh.ifi.seal.soprafs20.entity.Clue;
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

import java.util.List;

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

    //Add new Clues
    public void addClue(Clue newClue, long lobbyId, String token){
        playerIsInLobby(token, lobbyId);
        playerIsClueCreator(token);
        checkClue(newClue);
        newClue.setClueStatus(ClueStatus.ACTIVE);
        newClue.setPlayer(playerService.getPlayerByToken(token));
        clueRepository.save(newClue);
        clueRepository.flush();
    }

    private void checkClue(Clue clue){
        String hint = clue.getHint();
        int len = hint.length();
        for (int i = 0; i < len; i++){
            if(Character.isLetter(hint.charAt(i))){
                throw new BadRequestException("Clue is not a single word of only letters");
            }
        }
        //TODO check mysteryword
    }

    public void flagClue(long clueId, String token, long lobbyId){
        playerIsInLobby(token, lobbyId);
        playerIsClueCreator(token);
        Lobby lobby = lobbyService.getLobbyById(lobbyId);
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
    public List<Clue> getCluesForChecking(long lobbyId, String token){
        playerIsInLobby(token, lobbyId);
        playerIsClueCreator(token);
        return this.lobbyService.getLobbyById(lobbyId).getClues();
    }

    private void playerIsClueCreator(String token){
        Player player = playerService.getPlayerByToken(token);
        if (player.getRole() != PlayerRole.CLUE_CREATOR){
            throw new UnauthorizedException("Player is not Clue Creator");
        }
    }

    private void playerIsInLobby(String token, long lobbyId){
        Player player = playerService.getPlayerByToken(token);
        Lobby lobby = lobbyService.getLobbyById(lobbyId);
        if (player == null) {
            throw new UnauthorizedException("User is not a Player");
        }
        if (!lobby.getPlayers().contains(player)){
            throw new UnauthorizedException("Player is not in Lobby");
        }

    }

}


