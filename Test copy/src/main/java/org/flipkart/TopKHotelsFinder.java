package org.flipkart;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TopKHotelsFinder {
    public static List<Integer> awardTopKHotels(String positiveKeywords, String negativeKeywords,
                                                List<Integer> hotelIds,
                                                List<String> reviews, int k) {
        // Prepare keyword sets
        Set<String> positiveWords = new HashSet<>(Arrays.asList(positiveKeywords.toLowerCase().split(" ")));
        Set<String> negativeWords = new HashSet<>(Arrays.asList(negativeKeywords.toLowerCase().split(" ")));

        // Hotel scores
        Map<Integer, Integer> hotelScore = new HashMap<>();
        Pattern pattern = Pattern.compile("\\w+"); // regex for clean words

        for (int i = 0; i < reviews.size(); i++) {
            int hotel = hotelIds.get(i);
            int score = hotelScore.getOrDefault(hotel, 0);

            Matcher matcher = pattern.matcher(reviews.get(i).toLowerCase());
            while (matcher.find()) {
                String word = matcher.group();
                if (positiveWords.contains(word)) score += 3;
                if (negativeWords.contains(word)) score -= 1;
            }
            hotelScore.put(hotel, score);
        }

        // Min-heap for top k
        PriorityQueue<Map.Entry<Integer, Integer>> pq = new PriorityQueue<>(
                (a, b) -> a.getValue().equals(b.getValue())
                        ? b.getKey().compareTo(a.getKey())  // smaller ID wins when scores equal
                        : a.getValue() - b.getValue()       // smaller score on top
        );

        for (Map.Entry<Integer, Integer> entry : hotelScore.entrySet()) {
            pq.offer(entry);
            if (pq.size() > k) {
                pq.poll(); // trim to k
            }
        }

        // Drain heap → currently lowest-to-highest
        List<Integer> result = new ArrayList<>();
        while (!pq.isEmpty()) {
            result.add(pq.poll().getKey());
        }

        // Reverse to get highest-to-lowest order
        Collections.reverse(result);

        return result;
    }
}
