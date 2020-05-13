package ch.uzh.ifi.seal.soprafs20.rest.dto;

public class LobbyPostDTO {

    private String lobbyName;
    private Integer gameMode;
    private String language;

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
