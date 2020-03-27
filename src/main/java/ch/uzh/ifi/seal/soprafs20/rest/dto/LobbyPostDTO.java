package ch.uzh.ifi.seal.soprafs20.rest.dto;

import ch.uzh.ifi.seal.soprafs20.constant.GameModeStatus;

public class LobbyPostDTO {

    private String lobbyName;
    private GameModeStatus gameMode;

    public String getLobbyName() {
        return lobbyName;
    }

    public void setLobbyName(String lobbyName) {
        this.lobbyName = lobbyName;
    }

    public GameModeStatus getGameMode() {
        return gameMode;
    }

    public void setGameMode(GameModeStatus gameMode) {
        this.gameMode = gameMode;
    }
}
