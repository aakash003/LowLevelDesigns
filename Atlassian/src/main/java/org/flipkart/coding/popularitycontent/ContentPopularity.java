package org.flipkart.coding.popularitycontent;

import java.util.*;

public class ContentPopularity {
    private final Map<Integer, Integer> contentIdToCount;
    private final TreeMap<Integer, Set<Integer>> countToContentId;

    public ContentPopularity() {
        this.contentIdToCount = new HashMap<>();
        this.countToContentId = new TreeMap<>();
    }

    public void increasePopularity(final Integer contentId) {
        impression(contentId, true);
    }

    public void decreasePopularity(final Integer contentId) {
        impression(contentId, false);
    }

    private void impression(final Integer contentId, boolean increase) {
        int val = increase ? 1 : -1;
        if (!contentIdToCount.containsKey(contentId)) {
            contentIdToCount.put(contentId, val);
            countToContentId.computeIfAbsent(val, k -> new HashSet<>()).add(contentId);
        } else {
            int count = contentIdToCount.get(contentId);
            contentIdToCount.merge(contentId, val, Integer::sum);
            countToContentId.get(count).remove(contentId);
            if (countToContentId.get(count).isEmpty()) {
                countToContentId.remove(count);
            }
            countToContentId.computeIfAbsent(count + val, k -> new HashSet<>()).add(contentId);
        }
    }

    public int mostPopular() {
        if (countToContentId.isEmpty()) {
            return -1;
        }
        return countToContentId.lastEntry().getValue().stream().findFirst().get();
    }

}