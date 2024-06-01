package org.flipkart.helper;

import org.flipkart.enums.Rank;
import org.flipkart.service.BookingService;
import org.flipkart.service.strategy.HighestRatedService;
import org.flipkart.service.strategy.IRankingStrategy;
import org.flipkart.service.UserService;

import java.util.HashMap;
import java.util.Map;

public class ServiceFactory {

    private static UserService userService = new UserService();
    private static Map<Rank, IRankingStrategy> strategyMap = new HashMap<>();
    private static BookingService bookingService = new BookingService();

    static {
        strategyMap.put(Rank.HIGHEST_RATED, new HighestRatedService());
    }

    public static UserService getUserService() {
        if (userService == null) {
            return new UserService();
        }
        return userService;
    }

    public static BookingService getBookingService() {
        if (bookingService == null) {
            return new BookingService();
        }
        return bookingService;
    }

    public static IRankingStrategy getRankingStrategy(Rank rank) {
        if (!strategyMap.containsKey(rank)) {
            throw new IllegalArgumentException("Invalid Rank");
        }
        return strategyMap.get(rank);
    }
}
