package org.flipkart.helper;


import org.flipkart.service.MatchmakingEngine;

public class ServiceFactory {

    private static MatchmakingEngine matchmakingEngine;

    public static MatchmakingEngine getMatchMakingEngine() {
        if (matchmakingEngine == null)
            return new MatchmakingEngine();
        return matchmakingEngine;
    }
}
