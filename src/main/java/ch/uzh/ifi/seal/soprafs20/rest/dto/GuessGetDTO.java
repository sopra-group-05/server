package ch.uzh.ifi.seal.soprafs20.rest.dto;

public class GuessGetDTO {
    private String guess;
    private boolean success;

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

}
