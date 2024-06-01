package org.flipkart.service;

import lombok.NoArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.flipkart.entity.Item;

import java.util.HashMap;
import java.util.Map;


@NoArgsConstructor
public class ItemManagerService {
    private static final Logger log = LogManager.getLogger(ItemManagerService.class);
    Map<String,Item> itemMap = new HashMap<>();

    public Item getItem(String brand,String category){
        return itemMap.get(brand+":"+category);
    }

    public void addItem(String brand,String category,double price){
        log.info("Adding item");
        itemMap.put(brand+":"+category,new Item(brand,category,price));
    }

    public void deleteItem(String brand,String category){
        log.info("Deleting item");
        itemMap.remove(brand+":"+category);
    }

    public void displayItem(String brand,String category){
        log.info("Displaying item");
        Item item = itemMap.get(brand+":"+category);
        if(item != null){
            log.info("Item found: "+item);
        }else{
            log.error("Item not found");
        }
    }

    public void updateItemPrice(String brand,String category,double price){
        log.info("Updating item price");
        Item item = itemMap.get(brand+":"+category);
        if(item != null){
            item.setPrice(price);
            log.info("Item price updated");
        }else{
            log.error("Item not found");
        }
    }

}
