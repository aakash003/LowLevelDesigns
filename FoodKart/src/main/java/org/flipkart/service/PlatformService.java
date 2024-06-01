package org.flipkart.service;


import org.flipkart.entity.Order;
import org.flipkart.entity.Restaurant;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.flipkart.entity.User;
import org.flipkart.helper.ServiceFactory;

import java.util.*;


public class PlatformService {

    private static final Logger log = LogManager.getLogger(PlatformService.class);

    private Map<String, Restaurant> restaurantMap = new HashMap<>();

    private UserService userService = ServiceFactory.getUserService();

    public List<Restaurant> showRestaurants() {
        return new ArrayList<>(restaurantMap.values());
    }

    public List<Restaurant> showRestaurantsBasedOnPrice() {
        Map<Double, Restaurant> map = new HashMap<>();
        for (Map.Entry<String, Restaurant> entry : restaurantMap.entrySet()) {
            map.put(entry.getValue().getPrice(), entry.getValue());
        }
        List<Restaurant> result = new ArrayList<>(map.values());

        Collections.reverse(result);

        return result;
    }

    public List<Restaurant> showRestaurantsBasedOnRating() {
        Map<Double, Restaurant> map = new HashMap<>();
        for (Map.Entry<String, Restaurant> entry : restaurantMap.entrySet()) {
            map.put(entry.getValue().getRating(), entry.getValue());
        }
        List<Restaurant> result = new ArrayList<>(map.values());

        Collections.reverse(result);

        return result;
    }


    public void registerRestaurant(String name, String address, String foodItem, double price, int quantity) {
        // create new restaurant and add to restaurants list
        Restaurant restaurant = new Restaurant(name, address, foodItem, price, quantity);
        restaurantMap.put(name, restaurant);
        log.info("Restaurant registered successfully");
    }

    public void placeOrder(String name, int quantity) {
        // place order for restaurant
        Restaurant restaurant = restaurantMap.get(name);
        if (restaurant.getQuantity() < quantity) {
            log.error("Not enough quantity available");
            return;
        }
        User user = userService.getActiveUser();
        Order order = new Order(restaurant.getName(),name, quantity, user.getMobile(), restaurant.getPrice() * quantity);
        restaurant.setQuantity(restaurant.getQuantity() - quantity);

        log.info("Order placed successfully");
    }

    public void createReview(String name, double rating, String review) {
        // create review for restaurant
        Restaurant restaurant = restaurantMap.get(name);

        double rating1 = restaurant.getRating();
        rating = (rating1 + rating) / 2;
        restaurant.setRating(rating);

        restaurant.getReview().add(review);
        log.info("Review created successfully");
    }

    public void updateRestaurantQuantity(String name, int quantity) {
        // update quantity for restaurant
        Restaurant restaurant = restaurantMap.get(name);
        restaurant.setQuantity(quantity);
        log.info("Quantity updated successfully");
    }

    public void updateRestaurantLocation(String name, String loc) {
        // update location for restaurant
        Restaurant restaurant = restaurantMap.get(name);
        restaurant.setAddress(loc);
        log.info("Location updated successfully");
    }
}