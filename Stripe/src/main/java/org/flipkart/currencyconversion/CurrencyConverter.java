package org.flipkart.currencyconversion;

import java.util.*;

// "static void main" must be defined in a public class.
public class CurrencyConverter {
    public static void main(String[] args) {
        List<Node> data = new ArrayList<Node>();
        data.add(new Node("USD", "JPY", 110));
        data.add(new Node("USD", "AUD", 1.45));
        data.add(new Node("JPY", "GBP", 0.0070));
        System.out.println(getRatio("GBP", "AUD", data));
    }

    public static double getRatio(String start, String end, List<Node> data) {
        Map<String, Map<String, Double>> map = new HashMap();
        for (Node node : data) {
            if (!map.containsKey(node.start)) map.put(node.start, new HashMap());
            map.get(node.start).put(node.end, node.ratio);
            if (!map.containsKey(node.end)) map.put(node.end, new HashMap());
            map.get(node.end).put(node.start, 1.0 / node.ratio);
        }
        Queue<String> q = new LinkedList<>();
        Queue<Double> val = new LinkedList<>();
        q.offer(start);
        val.offer(1.0);
        Set<String> visited = new HashSet<>();
        while (!q.isEmpty()) {
            String cur = q.poll();
            double num = val.poll();
            if (visited.contains(cur)) continue;
            visited.add(cur);
            if (map.containsKey(cur)) {
                Map<String, Double> next = map.get(cur);
                for (String key : next.keySet()) {
                    if (!visited.contains(key)) {
                        q.offer(key);
                        if (key.equals(end)) return num * next.get(key);
                        val.offer(num * next.get(key));
                    }
                }
            }
        }
        return -1;
    }

    static class Node {
        String start;
        String end;
        double ratio;

        public Node(String s, String e, double r) {
            this.start = s;
            this.end = e;
            this.ratio = r;
        }
    }
}