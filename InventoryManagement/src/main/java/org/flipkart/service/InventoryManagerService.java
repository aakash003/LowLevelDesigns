package org.flipkart.service;

import lombok.NoArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.flipkart.entity.Item;

import java.util.HashMap;
import java.util.Map;

@NoArgsConstructor
public class InventoryManagerService {
    private static final Logger log = LogManager.getLogger(InventoryManagerService.class);
    Map<String, Integer> inventoryMap = new HashMap<>();

    public void addOrUpdateInventory(String brand, String category, int quantity) {
        log.info("Adding inventory");
        if (inventoryMap.containsKey(brand + ":" + category)) {
            inventoryMap.put(brand + ":" + category, inventoryMap.get(brand + ":" + category) + quantity);
        } else {
            inventoryMap.put(brand + ":" + category, quantity);
        }
    }

    public void deleteInventory(String brand, String category, int quantity) {
        log.info("Deleting inventory");
        if (inventoryMap.containsKey(brand + ":" + category)) {
            int currentQuantity = inventoryMap.get(brand + ":" + category);
            if (currentQuantity >= quantity) {
                inventoryMap.put(brand + ":" + category, currentQuantity - quantity);
            } else {
                log.error("Not enough quantity to delete");
            }
        } else {
            log.error("Inventory not found");
        }
    }

    public void displayInventory(String brand, String category) {
        log.info("Displaying inventory");
        Integer quantity = inventoryMap.get(brand + ":" + category);
        if (quantity != null) {
            log.info("Inventory found: " + quantity);
        } else {
            log.error("Inventory not found");
        }
    }

    public void displayAll() {
        log.info("Displaying inventory");
        log.info("Inventory found: " + inventoryMap);
    }

    public boolean checkInventory(Item item, int quantity) {
        if(inventoryMap.containsKey(item.getBrand() + ":" + item.getCategory())) {
            int currentQuantity = inventoryMap.get(item.getBrand() + ":" + item.getCategory());
            if (currentQuantity >= quantity) {
                return true;
            } else {
                log.error("Not enough quantity in inventory");
                return false;
            }
        } else {
            log.error("Inventory not found");
            return false;
        }
    }

    public int getInventory(String brand, String category) {
        log.info("Displaying inventory");
        Integer quantity = inventoryMap.get(brand + ":" + category);
        if (quantity != null) {
            log.info("Inventory found: " + quantity);
            return quantity;
        } else {
            log.error("Inventory not found");
        }
        return 0;
    }
}
