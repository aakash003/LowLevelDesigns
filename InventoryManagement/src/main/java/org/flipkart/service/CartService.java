package org.flipkart.service;

import lombok.NoArgsConstructor;
import org.flipkart.entity.CartItem;
import org.flipkart.entity.Item;
import org.flipkart.entity.User;
import org.flipkart.helper.ServiceFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@NoArgsConstructor
public class CartService {
    private Map<String, List<CartItem>> cartMap = new HashMap<>();
    private static final org.apache.logging.log4j.Logger log = org.apache.logging.log4j.LogManager.getLogger(CartService.class);

    private UserService userService = ServiceFactory.getUserService();
    private InventoryManagerService inventoryManagerService = ServiceFactory.getInventoryManager();
    private ItemManagerService itemManagerService = ServiceFactory.getItemManager();


    public void addItemToCart(String name, String category, String brand, int quantity) {
        log.info("Adding item to cart");
        Item item = itemManagerService.getItem(brand, category);

        User user = userService.getUser(name);

        if (item == null) {
            log.error("Item not found");
            return;
        }
        if (user == null) {
            log.error("User not found");
            return;
        }

        if(!inventoryManagerService.checkInventory(item, quantity)){
            log.error("Item not available in inventory");
            return;
        }

        if (!cartMap.containsKey(name)) {
            List<CartItem> cartItemList = new ArrayList<>();
            cartItemList.add(new CartItem(item, quantity, user));
            cartMap.put(name, cartItemList);
            return;
        }
        List<CartItem> cartItemList = cartMap.get(name);
        boolean itemFound = false;
        for (CartItem cartItem : cartItemList) {
            Item item1 = cartItem.getItem();
            if (item1.getBrand().equals(brand) && item1.getCategory().equals(category)) {
                cartItem.setQuantity(cartItem.getQuantity() + quantity);
                itemFound = true;
                break;
            }
        }
        if (!itemFound) {
            cartItemList.add(new CartItem(item, quantity, user));
        }
        cartMap.put(name, cartItemList);
    }


    public void updateItemQuantity(String name, String category, String brand, int quantity) {
        log.info("Updating item quantity in cart");
        List<CartItem> cartItemList = getCartItemList(name);
        if (cartItemList == null) {
            log.error("Cart not found");
            return;
        }
        boolean itemFound = false;
        for (CartItem cartItem : cartItemList) {
            Item item = cartItem.getItem();
            if (item.getBrand().equals(brand) && item.getCategory().equals(category)) {
                cartItem.setQuantity(quantity);
                itemFound = true;
                break;
            }
        }
        if (!itemFound) {
            log.error("Item not found in cart");
        }
    }


    public List<CartItem> getCartItemList(String name) {
        if(cartMap.containsKey(name)){
            return cartMap.get(name);
        }
        return null;
    }


}
