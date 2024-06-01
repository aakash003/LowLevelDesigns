package org.flipkart.service;

import lombok.NoArgsConstructor;
import org.flipkart.entity.Item;
import org.flipkart.entity.SearchResponse;
import org.flipkart.helper.ServiceFactory;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
public class SearchService {

    private static final org.apache.logging.log4j.Logger log = org.apache.logging.log4j.LogManager.getLogger(SearchService.class);
    private UserService userService = ServiceFactory.getUserService();
    private InventoryManagerService inventoryManagerService = ServiceFactory.getInventoryManager();
    private ItemManagerService itemManagerService = ServiceFactory.getItemManager();

    public List<SearchResponse> searchItem(List<String> categoryList, List<String> brandList) {
        List<SearchResponse> searchResponseList = new ArrayList<>();
        log.info("Searching item");
        for (String category : categoryList) {
            for (String brand : brandList) {
                Item item = itemManagerService.getItem(brand, category);
                if (item == null) {
                    log.error("Item not found");
                    return searchResponseList;
                }
                log.info("Item found");
                searchResponseList.add(new SearchResponse(item.getBrand(), item.getCategory(), item.getPrice(), inventoryManagerService.getInventory(item.getBrand(), item.getCategory())));
            }
        }
        return searchResponseList;
    }
}
