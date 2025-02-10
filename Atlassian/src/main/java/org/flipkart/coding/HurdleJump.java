package org.flipkart.coding;

import java.util.HashSet;
import java.util.Set;

public class HurdleJump {

    public static int countReachablePoints(int start, int end, Set<Integer> hurdles) {
        // DP array to store whether a point is reachable
        boolean[] dp = new boolean[end + 1];

        // Destination is always reachable
        dp[end] = true;

        // Iterate from end-1 to start
        for (int i = end - 1; i >= start; i--) {
            // If the current point is a hurdle, it's not reachable
            if (hurdles.contains(i)) {
                dp[i] = false;
                continue;
            }

            // Check if jumping forward 2 steps is possible
            if (i + 2 <= end && dp[i + 2]) {
                dp[i] = true;
            }
            // Check if jumping backward 2 steps is possible
            else if (i - 2 >= start && dp[i - 2]) {
                dp[i] = true;
            } else {
                dp[i] = false;
            }
        }

        // Count the number of reachable starting points
        int count = 0;
        for (int i = start; i <= end; i++) {
            if (dp[i]) {
                count++;
            }
        }

        return count;
    }

    public static void main(String[] args) {
        int start = 1;
        int end = 10;
        Set<Integer> hurdles = new HashSet<>();
        hurdles.add(1);
        hurdles.add(4);

        int result = countReachablePoints(start, end, hurdles);
        System.out.println("Number of reachable starting points: " + result);
    }
}