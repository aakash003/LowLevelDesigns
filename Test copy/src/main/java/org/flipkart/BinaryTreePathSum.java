package org.flipkart;

import java.util.*;

class BinaryTreePathSum {

    public static int bestSumAnyTreePath(List<Integer> parent, List<Integer> values) {
        // Build adjacency list (undirected — we'll track visited to avoid going back)
        Map<Integer, List<Integer>> graph = new HashMap<>();
        for (int i = 0; i < parent.size(); i++) {
            int p = parent.get(i);
            if (p == -1) continue; // skip root's dummy parent
            graph.computeIfAbsent(p, k -> new ArrayList<>()).add(i);
            graph.computeIfAbsent(i, k -> new ArrayList<>()).add(p);
        }

        int[] maxSum = {Integer.MIN_VALUE};
        dfs(graph, values, 0, -1, maxSum);
        return maxSum[0];
    }

    // Returns the best single-arm path sum starting at `node` going downward
    // (or in any direction, but not revisiting `parentNode`)
    private static int dfs(Map<Integer, List<Integer>> graph,
                           List<Integer> values,
                           int node, int parentNode, int[] maxSum) {
        int val = values.get(node);

        // Collect the best two child arms (we need top-2 to form a "V" path)
        int best1 = 0, best2 = 0; // 0 means "don't extend" (path can stop here)

        for (int neighbor : graph.getOrDefault(node, Collections.emptyList())) {
            if (neighbor == parentNode) continue; // don't go back up
            int childBest = dfs(graph, values, neighbor, node, maxSum);
            int gain = Math.max(0, childBest); // only extend if it helps
            if (gain >= best1) { best2 = best1; best1 = gain; }
            else if (gain > best2) { best2 = gain; }
        }

        // Best path passing through this node (could be a "V" using two arms)
        int pathThroughNode = val + best1 + best2;
        maxSum[0] = Math.max(maxSum[0], pathThroughNode);

        // Return the best single-arm extension upward to the parent
        return val + best1;
    }

    public static void main(String[] args) {
        List<Integer> parent = Arrays.asList(-1, 0, 1, 2, 0); // Fixed!
        List<Integer> values = Arrays.asList(5, 7, -10, 4, 15);
        System.out.println(bestSumAnyTreePath(parent, values)); // Output: 27
    }
}