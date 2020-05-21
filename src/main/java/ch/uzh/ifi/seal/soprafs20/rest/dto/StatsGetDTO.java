package ch.uzh.ifi.seal.soprafs20.rest.dto;

public class StatsGetDTO {

	private Long playerId;
	private String playerName;
	private Long score;
	private Long guessCount;
	private Long correctGuessCount;
	private Long teamPoints;
	private Long timeToGuess;
	private Long givenClues;
	private Long goodClues;
	private Long timeForClue;
	
	
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
	public Long getTeamPoints() {
		return teamPoints;
	}
	public void setTeamPoints(Long teamPoints) {
		this.teamPoints = teamPoints;
	}
	public Long getTimeToGuess() {
		return timeToGuess;
	}
	public void setTimeToGuess(Long timeToGuess) {
		this.timeToGuess = timeToGuess;
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
	}
	public Long getPlayerId() {
		return playerId;
	}
	public void setPlayerId(Long playerId) {
		this.playerId = playerId;
	}
	public String getPlayerName() {
		return playerName;
	}
	public void setPlayerName(String playerName) {
		this.playerName = playerName;
	}

	

}
