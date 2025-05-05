import java.util.*;

public class CalculateShipping {
    public static void main(String[] args) {
        // Shipping cost tests
        String input = "US:UK:Fedex:5, CA:FR:DHL:10, FR:UK:UPS:6, UK:US:DHL:2, UK:FR:DHL:10";
        System.out.println(costOfPackages(input, "US", "UK")); // 5
        System.out.println(costOfPackages(input, "FR", "UK")); // 6
        System.out.println(costOfPackages(input, "US", "FR")); // 15

        // Bank transfer tests
        Map<String, Integer> balances = new HashMap<>();
        balances.put("AU", 80);
        balances.put("US", 140);
        balances.put("MX", 110);
        balances.put("SG", 120);
        balances.put("FR", 70);

        balanceAccounts(balances);
    }

    public static int costOfPackages(String input, String start, String end) {
        Map<String, Map<String, Integer>> graph = new HashMap<>();

        String[] routes = input.split(", ");
        for (String route : routes) {
            String[] parts = route.split(":");
            String src = parts[0];
            String dest = parts[1];
            int cost = Integer.parseInt(parts[3]);

            graph.computeIfAbsent(src, k -> new HashMap<>()).put(dest, cost);
        }

        Queue<Pair> pq = new PriorityQueue<>(Comparator.comparingInt(a -> a.cost));
        pq.offer(new Pair(start, 0));

        Map<String, Integer> best = new HashMap<>();
        best.put(start, 0);

        while (!pq.isEmpty()) {
            Pair cur = pq.poll();

            if (cur.country.equals(end)) {
                return cur.cost;
            }

            if (!graph.containsKey(cur.country)) continue;

            for (Map.Entry<String, Integer> entry : graph.get(cur.country).entrySet()) {
                String nextCountry = entry.getKey();
                int nextCost = cur.cost + entry.getValue();

                if (!best.containsKey(nextCountry) || nextCost < best.get(nextCountry)) {
                    best.put(nextCountry, nextCost);
                    pq.offer(new Pair(nextCountry, nextCost));
                }
            }
        }

        return -1; // Not reachable
    }

    public static void balanceAccounts(Map<String, Integer> balances) {
        final int THRESHOLD = 100;

        List<Account> surplus = new ArrayList<>();
        List<Account> deficit = new ArrayList<>();

        for (Map.Entry<String, Integer> entry : balances.entrySet()) {
            String country = entry.getKey();
            int balance = entry.getValue();
            if (balance > THRESHOLD) {
                surplus.add(new Account(country, balance - THRESHOLD));
            } else if (balance < THRESHOLD) {
                deficit.add(new Account(country, THRESHOLD - balance));
            }
        }

        int i = 0, j = 0;
        while (i < surplus.size() && j < deficit.size()) {
            Account from = surplus.get(i);
            Account to = deficit.get(j);

            int amount = Math.min(from.amount, to.amount);

            System.out.println("from: " + from.country + ", to: " + to.country + ", amount: " + amount);

            from.amount -= amount;
            to.amount -= amount;

            if (from.amount == 0) i++;
            if (to.amount == 0) j++;
        }
    }

    static class Pair {
        String country;
        int cost;

        public Pair(String c, int cost) {
            this.country = c;
            this.cost = cost;
        }
    }

    static class Account {
        String country;
        int amount;

        public Account(String c, int amount) {
            this.country = c;
            this.amount = amount;
        }
    }
}
