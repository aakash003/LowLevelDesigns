package org.flipkart.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.flipkart.entity.*;

import java.util.*;
import java.util.stream.Collectors;

public class MatchmakingEngine {
    private List<Game> playingPool = new ArrayList<>();
    private HashMap<String, GameMode> gameModeHashMap = new HashMap<>();

    public List<Game> getPlayingPool() {
        return playingPool;
    }

    public List<List<Game>> getWaitingPool() {
        return allWaitingPool;
    }

    //private List<Game> waitingPool = new ArrayList<>();
    private static final Logger log = LogManager.getLogger(MatchmakingEngine.class);

    private List<List<Game>> allWaitingPool = new ArrayList<>();

    public void joinGame(GameRequest gameRequest) {
        if (gameRequest == null) {
            log.error("Game request is null");
            return;
        }

        List<Player> playerList = gameRequest.getPlayerList();
        HashSet<GameMode> gameModeSet = gameRequest.getGameModes();
        //check themselves
        for (GameMode gameMode : gameModeSet) {
            if (gameMode.getRequiredPlayers() == playerList.size()) {
                Game game = new Game(gameMode, gameRequest.getGameLocations().get(0));
                game.setPlayers(playerList);
                playingPool.add(game);
                log.info("Game moved to playing pool: " + game.toString());
                return;
            }
        }

        List<Game> waitingPool = new ArrayList<>();

        //common location in all the players in request

        //check in waiting pool
        for (Player player : playerList) {
            //setting player preferences in game
            player.setGameMode(gameModeSet);
            player.setRankPreference(gameRequest.getRankMatchCriteria());
            player.setGameLocation(new HashSet<>(gameRequest.getGameLocations()));

            Game game = findSuitableGame(player, gameModeSet, waitingPool);
            if (game != null) {
                addPlayerToGame(player, game, waitingPool);
            } else {
                createNewGame(player, gameModeSet, gameRequest.getGameLocations().get(0), waitingPool);
            }
        }
        if (waitingPool.size() > 0)
            allWaitingPool.add(waitingPool);
    }

    private Game findSuitableGame(Player player, HashSet<GameMode> gameModeSet, List<Game> waitingPool) {

        //check in current waiting pool
        for (Game game : waitingPool) {
            if (isGameSuitable(game, player, gameModeSet)) {
                return game;
            }
        }
        //check in current all-waiting pool
        for (List<Game> wait : allWaitingPool) {

            for (Game game : wait) {
                List<Player> players = game.getPlayers();
                updateGame(game, players, player);
                if (isGameSuitable(game, player, gameModeSet)) {
                    return game;
                }
            }

        }
        return null;
    }

    private void updateGame(Game game, List<Player> players1, Player playerRequestingJoin) {
        List<Player> players = new ArrayList<>(players1);
        players.add(playerRequestingJoin);
        //update game mode
        List<List<String>> gameModeStringList = new ArrayList<>();
        List<Integer> commons = new ArrayList<Integer>();
        for (Player player : players) {
            gameModeStringList.add(player.getGameMode().stream().map(GameMode::getName).collect(Collectors.toList()));
        }
        List<String> commonElements = gameModeStringList.stream()
                .reduce((s1, s2) -> {
                    s1.retainAll(s2);
                    return s1;
                }).orElse(Collections.emptyList());

        game.setGameMode(gameModeHashMap.get(commonElements.get(0)));

        //update location
        List<List<String>> locationStringList = new ArrayList<>();
        List<Integer> commons1 = new ArrayList<Integer>();
        for (Player player : players) {
            locationStringList.add(player.getGameLocation().stream().map(GameLocation::getName).collect(Collectors.toList()));
        }
        List<String> commonElements1 = gameModeStringList.stream()
                .reduce((s1, s2) -> {
                    s1.retainAll(s2);
                    return s1;
                }).orElse(Collections.emptyList());

        game.setGameLocation(new GameLocation(commonElements1.get(0)));
    }


    private boolean isGameSuitable(Game game, Player player, HashSet<GameMode> gameModeSet) {
        return game.getPlayers().size() < game.getGameMode().getRequiredPlayers()
                && gameModeSet.contains(game.getGameMode())
                && checkRankMatch(game, player);
    }

    private void addPlayerToGame(Player player, Game game, List<Game> waitingPool) {
        game.addPlayer(player);
        if (game.getPlayers().size() == game.getGameMode().getRequiredPlayers()) {
            moveToPlayingPool(game, waitingPool);
        }
        //log.info("Player " + player.getName() + " added to game with game mode: " + game.getGameMode().getName());
    }

    private void moveToPlayingPool(Game game, List<Game> waitingPool) {
        playingPool.add(game);
        waitingPool.remove(game);
        // Iterate over allWaitingPool and remove the game from the list it belongs to
        for (List<Game> wait : allWaitingPool) {
            if (wait.remove(game)) {
                // If the game was successfully removed from a list, stop the iteration
                break;
            }
        }
        log.info("Game moved to playing pool: " + game.toString());
    }

    private void createNewGame(Player player, HashSet<GameMode> gameModeSet, GameLocation gameLocation, List<Game> waitingPool) {
        GameMode gameMode = getLowestPlayerCountGameMode(gameModeSet);
        Game game = new Game(gameMode, gameLocation);
        game.addPlayer(player);
        waitingPool.add(game);
        //log.info("New game created and player " + player.getName() + " added to it");
    }

    private GameMode getLowestPlayerCountGameMode(HashSet<GameMode> gameModeSet) {
        GameMode lowestPlayerCountGameMode = null;
        int lowestPlayerCount = Integer.MAX_VALUE;
        for (GameMode gameMode : gameModeSet) {
            int playerCount = (int) playingPool.stream().filter(game -> game.getGameMode().equals(gameMode)).count();
            if (playerCount < lowestPlayerCount) {
                lowestPlayerCount = playerCount;
                lowestPlayerCountGameMode = gameMode;
            }
        }
        return lowestPlayerCountGameMode;
    }

    private boolean checkRankMatch(Game game, Player player) {
        return game.getPlayers().stream().allMatch(p -> p.getRank().equals(player.getRank()));
    }

    public void joinGame(List<GameRequest> gameRequestList) {
        for (GameRequest request : gameRequestList) {
            joinGame(request);
        }
    }

    public void setGameModeHashMap(HashMap<String, GameMode> gameModeHashMap) {
        this.gameModeHashMap = gameModeHashMap;
    }
}
