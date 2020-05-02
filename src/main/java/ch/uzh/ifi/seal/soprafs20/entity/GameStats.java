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
	
	@Id
	@Column(nullable = false, unique = false)
    private Long PlayerId;
	
	@Column(nullable = false)
	private Long score;
	
	@Column(nullable = false)
	private Long guessCount;
	
	@Column(nullable = false)
	private Long correctGuessCount;
	
	@Column(nullable = false)
	private Long timeToGuess;
	
	@Column(nullable = false)
	private Long givenClues;
	
	@Column(nullable = false)
	private Long goodClues;
	
	@Column(nullable = false)
	private Long timeForClue;

	public Long getPlayerId() {
		return PlayerId;
	}

	public void setPlayerId(Long playerId) {
		PlayerId = playerId;
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
	
	public void incCorrectGuessCount(Long i)
	{
		this.correctGuessCount+=i;
	}

	public void incGuessCount(Long i)
	{
		this.guessCount+=i;
	}
	
	public void incGivenCount(Long i)
	{
		this.givenClues+=i;
	}

	public void incGoodCount(Long i)
	{
		this.goodClues+=i;
	}
	
}
