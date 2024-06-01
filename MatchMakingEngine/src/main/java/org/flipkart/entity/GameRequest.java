package org.flipkart.entity;


import java.util.HashSet;
import java.util.List;

public class GameRequest {


    private HashSet<GameMode> gameModes;
    private List<GameLocation> gameLocations;
    private String rankMatchCriteria;
    private List<Player> playerList;


    public HashSet<GameMode> getGameModes() {
        return gameModes;
    }

    public void setGameModes(HashSet<GameMode> gameModes) {
        this.gameModes = gameModes;
    }

    public List<GameLocation> getGameLocations() {
        return gameLocations;
    }

    public void setGameLocations(List<GameLocation> gameLocations) {
        this.gameLocations = gameLocations;
    }

    public String getRankMatchCriteria() {
        return rankMatchCriteria;
    }

    public void setRankMatchCriteria(String rankMatchCriteria) {
        this.rankMatchCriteria = rankMatchCriteria;
    }

    public List<Player> getPlayerList() {
        return playerList;
    }

    public void setPlayerList(List<Player> playerList) {
        this.playerList = playerList;
    }



    public GameRequest(List<Player> playerList, HashSet<GameMode> gameModes, List<GameLocation> gameLocations, String rankMatchCriteria) {
        this.playerList = playerList;
        this.gameModes = gameModes;
        this.gameLocations = gameLocations;
        this.rankMatchCriteria = rankMatchCriteria;
    }

    // getters and setters
}
