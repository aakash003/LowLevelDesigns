package org.flipkart;

import java.util.*;
import java.util.stream.Stream;

public class CurrencyConversion {

    static class Node implements Comparable {
        String currency;
        double rate;
        List<String> path;

        public Node(String currency, double rate, List<String> path) {
            this.currency = currency;
            this.rate = rate;
            this.path = path;
        }

        @Override
        public int compareTo(Object o) {
            return Double.compare(this.rate, ((Node) o).rate); // PQ will pick the lowest rate first
        }
    }

    public static double getAbsoluteMinRate(String[][] pairs, double[] rates, String[] target, double amount) {
            // Build graph
            Map<String, List<Node>> graph = new HashMap<>();
            for (int i = 0; i < pairs.length; i++) {
                String from = pairs[i][0];
                String to = pairs[i][1];
                double rate = rates[i];
                graph.computeIfAbsent(from, k -> new ArrayList<>()).add(new Node(to, rate, List.of(from, to)));
                graph.computeIfAbsent(to, k -> new ArrayList<>()).add(new Node(from, 1 / rate, List.of(to, from)));
            }

            // Dijkstra's algorithm
            PriorityQueue<Node> pq = new PriorityQueue<>();
            pq.offer(new Node(target[0], 1.0, List.of(target[0]))); // Start with source currency
            Set<String> visited = new HashSet<>();

            while (!pq.isEmpty()) {
                Node current = pq.poll();
                if (visited.contains(current.currency)) continue;
                visited.add(current.currency);

                if (current.currency.equals(target[1])) {
                    return amount * current.rate; // Found target currency
                }

                for (Node neighbor : graph.getOrDefault(current.currency, Collections.emptyList())) {
                    if (!visited.contains(neighbor.currency)) {
                        pq.offer(new Node(neighbor.currency, current.rate * neighbor.rate,
                                Stream.concat(current.path.stream(), neighbor.path.stream()).toList()));
                    }
                }
            }

            throw new IllegalArgumentException("No conversion path found between " + target[0] + " and " + target[1]);
    }


    public static void main(String[] args) {
        System.out.println("╔══════════════════════════════════════════╗");
        System.out.println("║     Currency Conversion                  ║");
        System.out.println("╚══════════════════════════════════════════╝\n");

        String [][] pairs = {{"USD", "EUR"}, {"EUR", "GBP"}, {"USD", "JPY"}, {"JPY", "GBP"}};
        double [] rates = {0.85, 0.9, 110.0, 0.007};
        String []target = {"USD", "GBP"};
        double result = getAbsoluteMinRate(pairs, rates, target, 100);
        System.out.printf("Converted amount: %.2f %s → %.2f %s%n",
                100.0, target[0], result, target[1]);

        double result2 = getFastestRouteBFS(pairs, rates, target, 100);
        System.out.printf("[FASTEST]Converted amount (BFS): %.2f %s → %.2f %s%n",
                100.0, target[0], result2, target[1]);
    }

    public static double getFastestRouteBFS(String[][] pairs, double[] rates, String[] target, double amount) {
        // Build graph
        Map<String, List<Node>> graph = new HashMap<>();
        for (int i = 0; i < pairs.length; i++) {
            String from = pairs[i][0];
            String to = pairs[i][1];
            double rate = rates[i];
            graph.computeIfAbsent(from, k -> new ArrayList<>()).add(new Node(to, rate, List.of(from, to)));
            graph.computeIfAbsent(to, k -> new ArrayList<>()).add(new Node(from, 1 / rate, List.of(to, from)));
        }

        // BFS
        Queue<Node> queue = new LinkedList<>();
        queue.offer(new Node(target[0], 1.0, List.of(target[0]))); // Start with source currency
        Set<String> visited = new HashSet<>();

        while (!queue.isEmpty()) {
            Node current = queue.poll();
            if (visited.contains(current.currency)) continue;
            visited.add(current.currency);

            if (current.currency.equals(target[1])) {
                return amount * current.rate; // Found target currency
            }

            for (Node neighbor : graph.getOrDefault(current.currency, Collections.emptyList())) {
                if (!visited.contains(neighbor.currency)) {
                    queue.offer(new Node(neighbor.currency, current.rate * neighbor.rate,
                            Stream.concat(current.path.stream(), neighbor.path.stream()).toList()));
                }
            }
        }

        throw new IllegalArgumentException("No conversion path found between " + target[0] + " and " + target[1]);
    }
}

