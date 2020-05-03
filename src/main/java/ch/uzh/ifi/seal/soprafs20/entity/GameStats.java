package ch.uzh.ifi.seal.soprafs20.entity;

import java.util.List;

import javax.persistence.Id;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.OneToOne;
import javax.persistence.Table;



@Entity
@Table(name = "STATS")
public class GameStats {
	
	final Long MAXTIME = 30l;
	
	@Id
	@Column(nullable = false, unique = false)
	@GeneratedValue
    private Long statsId;
	
	@Column(nullable = false, unique = false)
    private Long playerId;
	
	@Column(nullable = false, unique = false)
    private Long lobbyId;
	
	@Column(nullable = false)
	private Long score;
	
	@Column(nullable = false)
	private Long guessCount;
	
	@Column(nullable = false)
	private Long correctGuessCount;
	
	@Column(nullable = false)
	private Long teamPoints;
	
	@Column(nullable = false)
	private Long timeToGuess;
	
	@Column(nullable = false)
	private Long givenClues;
	
	@Column(nullable = false)
	private Long goodClues;
	
	@Column(nullable = false)
	private Long timeForClue;
	
	public GameStats()
	{}

	public GameStats(Long playerId, Long lobbyId) {
		this.playerId = playerId;
		this.lobbyId = lobbyId;
		this.guessCount = 0l;
		this.correctGuessCount = 0l;
		this.givenClues = 0l;
		this.goodClues = 0l;
		this.score = 0l;
		this.timeToGuess = 0l;
		this.timeForClue = 0l;
		this.teamPoints = 0l;
	}

	
	
	public Long getPlayerId() {
		return playerId;
	}

	public void setPlayerId(Long playerId) {
		this.playerId = playerId;
	}

	public Long getScore() {
		return score;
	}

	public void setScore(Long score) {
		this.score = score;
	}

	public Long getGuessCount() {
		return guessCount;
	}

	public void setGuessCount(Long guessCount) {
		this.guessCount = guessCount;
	}

	public Long getCorrectGuessCount() {
		return correctGuessCount;
	}

	public void setCorrectGuessCount(Long correctGuessCount) {
		this.correctGuessCount = correctGuessCount;
	}

	public Long getTimeToGuess() {
		return timeToGuess;
	}

	public void setTimeToGuess(Long timeToGuess) {
		this.timeToGuess = timeToGuess;
		if(this.timeToGuess > this.MAXTIME)
		{
			this.timeToGuess = this.MAXTIME;
		}
	}

	public Long getGivenClues() {
		return givenClues;
	}

	public void setGivenClues(Long givenClues) {
		this.givenClues = givenClues;
	}

	public Long getGoodClues() {
		return goodClues;
	}

	public void setGoodClues(Long goodClues) {
		this.goodClues = goodClues;
	}

	public Long getTimeForClue() {
		return timeForClue;
		
	}

	public void setTimeForClue(Long timeForClue) {
		this.timeForClue = timeForClue;
		if(this.timeForClue > this.MAXTIME)
		{
			this.timeForClue = this.MAXTIME;
		}
	}
	
	public Long getStatsId() {
		return statsId;
	}

	public void setStatsId(Long statsId) {
		this.statsId = statsId;
	}

	public Long getLobbyId() {
		return lobbyId;
	}

	public void setLobbyId(Long lobbyId) {
		this.lobbyId = lobbyId;
	}

	public Long getTeamPoints() {
		return teamPoints;
	}

	public void setTeamPoints(Long teamPoints) {
		this.teamPoints = teamPoints;
	}

	public void incCorrectGuessCount(Long i)
	{
		this.correctGuessCount+=i;
	}

	public void incGuessCount(Long i)
	{
		this.guessCount+=i;
	}
	
	public void incGivenClueCount(Long i)
	{
		this.givenClues+=i;
	}

	public void incGoodClueCount(Long i)
	{
		this.goodClues+=i;
	}


	public void decGoodClueCount(Long i) 
	{
		this.goodClues-=i;
	}
	
	public void addGuessTime(Long time) 
	{
		this.timeToGuess = (this.timeToGuess * (this.guessCount - 1) + time) / this.guessCount;
		if(this.timeToGuess > this.MAXTIME)
		{
			this.timeToGuess = this.MAXTIME;
		}
	}

	public void addClueTime(Long time) 
	{
		this.timeForClue = (this.timeForClue * (this.givenClues - 1) + time) / this.givenClues;
		if(this.timeForClue > this.MAXTIME)
		{
			this.timeForClue = this.MAXTIME;
		}
	}	
	
	public void calculateScore()
	{
		this.score = this.teamPoints * this.MAXTIME + this.correctGuessCount * (this.MAXTIME - this.timeToGuess) + this.goodClues * (this.MAXTIME - this.timeForClue); 
	}



}
