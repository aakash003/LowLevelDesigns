package org.flipkart.entity;

import org.flipkart.enums.Rank;

import java.util.HashSet;

public class Player {
    private String name;

    @Override
    public String toString() {
        return "Player{" +
                "name='" + name + '\'' +
                ", rank=" + rank +
                '}';
    }

    private Rank rank;
    private HashSet<GameMode> gameMode;

    public HashSet<GameMode> getGameMode() {
        return gameMode;
    }

    public void setGameMode(HashSet<GameMode> gameMode) {
        this.gameMode = gameMode;
    }

    public HashSet<GameLocation> getGameLocation() {
        return gameLocation;
    }

    public void setGameLocation(HashSet<GameLocation> gameLocation) {
        this.gameLocation = gameLocation;
    }

    public String getRankPreference() {
        return rankPreference;
    }

    public void setRankPreference(String rankPreference) {
        this.rankPreference = rankPreference;
    }

    private HashSet<GameLocation> gameLocation;
    private String rankPreference;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Rank getRank() {
        return rank;
    }

    public void setRank(Rank rank) {
        this.rank = rank;
    }



    public Player(String name, Rank rank) {
        this.name = name;
        this.rank = rank;
    }

    // getters and setters
}