package ch.uzh.ifi.seal.soprafs20.rest.dto;

public class GuessGetDTO {
    private String guess;
    private boolean success;
    private int leftCards;
    private int wonCards;
    private int lostCards;


	public boolean isSuccess() {
		return success;
	}

	public void setSuccess(boolean success) {
		this.success = success;
	}

	public String getGuess() {
		return guess;
	}

	public void setGuess(String guess) {
		this.guess = guess;
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
	
	

}
