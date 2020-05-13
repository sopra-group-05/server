package ch.uzh.ifi.seal.soprafs20.rest.dto;

public class LobbyPostDTO {

    private String lobbyName;
    private Integer gameMode;
    private String language;
    private int numberOfCards;
    private int numberOfBots;

    public int getNumberOfCards() {
        return numberOfCards;
    }

    public void setNumberOfCards(int numberOfCards) {
        this.numberOfCards = numberOfCards;
    }

    public int getNumberOfBots() {
        return numberOfBots;
    }

    public void setNumberOfBots(int numberOfBots) {
        this.numberOfBots = numberOfBots;
    }

    public String getLobbyName() {
        return lobbyName;
    }

    public void setLobbyName(String lobbyName) {
        this.lobbyName = lobbyName;
    }

    public Integer getGameMode() {
        return gameMode;
    }

    public void setGameMode(Integer gameMode) {
        this.gameMode = gameMode;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }
}
