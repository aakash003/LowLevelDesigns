package org.flipkart;


import org.flipkart.entity.Restaurant;
import org.flipkart.helper.ServiceFactory;
import org.flipkart.service.PlatformService;
import org.flipkart.service.UserService;

import java.util.*;

public class Main {

    public static void main(String[] args) {
        PlatformService platformService = ServiceFactory.getPlatformService();
        UserService userService = ServiceFactory.getUserService();

        Scanner scanner = new Scanner(System.in);

        while (true) {
            String inp = scanner.nextLine();
            inp = inp.trim();
            String[] inpArr = inp.split("\\(");
            List<String> items = Arrays.asList(inpArr[1].replaceAll("[\\(\\) \" ]", "" ).split("\\s*,\\s*"));
            try {
                switch (inpArr[0]) {
                    case "register_user": {
                        String name = items.get(0).substring(1, items.get(0).length() - 1);
                        String email = items.get(1).substring(1, items.get(1).length() - 1);
                        String mobile = items.get(2).substring(1, items.get(2).length() - 1);
                        String address = items.get(3).substring(1, items.get(3).length() - 1);
                        userService.signUp(name, email, mobile, address);
                    }
                    break;
                    case "login_user": {
                        String mobile = items.get(0).substring(1, items.get(0).length() - 1);;
                        mobile = mobile.replace("\"", "");
                        userService.login(mobile);
                    }
                    break;
                    case "register_restaurant": {
                        String name = items.get(0).substring(1, items.get(0).length() - 1);
                        String address = items.get(1).substring(1, items.get(1).length() - 1);
                        String foodItem = items.get(2).substring(1, items.get(2).length() - 1);
                        double price = Double.valueOf(items.get(3));
                        int quantity = Integer.valueOf(items.get(4));

                        platformService.registerRestaurant(name, address, foodItem, price, quantity);
                    }
                    break;
                    case "logout": {
                        userService.logout();
                    }
                    break;
                    case "show_restaurant": {
                        if(items.get(0).substring(1, items.get(0).length() - 1).equalsIgnoreCase("price")){
                            List<Restaurant>restaurantList = platformService.showRestaurantsBasedOnPrice();
                            for (Restaurant restaurant : restaurantList) {
                                System.out.println("Name=" + restaurant.getName() + " " + "Address=" + restaurant.getAddress() + " " + "FoodItem=" + restaurant.getFoodItem() + " " + "Price=" + restaurant.getPrice() + " " + "Quantity=" + restaurant.getQuantity() + "Review=" + restaurant.getReviewAsString()+ "Rating=" + restaurant.getRating());
                            }
                        }else if(items.get(0).substring(1, items.get(0).length() - 1).equalsIgnoreCase("rating")){
                            List<Restaurant>restaurantList = platformService.showRestaurantsBasedOnRating();
                            for (Restaurant restaurant : restaurantList) {
                                System.out.println("Name=" + restaurant.getName() + " " + "Address=" + restaurant.getAddress() + " " + "FoodItem=" + restaurant.getFoodItem() + " " + "Price=" + restaurant.getPrice() + " " + "Quantity=" + restaurant.getQuantity() + "Review=" + restaurant.getReviewAsString()+ "Rating=" + restaurant.getRating());
                            }
                        }else{
                            List<Restaurant>restaurantList = platformService.showRestaurants();
                            for (Restaurant restaurant : restaurantList) {
                                System.out.println("Name=" + restaurant.getName() + " " + "Address=" + restaurant.getAddress() + " " + "FoodItem=" + restaurant.getFoodItem() + " " + "Price=" + restaurant.getPrice() + " " + "Quantity=" + restaurant.getQuantity() + "Review=" + restaurant.getReviewAsString()+ "Rating=" + restaurant.getRating());
                            }
                        }
                    }
                    break;
                    case "place_order": {
                        String name = items.get(0).substring(1, items.get(0).length() - 1);
                        int quantity = Integer.valueOf(items.get(1));
                        platformService.placeOrder(name,quantity);
                    }
                    break;
                    case "create_review": {
                        String name = items.get(0).substring(1, items.get(0).length() - 1);
                        double rating = Double.valueOf(items.get(1));
                        String review = items.get(2).substring(1, items.get(2).length() - 1);
                        platformService.createReview(name,rating,review);
                    }
                    break;
                    case "update_quantity": {
                        String name = items.get(0).substring(1, items.get(0).length() - 1);
                        int quantity = Integer.valueOf(items.get(1));
                        platformService.updateRestaurantQuantity(name,quantity);
                    }
                    break;
                    case "update_location": {
                        String name = items.get(0).substring(1, items.get(0).length() - 1);;
                        String loc = items.get(1).substring(1, items.get(1).length() - 1);;
                        platformService.updateRestaurantLocation(name,loc);
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