package ch.uzh.ifi.seal.soprafs20.rest.dto;

public class CluePostDTO {
    private String hint;
    private String hint2;
    private Long timeForClue;

    public String getHint() {
        return hint;
    }

    public void setHint(String hint){
        this.hint = hint;
    }

	public String getHint2() {
		return hint2;
	}

	public void setHint2(String hint2) {
		this.hint2 = hint2;
	}


	public Long getTimeForClue() {
		return timeForClue;
	}

	public void setTimeForClue(Long timeForClue) {
		this.timeForClue = timeForClue;
	}
        
}
