package org.flipkart.helper;

import org.flipkart.entity.User;
import org.flipkart.service.*;

public class ServiceFactory {

    private static InventoryManagerService inventoryManager;
    private static ItemManagerService itemManager;
    private static UserService userService;
    private static CartService cartService;
    private static SearchService searchService;
    private static CheckoutService checkoutService;

    public static InventoryManagerService getInventoryManager() {
        if (inventoryManager == null) {
            inventoryManager = new InventoryManagerService();
        }

        return inventoryManager;
    }

    public static ItemManagerService getItemManager() {
        if (itemManager == null) {
            itemManager = new ItemManagerService();
        }
        return itemManager;
    }

    public static UserService getUserService() {
        if (userService == null) {
            userService = new UserService();
        }
        return userService;
    }

    public static CartService getCartService() {
        if (cartService == null) {
            cartService = new CartService();
        }
        return cartService;
    }

    public static SearchService getSearchService() {
        if (searchService == null) {
            searchService = new SearchService();
        }
        return searchService;
    }

    public static CheckoutService getChekoutService() {
        if(checkoutService == null) {
            checkoutService = new CheckoutService();
        }
        return checkoutService;
    }
}
