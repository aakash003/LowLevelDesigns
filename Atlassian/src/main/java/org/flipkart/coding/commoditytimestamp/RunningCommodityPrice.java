package org.flipkart.coding.commoditytimestamp;

import java.util.*;


class RunningCommodityPrice {
    private final Map<Integer, Integer> timestampToPrice;
    private final TreeMap<Integer, Integer> priceFrequency;
    private int maxCommodityPrice;


    public RunningCommodityPrice() {
        this.timestampToPrice = new HashMap<>();
        this.priceFrequency = new TreeMap<>(Collections.reverseOrder());
        this.maxCommodityPrice = Integer.MIN_VALUE;
    }

    public void upsertCommodityPrice(int timestamp, int commodityPrice) {
        Integer oldPrice = timestampToPrice.put(timestamp, commodityPrice);

        if (oldPrice != null) {
            removeOldPrice(oldPrice);
        }

        priceFrequency.merge(commodityPrice, 1, Integer::sum);
        maxCommodityPrice = priceFrequency.firstKey();
    }

    public int getMaxCommodityPrice() {
        return maxCommodityPrice;
    }

    private void removeOldPrice(int oldPrice) {
        priceFrequency.computeIfPresent(oldPrice, (key, count) -> count == 1 ? null : count - 1);
    }

    public void deleteCommodityPrice(int timestamp) {
        Integer oldPrice = timestampToPrice.remove(timestamp);
        if (oldPrice != null) {
            removeOldPrice(oldPrice);
            maxCommodityPrice = priceFrequency.isEmpty() ? Integer.MIN_VALUE : priceFrequency.firstKey();
        }
    }

    public static void main(String[] args) {
        RunningCommodityPrice r = new RunningCommodityPrice();
        r.upsertCommodityPrice(4, 27);
        r.upsertCommodityPrice(6, 26);
        r.upsertCommodityPrice(9, 25);
        System.out.println(r.getMaxCommodityPrice()); // Output: 27
        r.deleteCommodityPrice(4);
        System.out.println(r.getMaxCommodityPrice()); // Output: 26
    }
}