package org.flipkart;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.flipkart.entity.SearchResponse;
import org.flipkart.service.*;
import org.flipkart.helper.ServiceFactory;

import java.util.List;

public class Main {
    private static final Logger log = LogManager.getLogger(Main.class);


    public static void main(String[] args) {
        UserService userService = ServiceFactory.getUserService();
        InventoryManagerService inventoryManagerService = ServiceFactory.getInventoryManager();
        ItemManagerService itemManagerService = ServiceFactory.getItemManager();
        CartService cartService = ServiceFactory.getCartService();
        CheckoutService checkoutService = ServiceFactory.getChekoutService();
        SearchService searchService = ServiceFactory.getSearchService();


        itemManagerService.addItem("Amul", "Milk", 100);
        itemManagerService.addItem("Amul", "Curd", 200);
        itemManagerService.addItem("Nestle", "Milk", 10);
        itemManagerService.addItem("Nestle", "Curd", 20);

        inventoryManagerService.addOrUpdateInventory("Amul", "Milk", 10);
        inventoryManagerService.addOrUpdateInventory("Amul", "Curd", 20);
        inventoryManagerService.addOrUpdateInventory("Nestle", "Milk", 5);
        inventoryManagerService.addOrUpdateInventory("Nestle", "Curd", 10);

        inventoryManagerService.displayInventory("Amul", "Milk");
        inventoryManagerService.displayAll();

        userService.createUser("Alice", "Bangalore 560017", 3000);
        userService.createUser("Bob", "Lahore 540017", 2000);

        cartService.addItemToCart("Alice", "Milk", "Amul", 5);
        cartService.addItemToCart("Alice", "Curd", "Amul", 10);


        inventoryManagerService.displayAll();

        checkoutService.checkOutService("Alice");
        inventoryManagerService.displayAll();

        List<SearchResponse> result = searchService.searchItem(List.of("Milk"), List.of("Amul", "Nestle"));
        for (SearchResponse searchResponse : result) {
            log.info(searchResponse.getBrand() + "," + searchResponse.getCategory() + "," + searchResponse.getPrice() + "," + searchResponse.getQuantity() + "\n");
        }

    }


}