package ch.uzh.ifi.seal.soprafs20.rest.dto;

public class OverallRankDTO {
    private long id;
    private String username;
    private long score;
    private long correctGuesses;
    private long bestClues;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public long getScore() {
        return score;
    }

    public void setScore(long score) {
        this.score = score;
    }

    public long getCorrectGuesses() {
        return correctGuesses;
    }

    public void setCorrectGuesses(long correctGuesses) {
        this.correctGuesses = correctGuesses;
    }

    public long getBestClues() {
        return bestClues;
    }

    public void setBestClues(long bestClues) {
        this.bestClues = bestClues;
    }
}
