package org.flipkart.service;

import lombok.NoArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.flipkart.entity.CartItem;
import org.flipkart.entity.Item;
import org.flipkart.entity.User;
import org.flipkart.helper.ServiceFactory;

import java.util.List;

@NoArgsConstructor
public class CheckoutService {
    private static final Logger log = LogManager.getLogger(CheckoutService.class);

    private UserService userService = ServiceFactory.getUserService();
    private InventoryManagerService inventoryManagerService = ServiceFactory.getInventoryManager();
    private ItemManagerService itemManagerService = ServiceFactory.getItemManager();
    private CartService cartService = ServiceFactory.getCartService();

    public boolean checkOutService(String name){
        log.info("Checking out");
        if(!userService.checkUser(name)){
            log.error("User not found");
            return false;
        }

        User user = userService.getUser(name);
        List<CartItem> cartItemList = cartService.getCartItemList(name);

        //validate cartItem
        double total = 0;
        for(CartItem cartItem : cartItemList){
            Item item = cartItem.getItem();
            int quantity = cartItem.getQuantity();
            if(!inventoryManagerService.checkInventory(item,quantity)){
                log.error("Item not available in inventory");
                return false;
            }
            total += item.getPrice()*quantity;
            if(total>user.getBalance()){
                log.error("Not enough balance");
                return false;
            }
        }
        //Debit
        user.setBalance(user.getBalance()-total);
        for(CartItem cartItem : cartItemList){
            Item item = cartItem.getItem();
            int quantity = cartItem.getQuantity();
            inventoryManagerService.deleteInventory(item.getBrand(),item.getCategory(),quantity);
        }

        return true;
    }


}
