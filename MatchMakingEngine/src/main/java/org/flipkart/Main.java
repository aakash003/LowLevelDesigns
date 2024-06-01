package org.flipkart;


import org.flipkart.entity.*;
import org.flipkart.enums.Rank;
import org.flipkart.helper.ServiceFactory;
import org.flipkart.service.MatchmakingEngine;

import java.util.*;

public class Main {


    public static void main(String[] args) {

        MatchmakingEngine matchmakingEngine = ServiceFactory.getMatchMakingEngine();

        //Initialize players
        Player p1 = new Player("player1", Rank.BRONZE);
        Player p2 = new Player("player2", Rank.BRONZE);
        Player p3 = new Player("player3", Rank.BRONZE);
        Player p4 = new Player("player4", Rank.BRONZE);
        Player p5 = new Player("player5", Rank.SILVER);
        Player p6 = new Player("player6", Rank.GOLD);
        Player p7 = new Player("player7", Rank.GOLD);
        Player p8 = new Player("player8", Rank.PLATINUM);
        Player p9 = new Player("player9", Rank.DIAMOND);

        //Initialise GameModes
        GameMode TwoVTwo = new GameMode("TwoVTwo", 4);
        GameMode FastDraw = new GameMode("FastDraw", 2);
        GameMode Raid = new GameMode("Raid", 6);

        HashMap<String,GameMode>gameModeHashMap = new HashMap<>();
        gameModeHashMap.put("TwoVTwo",TwoVTwo);
        gameModeHashMap.put("FastDraw",FastDraw);
        gameModeHashMap.put("Raid",Raid);

        matchmakingEngine.setGameModeHashMap(gameModeHashMap);

        //Initialise GameRequests
        Map<String, GameRequest> gameRequestMap = new HashMap<>();
        gameRequestMap.put("a", new GameRequest(List.of(p8, p9), new HashSet<>(Arrays.asList(FastDraw)), List.of(new GameLocation("CastleTown"), new GameLocation("AirBase")), "Any"));
        gameRequestMap.put("b", new GameRequest(List.of(p1, p2), new HashSet<>(Arrays.asList(TwoVTwo)), List.of(new GameLocation("CastleTown"), new GameLocation("AirBase")), "Same"));
        gameRequestMap.put("c", new GameRequest(List.of(p3), new HashSet<>(Arrays.asList(TwoVTwo, Raid)), List.of(new GameLocation("SavageLand"), new GameLocation("AirBase")), "Same"));
        gameRequestMap.put("d", new GameRequest(List.of(p4), new HashSet<>(Arrays.asList(TwoVTwo, Raid)), List.of(new GameLocation("SavageLand"), new GameLocation("AirBase")), "Any"));
        gameRequestMap.put("e", new GameRequest(List.of(p5, p6, p7), new HashSet<>(Arrays.asList(TwoVTwo, Raid)), List.of(new GameLocation("SavageLand")), "Any"));


        Scanner scanner = new Scanner(System.in);

        while (true) {
            String inp = scanner.nextLine();
            inp = inp.trim();
            String[] inpArr = inp.split(" ");
            try {
                switch (inpArr[0]) {
                    case "Request": {
                        GameRequest gameRequest = gameRequestMap.get(inpArr[1]);
                        matchmakingEngine.joinGame(gameRequest);
                    }
                    break;
                    case "Concurrent": {
                        inp = inp.substring(inp.indexOf(" ") + 1);
                        String[] inpArr2 = inp.split(",");
                        List<GameRequest> gameRequestList = new ArrayList<>();
                        for (String index : inpArr2) {
                            GameRequest gameRequest = gameRequestMap.get(index);
                            gameRequestList.add(gameRequest);
                        }


                        matchmakingEngine.joinGame(gameRequestList);

                        for (List<Game> waitingPool : matchmakingEngine.getWaitingPool()) {
                            StringBuilder sb = new StringBuilder();
                            sb.append("Waiting pool: ");
                            for (Game game : waitingPool) {
                                sb.append(game.getPlayers().toString());
                            }
                            System.out.println(sb.toString());
                        }
                    }
                    break;
                    default:
                        System.out.println("Invalid Command");
                }
            } catch (RuntimeException runtimeException) {
                System.out.println(runtimeException);
            }
        }

    }


}