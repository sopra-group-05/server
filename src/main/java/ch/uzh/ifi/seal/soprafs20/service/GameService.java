package ch.uzh.ifi.seal.soprafs20.service;

import ch.uzh.ifi.seal.soprafs20.constant.Language;
import ch.uzh.ifi.seal.soprafs20.constant.MysteryWordStatus;
import ch.uzh.ifi.seal.soprafs20.entity.Game;
import ch.uzh.ifi.seal.soprafs20.entity.Lobby;
import ch.uzh.ifi.seal.soprafs20.entity.MysteryWord;
import ch.uzh.ifi.seal.soprafs20.exceptions.ForbiddenException;
import ch.uzh.ifi.seal.soprafs20.repository.CardRepository;
import ch.uzh.ifi.seal.soprafs20.repository.GameRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * Card Service
 * This class is the "worker" and responsible for all functionality related to the Game
 * (e.g., it converts, modifies, deletes, finds). The result will be passed back to the caller.
 */
@Service
@Transactional
public class GameService {

    private final Logger log = LoggerFactory.getLogger(GameService.class);

    private final GameRepository gameRepository;

    @Autowired
    public GameService(@Qualifier("gameRepository") GameRepository gameRepository) {
        this.gameRepository = gameRepository;
    }
    
    /**
     * This method update the deck stats
     *
     * @param - the active game
     * @param - was the guess correct
     * 
     */
    public void updateLeftCards(Game game, boolean success)
    {

    	game.setLeftCards(game.getLeftCards()-1);
    	
    	if (success)
    	{
    		game.setWonCards(game.getWonCards()+1);
    	}
    	else
    	{
    		if (game.getWonCards()>0)
    		{
    			game.setWonCards(game.getWonCards()-1);
    		}
    		game.setLostCards(game.getLostCards()+1);
    	}
    }
    

    /**
     * This method will get a specific will compare the guess with the mystery word and update game repository
     *
     * @param - the active lobby
     * @param - the guess from the player
     */
    public void compareGuess(Lobby lobby, String guess)
    {
    	Game game = lobby.getGame();
    	
    	List<MysteryWord> mysteryWords = lobby.getDeck().getActiveCard().getMysteryWords();
    	for(MysteryWord w : mysteryWords)
    	{
    		if (w.getStatus() == MysteryWordStatus.IN_USE)
    		{
    			game.setActiveGuess(guess);
    			boolean success = guess.toLowerCase().equals(w.getWord().toLowerCase());
    			game.setLastGuessSuccess(success);
    			updateLeftCards(game,success);
    			game = gameRepository.save(game);
    		}
    	}
    }
    
    public String getGuess(Lobby lobby)
    {
    	return lobby.getGame().getActiveGuess();
    }
    
    public boolean getGuessSuccess(Lobby lobby)
    {
    	return lobby.getGame().getLastGuessSuccess();
    }
      
    public int getLeftCards(Lobby lobby)
    {
    	return lobby.getGame().getLeftCards();
    }
    
    public int getWonCards(Lobby lobby)
    {
    	return lobby.getGame().getWonCards();
    }
    
    public int getLostCards(Lobby lobby)
    {
    	return lobby.getGame().getLostCards();
    }  
        
    public Game createNewGame(Lobby lobby)
    {
    	Game game = new Game();
    	game.setLastGuessSuccess(false);
    	game.setActiveGuess("");
    	game.setLeftCards(lobby.getDeck().getCards().size());
    	game.setWonCards(0);
    	game.setLostCards(0);
    	game = gameRepository.save(game);
    	return game;
    }
    
    
    
}
