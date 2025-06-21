package org.flipkart.coding;

import java.util.*;



public class CandidateVotes {
    static class Candidate {
        String name;
        int totalVotes;
        int firstVoteIndex;

        public Candidate(String name, int firstVoteIndex) {
            this.name = name;
            this.totalVotes = 1; // Start with 1 vote since this is the first occurrence
            this.firstVoteIndex = firstVoteIndex;
        }

        public void addVote() {
            totalVotes++;
        }
    }


    public static void main(String[] args) {
        String[] votes = {"Alice", "Bob", "Alice", "Bob", "Charlie", "Charlie", "Charlie", "Alice", "Bob", "Bob"};

        // Step 1: Count votes and track first vote index
        Map<String, Candidate> candidates = new HashMap<>();
        for (int i = 0; i < votes.length; i++) {
            String name = votes[i];
            if (!candidates.containsKey(name)) {
                candidates.put(name, new Candidate(name, i)); // First time seeing this candidate
            } else {
                candidates.get(name).addVote(); // Increment vote count for existing candidate
            }
        }

        // Step 2: Find the maximum vote count
        int maxVotes = candidates.values().stream().mapToInt(c -> c.totalVotes).max().orElse(0);

        // Step 3: Use a priority queue to break ties based on the earliest vote
        PriorityQueue<Candidate> heap = new PriorityQueue<>(Comparator.comparingInt(c -> c.firstVoteIndex));

        for (Candidate candidate : candidates.values()) {
            if (candidate.totalVotes == maxVotes) {
                heap.offer(candidate); // Add tied candidates to the heap
            }
        }

        // Step 4: The candidate at the top of the heap is the winner (earliest first vote)
        Candidate winner = heap.poll();
        System.out.println("Winner: " + winner.name + " with " + winner.totalVotes + " votes.");
    }
}