package org.flipkart.entity;


import java.util.ArrayList;
import java.util.List;

public class Game {
    private GameMode gameMode;

    @Override
    public String toString() {
        return "Game{" +
                "gameMode=" + gameMode.getName() +
                ", gameLocation=" + gameLocation.getName() +
                ", players=" + players.toString() +
                '}';
    }

    private GameLocation gameLocation;
    private List<Player> players = new ArrayList<>();

    public GameMode getGameMode() {
        return gameMode;
    }

    public void setGameMode(GameMode gameMode) {
        this.gameMode = gameMode;
    }

    public GameLocation getGameLocation() {
        return gameLocation;
    }

    public void setGameLocation(GameLocation gameLocation) {
        this.gameLocation = gameLocation;
    }

    public List<Player> getPlayers() {
        return players;
    }

    public void setPlayers(List<Player> players) {
        this.players = players;
    }



    public Game(GameMode gameMode, GameLocation gameLocation) {
        this.gameMode = gameMode;
        this.gameLocation = gameLocation;
    }

    public void addPlayer(Player player) {
        players.add(player);
    }

    // getters and setters
}