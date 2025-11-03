package org.flipkart;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * You are given a list of documents with id and text.
 * Eg :- DocId, Text
 * 1, "Cloud computing is the on-demand availability of computer system resources."
 * 2, "One integrated service for metrics uptime cloud monitoring dashboards and alerts reduces time spent navigating between systems."
 * 3, "Monitor entire cloud infrastructure, whether in the cloud computing is or in virtualized data centers." Search a given phrase in all the documents in a efficient manner. Assume that you have more than 1 million docs.
 * Eg :- search("cloud") >> This should output [1,2,3] search("cloud monitoring") >> This should output [2] search("Cloud computing is") >> This should output [1,3]
 */


public class SearchEngine {

    private final Map<String, Map<Integer, List<Integer>>> invertedIndex = new ConcurrentHashMap<>();
    private static final Pattern TOKEN_PATTERN = Pattern.compile("\\w+");

    // Build index at initialization
    public SearchEngine(Map<Integer, String> documents) {
        buildIndex(documents);
    }

    private void buildIndex(Map<Integer, String> documents) {
        documents.forEach((docId, text) -> {
            List<String> tokens = tokenize(text);
            for (int pos = 0; pos < tokens.size(); pos++) {
                String token = tokens.get(pos);
                invertedIndex
                        .computeIfAbsent(token, k -> new ConcurrentHashMap<>())
                        .computeIfAbsent(docId, k -> new ArrayList<>())
                        .add(pos);
            }
        });
    }

    private List<String> tokenize(String text) {
        List<String> tokens = new ArrayList<>();
        Matcher matcher = TOKEN_PATTERN.matcher(text.toLowerCase());
        while (matcher.find()) tokens.add(matcher.group());
        return tokens;
    }

    public List<Integer> search(String phrase) {
        List<String> tokens = tokenize(phrase);
        if (tokens.isEmpty()) return Collections.emptyList();

        // Start with first token’s doc set
        Set<Integer> candidateDocs = new HashSet<>(invertedIndex.getOrDefault(tokens.get(0), Map.of()).keySet());

        // Intersect with subsequent tokens’ docs
        for (int i = 1; i < tokens.size(); i++) {
            candidateDocs.retainAll(invertedIndex.getOrDefault(tokens.get(i), Map.of()).keySet());
            if (candidateDocs.isEmpty()) return Collections.emptyList();
        }

        // Phrase verification
        List<Integer> results = new ArrayList<>();
        for (Integer docId : candidateDocs) {
            if (isPhrasePresent(docId, tokens)) results.add(docId);
        }

        return results;
    }

    private boolean isPhrasePresent(int docId, List<String> tokens) {
        List<List<Integer>> positions = new ArrayList<>();
        for (String token : tokens) {
            List<Integer> posList = invertedIndex.get(token).get(docId);
            if (posList == null) return false;
            positions.add(posList);
        }

        // Check for consecutive position alignment
        for (int startPos : positions.get(0)) {
            boolean match = true;
            for (int i = 1; i < tokens.size(); i++) {
                if (!positions.get(i).contains(startPos + i)) {
                    match = false;
                    break;
                }
            }
            if (match) return true;
        }
        return false;
    }

    // --- Testing ---
    public static void main(String[] args) {
        Map<Integer, String> docs = Map.of(
                1, "Cloud computing is the on-demand availability of computer system resources.",
                2, "One integrated service for metrics uptime cloud monitoring dashboards and alerts reduces time spent navigating between systems.",
                3, "Monitor entire cloud infrastructure, whether in the cloud computing is or in virtualized data centers."
        );

        SearchEngine engine = new SearchEngine(docs);

        System.out.println("Search 'cloud' -> " + engine.search("cloud")); // [1,2,3]
        System.out.println("Search 'cloud monitoring' -> " + engine.search("cloud monitoring")); // [2]
        System.out.println("Search 'Cloud computing is' -> " + engine.search("Cloud computing is")); // [1,3]
    }
}

/**
 * Operation	Time Complexity	Space Complexity	Notes
 * Build Index	O(T)	O(V + T)	One-time cost
 * Search (1 token)	O(k)	—	k = docs containing that token
 * Search (phrase)	O(m * f * d)	—	m = words in phrase
 * Tokenize (search phrase)	O(m)	—	negligible
 */