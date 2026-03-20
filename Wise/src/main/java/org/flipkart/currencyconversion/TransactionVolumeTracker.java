package org.flipkart.currencyconversion;

import java.math.*;
import java.util.*;

// ─────────────────────────────────────────────
// 1. MODELS
// ─────────────────────────────────────────────

public class TransactionVolumeTracker {

    // ── Transaction — now includes explicit rate ──
    public record Transaction(
            BigDecimal amount,
            String     convertFrom,
            String     convertTo,
            BigDecimal rate          // 1 convertFrom = rate convertTo
    ) {
        public Transaction {
            Objects.requireNonNull(convertFrom, "convertFrom cannot be null");
            Objects.requireNonNull(convertTo,   "convertTo cannot be null");
            Objects.requireNonNull(amount,      "amount cannot be null");
            Objects.requireNonNull(rate,         "rate cannot be null");

            if (convertFrom.isBlank() || convertTo.isBlank())
                throw new IllegalArgumentException("Currency code cannot be blank");

            if (convertFrom.equalsIgnoreCase(convertTo))
                throw new IllegalArgumentException("Cannot convert currency to itself");

            if (amount.compareTo(BigDecimal.ZERO) <= 0)
                throw new IllegalArgumentException("Amount must be positive");

            if (rate.compareTo(BigDecimal.ZERO) <= 0)
                throw new IllegalArgumentException("Rate must be positive");

            if (convertFrom.length() != 3 || convertTo.length() != 3)
                throw new IllegalArgumentException("Currency code must be 3 characters");
        }

        public String currencyPair() {
            return convertFrom + "/" + convertTo;
        }
    }

    // ── Edge in currency graph ────────────────────
    public record Edge(
            String     to,
            BigDecimal rate,
            String     from    // kept for path reconstruction
    ) {}

    // ── Dijkstra node ─────────────────────────────
    // Comparable so PriorityQueue orders by best rate
    private record DijkstraNode(
            String     currency,
            double     logRate,   // accumulated -log(rate) → minimize = maximize product
            List<String> path     // currencies visited so far
    ) implements Comparable<DijkstraNode> {

        @Override
        public int compareTo(DijkstraNode other) {
            // Min-heap on logRate → smallest -log = largest product
            return Double.compare(this.logRate, other.logRate);
        }
    }

    // ── Conversion result ─────────────────────────
    public record ConversionResult(
            String        from,
            String        to,
            BigDecimal    inputAmount,
            BigDecimal    outputAmount,
            List<String>  path,          // e.g. ["GBP", "USD", "INR"]
            BigDecimal    effectiveRate   // total rate applied
    ) {
        @Override
        public String toString() {
            return String.format(
                "%s %.2f → %s %.2f | path: %s | rate: %.6f",
                from, inputAmount, to, outputAmount,
                String.join(" → ", path), effectiveRate
            );
        }
    }

    // ─────────────────────────────────────────────
    // 2. STATE
    // ─────────────────────────────────────────────

    private final Map<String, BigDecimal>    volumeMap;
    private final Deque<Transaction>         window;
    private final int                        N;
    private final int                        k;

    public TransactionVolumeTracker(int N, int k) {
        this.N         = N;
        this.k         = k;
        this.volumeMap = new HashMap<>();
        this.window    = new ArrayDeque<>();
    }

    // ─────────────────────────────────────────────
    // 3. SLIDING WINDOW MANAGEMENT
    // ─────────────────────────────────────────────

    public void addTransaction(Transaction tx) {
        window.addLast(tx);
        volumeMap.merge(tx.currencyPair(), tx.amount(), BigDecimal::add);
        if (window.size() > N) {
            evictOldest();
        }
    }

    private void evictOldest() {
        Transaction dropped = window.pollFirst();
        BigDecimal updated  = volumeMap.get(dropped.currencyPair())
                                       .subtract(dropped.amount());
        if (updated.compareTo(BigDecimal.ZERO) == 0) {
            volumeMap.remove(dropped.currencyPair());
        } else {
            volumeMap.put(dropped.currencyPair(), updated);
        }
    }

    // ─────────────────────────────────────────────
    // 4. TOP K BY VOLUME
    // ─────────────────────────────────────────────

    public List<Map.Entry<String, BigDecimal>> topK() {
        PriorityQueue<Map.Entry<String, BigDecimal>> minHeap = new PriorityQueue<>(
            Math.max(1, k),
            Comparator.comparing(Map.Entry<String, BigDecimal>::getValue)
                      .thenComparing(Map.Entry::getKey)
        );

        for (Map.Entry<String, BigDecimal> entry : volumeMap.entrySet()) {
            minHeap.offer(entry);
            if (minHeap.size() > k) minHeap.poll();
        }

        List<Map.Entry<String, BigDecimal>> result = new ArrayList<>();
        while (!minHeap.isEmpty()) result.add(minHeap.poll());
        Collections.reverse(result); // highest volume first
        return result;
    }

    // ─────────────────────────────────────────────
    // 5. GRAPH BUILDER
    //    Builds directed adjacency list from window
    //    Edge weight = explicit rate on transaction
    // ─────────────────────────────────────────────

    private Map<String, List<Edge>> buildGraph() {
        Map<String, List<Edge>> graph = new HashMap<>();

        for (Transaction tx : window) {
            String     from = tx.convertFrom();
            String     to   = tx.convertTo();
            BigDecimal rate = tx.rate();

            // Add directed edge: from → to
            graph.computeIfAbsent(from, x -> new ArrayList<>())
                 .add(new Edge(to, rate, from));

            // Ensure 'to' node exists even if no outgoing edges
            graph.computeIfAbsent(to, x -> new ArrayList<>());
        }

        return graph;
    }

    // ─────────────────────────────────────────────
    // 6. CONVERT — Dijkstra for max product of rates
    //
    //    maximize(r1 * r2 * r3)
    //    = maximize(log(r1) + log(r2) + log(r3))
    //    = minimize(-log(r1) - log(r2) - log(r3))
    //    → standard Dijkstra with -log(rate) as cost
    // ─────────────────────────────────────────────

    public Optional<ConversionResult> convert(
            String     from,
            String     to,
            BigDecimal amount
    ) {
        // Validate inputs
        Objects.requireNonNull(from,   "from cannot be null");
        Objects.requireNonNull(to,     "to cannot be null");
        Objects.requireNonNull(amount, "amount cannot be null");

        if (from.equalsIgnoreCase(to))
            throw new IllegalArgumentException("from and to cannot be the same");

        if (amount.compareTo(BigDecimal.ZERO) <= 0)
            throw new IllegalArgumentException("Amount must be positive");

        Map<String, List<Edge>> graph = buildGraph();

        // Source or target not in graph at all
        if (!graph.containsKey(from) || !graph.containsKey(to)) {
            return Optional.empty();
        }

        // ── Dijkstra setup ────────────────────────────
        // bestLogRate[currency] = best (minimum) -log(rate) found so far
        Map<String, Double> bestLogRate = new HashMap<>();
        graph.keySet().forEach(c -> bestLogRate.put(c, Double.MAX_VALUE));
        bestLogRate.put(from, 0.0);

        // Min-heap ordered by accumulated -log(rate)
        PriorityQueue<DijkstraNode> pq = new PriorityQueue<>();
        pq.offer(new DijkstraNode(from, 0.0, List.of(from)));

        // ── Dijkstra loop ─────────────────────────────
        while (!pq.isEmpty()) {
            DijkstraNode current = pq.poll();

            // Already found a better path to this node
            if (current.logRate() > bestLogRate.get(current.currency())) {
                continue;
            }

            // Reached destination
            if (current.currency().equals(to)) {
                // Reconstruct result
                double     totalLogRate    = current.logRate();
                BigDecimal effectiveRate   = BigDecimal.valueOf(Math.exp(-totalLogRate))
                                                       .setScale(6, RoundingMode.HALF_UP);
                BigDecimal convertedAmount = amount.multiply(effectiveRate)
                                                   .setScale(2, RoundingMode.HALF_UP);

                return Optional.of(new ConversionResult(
                    from,
                    to,
                    amount,
                    convertedAmount,
                    current.path(),
                    effectiveRate
                ));
            }

            // Explore neighbours
            List<Edge> neighbours = graph.getOrDefault(current.currency(), List.of());
            for (Edge edge : neighbours) {

                // -log(rate) — minimizing this maximizes product of rates
                double edgeCost    = -Math.log(edge.rate().doubleValue());
                double newLogRate  = current.logRate() + edgeCost;

                if (newLogRate < bestLogRate.get(edge.to())) {
                    bestLogRate.put(edge.to(), newLogRate);

                    // Build new path
                    List<String> newPath = new ArrayList<>(current.path());
                    newPath.add(edge.to());

                    pq.offer(new DijkstraNode(edge.to(), newLogRate, newPath));
                }
            }
        }

        // No path found
        return Optional.empty();
    }

    // ─────────────────────────────────────────────
    // 7. DEMO
    // ─────────────────────────────────────────────

    public static void main(String[] args) {

        TransactionVolumeTracker tracker = new TransactionVolumeTracker(10, 3);

        // ── Add transactions with explicit rates ──
        //                           amount        from    to     rate
        tracker.addTransaction(new Transaction(new BigDecimal("800"),  "GBP", "USD", new BigDecimal("2.0")));
        tracker.addTransaction(new Transaction(new BigDecimal("200"),  "USD", "INR", new BigDecimal("83.0")));
        tracker.addTransaction(new Transaction(new BigDecimal("300"),  "EUR", "USD", new BigDecimal("1.1")));
        tracker.addTransaction(new Transaction(new BigDecimal("150"),  "GBP", "EUR", new BigDecimal("1.15")));
        tracker.addTransaction(new Transaction(new BigDecimal("100"),  "EUR", "INR", new BigDecimal("90.0")));
        tracker.addTransaction(new Transaction(new BigDecimal("50"),   "USD", "EUR", new BigDecimal("0.91")));

        // ── Top K by volume ───────────────────────
        System.out.println("═══ Top 3 Currency Pairs by Volume ═══");
        tracker.topK().forEach(e ->
            System.out.printf("  %-10s → %s%n", e.getKey(), e.getValue()));

        // ── Conversion queries ────────────────────
        System.out.println("\n═══ Conversion Queries ═══");

        List<String[]> queries = List.of(
            new String[]{"GBP", "INR",  "500"},   // GBP→USD→INR
            new String[]{"GBP", "INR",  "500"},   // GBP→EUR→INR (compare paths)
            new String[]{"EUR", "INR",  "100"},   // direct edge
            new String[]{"GBP", "EUR",  "200"},   // direct edge
            new String[]{"INR", "GBP",  "1000"},  // no path — directed graph
            new String[]{"USD", "INR",  "300"}    // direct
        );

        for (String[] q : queries) {
            String     from   = q[0];
            String     to     = q[1];
            BigDecimal amount = new BigDecimal(q[2]);

            Optional<ConversionResult> result = tracker.convert(from, to, amount);

            System.out.printf("%n  %s → %s  (amount: %s)%n", from, to, amount);
            result.ifPresentOrElse(
                r -> System.out.println("  ✅ " + r),
                ()  -> System.out.println("  ❌ No conversion path found")
            );
        }

        // ── Sliding window eviction ───────────────
        System.out.println("\n═══ Adding 5 more transactions (window=10) ═══");
        for (int i =