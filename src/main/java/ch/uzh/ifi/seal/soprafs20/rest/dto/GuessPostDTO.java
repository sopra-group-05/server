package ch.uzh.ifi.seal.soprafs20.rest.dto;

public class GuessPostDTO {
    private String guess;
    private Long timeToGuess;

	public String getGuess() {
		return guess;
	}

	public void setGuess(String guess) {
		this.guess = guess;
	}

	public Long getTimeToGuess() {
		return timeToGuess;
	}

	public void setTimeToGuess(Long timeToGuess) {
		this.timeToGuess = timeToGuess;
	}
}
