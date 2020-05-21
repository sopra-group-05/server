package ch.uzh.ifi.seal.soprafs20.service;

import ch.uzh.ifi.seal.soprafs20.constant.MysteryWordStatus;
import ch.uzh.ifi.seal.soprafs20.entity.Game;
import ch.uzh.ifi.seal.soprafs20.entity.GameStats;
import ch.uzh.ifi.seal.soprafs20.entity.Lobby;
import ch.uzh.ifi.seal.soprafs20.entity.MysteryWord;
import ch.uzh.ifi.seal.soprafs20.repository.GameRepository;
import ch.uzh.ifi.seal.soprafs20.repository.StatsRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


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
    private final StatsRepository statsRepository;
    private final UserService userService;

    @Autowired
    public GameService(@Qualifier("gameRepository") GameRepository gameRepository, @Qualifier("statsRepository") StatsRepository statsRepository, UserService userService) {
        this.gameRepository = gameRepository;
        this.statsRepository = statsRepository;
        this.userService = userService;
    }
    
    /**
     * This method update the deck stats
     *
     * @param game - the active game
     * @param success - was the guess correct
     * 
     */
    public void updateLeftCards(Game game, boolean success, String guess)
    {

    	game.setLeftCards(game.getLeftCards()-1);
    	
    	if (success)
    	{
    		game.setWonCards(game.getWonCards()+1);
    	}
    	else
    	{
    		if ((game.getWonCards()>0)&&(!guess.isEmpty()))
    		{
    			game.setWonCards(game.getWonCards()-1);
    			game.setLostCards(game.getLostCards()+1);
    		}
    		game.setLostCards(game.getLostCards()+1);
    	}
    }
    

    /**
     * This method will get a specific will compare the guess with the mystery word and update game repository
     *
     * @param lobby - the active lobby
     * @param guess - the guess from the player
     */
    public void compareGuess(Lobby lobby, String guess, Long guesserId, Long timeToGuess)
    {
    	Game game = lobby.getGame();	
    	String mysteryWord = getMysteryWord(lobby);
		game.setActiveGuess(guess);
		boolean success = guess.equalsIgnoreCase(mysteryWord);
		game.setLastGuessSuccess(success);
		updateLeftCards(game,success,guess);
		updateGuesserStats(success,timeToGuess,guesserId,lobby.getId());
		updateTeamPoints(lobby.getId(),game);
		game = gameRepository.save(game);
		gameRepository.flush();
    }
    
    public void updateGuesserStats(boolean success, Long timeToGuess, Long playerId, Long lobbyId) 
    {
    
    	GameStats gameStats = statsRepository.findByPlayerIdAndLobbyId(playerId,lobbyId);
    	gameStats.incGuessCount(1L);
    	gameStats.addGuessTime(timeToGuess);
    	if(success)
    	{
    		gameStats.incCorrectGuessCount(1L);
    		userService.updateCorrectGuessCount(playerId, 1L);
    	}
    	gameStats.calculateScore();
    	statsRepository.save(gameStats);
    	statsRepository.flush();
	}
    
    
    
    public void updateClueGeneratorStats(boolean goodClue, Long timeForClue, Long playerId, Long lobbyId) 
    {
    	GameStats gameStats = statsRepository.findByPlayerIdAndLobbyId(playerId,lobbyId);
    	gameStats.incGivenClueCount(1L);
    	gameStats.addClueTime(timeForClue);
    	if(goodClue)
    	{
    		gameStats.incGoodClueCount(1L);
    		userService.updateBestClueCount(playerId, 1L);
    	}
    	gameStats.calculateScore();
    	statsRepository.save(gameStats);
    	statsRepository.flush();
	}
    

	public void reduceGoodClues(Long playerId, Long lobbyId) {
		GameStats gameStats = statsRepository.findByPlayerIdAndLobbyId(playerId,lobbyId);
    	gameStats.decGoodClueCount(1L);
    	userService.updateBestClueCount(playerId, -1L);
     	gameStats.calculateScore();
    	statsRepository.save(gameStats);
    	statsRepository.flush();
		
	}
	
	public void updateTeamPoints(Long lobbyId, Game game)
	{
		List<GameStats> allGameStats = statsRepository.findAllByLobbyId(lobbyId);
		for (GameStats eachGameStats : allGameStats)
		{
			eachGameStats.setTeamPoints(game.getWonCards());
			eachGameStats.calculateScore();
		}
		//if there are no more cards left, then update overall stats for each user
		if(game.getLeftCards() <= 0) {
		    for(GameStats playerGameStats : allGameStats) {
                userService.updateScore(playerGameStats.getPlayerId(), playerGameStats.getScore());
            }
        }
    	statsRepository.saveAll(allGameStats);
    	statsRepository.flush();
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
    
    public Long getWonCards(Lobby lobby)
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
    	game.setWonCards(0L);
    	game.setLostCards(0);
    	gameRepository.save(game);
    	gameRepository.flush();
    	return game;
    }

	public void addStats(Long playerId, Long lobbyId) {
        // check if GameStats for that playerID and Lobby already exists
        // if it doesn't exist create new GameStats
        GameStats existingGameStats = statsRepository.findByPlayerIdAndLobbyId(playerId, lobbyId);
        if (existingGameStats == null) {
            GameStats gameStats = new GameStats(playerId,lobbyId);
            statsRepository.save(gameStats);
            statsRepository.flush();
        }
	}

	public List<GameStats> getAllLobbyGameStats (Long lobbyId)
	{
		return (statsRepository.findAllByLobbyId(lobbyId));
	}

	public GameStats getPlayersStats(long playerId, long lobbyId) 
	{
		return (statsRepository.findByPlayerIdAndLobbyId(playerId,lobbyId));
	}

	/**
     * returns list of all Game statistics the player played in all Lobbies of their Games
     * */
	public List<GameStats> getPlayersStats(Long playerId) {
        return statsRepository.findAllByPlayerId(playerId);
    }

	public String getMysteryWord(Lobby lobby) 
	{
			
    	List<MysteryWord> mysteryWords = lobby.getDeck().getActiveCard().getMysteryWords();
    	
    	for(MysteryWord w : mysteryWords)
    	{   		
    		if (w.getStatus() == MysteryWordStatus.IN_USE)
    		{
    			return(w.getWord());		
      		}
    	}
    	return("");

	}

	public void deleteGame(Game lobbyGame) {
		this.gameRepository.delete(lobbyGame);
	}
    
    
}
