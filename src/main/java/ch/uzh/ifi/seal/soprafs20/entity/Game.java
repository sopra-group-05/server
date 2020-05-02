package ch.uzh.ifi.seal.soprafs20.entity;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.*;


@Entity
@Table(name = "GAME")
public class Game {
	
	@Id
	@Column(nullable = false, unique = false)
    @GeneratedValue
    private Long GameId;

	@Column(nullable = false)
    private Boolean lastGuessSuccess;
	
	@Column(nullable = false)
    private String activeGuess;
	
	@Column(nullable = false)
	private int leftCards;
	
	@Column(nullable = false)
	private int wonCards;
	
	@Column(nullable = false)
	private int lostCards;

	@OneToMany
    private List<Clue> clues = new ArrayList<>();

	@Column
    private int comparingGuessCounter = 0;
	
	
	public Long getGameId() {
		return GameId;
	}


	public void setGameId(Long gameId) {
		GameId = gameId;
	}


	public Boolean getLastGuessSuccess() {
		return lastGuessSuccess;
	}


	public void setLastGuessSuccess(Boolean lastGuessSuccess) {
		this.lastGuessSuccess = lastGuessSuccess;
	}


	public String getActiveGuess() {
		return activeGuess;
	}


	public void setActiveGuess(String activeGuess) {
		this.activeGuess = activeGuess;
	}


	public int getLeftCards() {
		return leftCards;
	}


	public void setLeftCards(int leftCards) {
		this.leftCards = leftCards;
	}


	public int getWonCards() {
		return wonCards;
	}


	public void setWonCards(int wonCards) {
		this.wonCards = wonCards;
	}


	public int getLostCards() {
		return lostCards;
	}


	public void setLostCards(int lostCards) {
		this.lostCards = lostCards;
	}

	public void setClues(List<Clue> clues){this.clues = clues;}

	public List<Clue> getClues(){return this.clues;}

	public void addClue(Clue clue){this.clues.add(clue);}

    public int getComparingGuessCounter() {
        return comparingGuessCounter;
    }

    public void setComparingGuessCounter(int comparingGuessCounter) {
        this.comparingGuessCounter = comparingGuessCounter;
    }
}
