package org.flipkart;


import java.util.Scanner;

public class CustomerLog {

    public static int compute_penalty(String log, int closingTime) {
        String[] arr = log.split(" ");
        int penalty = 0;

// Before or at closing — penalize 'N' (store open but no customer)
        for (int i = 0; i < closingTime; i++) {
            if (arr[i].equals("N")) penalty++;
        }

// After closing — penalize 'Y' (store closed but customer comes)
        for (int i = closingTime; i < arr.length; i++) {
            if (arr[i].equals("Y")) penalty++;
        }

        return penalty;
    }


    public static int getClosingWithMinPenalty(String log) {
        String[] arr = log.split(" ");
        int n = arr.length;

        int[] prefixN = new int[n + 1];  // prefixN[i] = number of N's from 0 to i-1
        int[] suffixY = new int[n + 1];  // suffixY[i] = number of Y's from i to end

        // Build prefix sum for 'N'
        for (int i = 1; i <= n; i++) {
            prefixN[i] = prefixN[i - 1] + (arr[i - 1].equals("N") ? 1 : 0);
        }

        // Build suffix sum for 'Y'
        for (int i = n - 1; i >= 0; i--) {
            suffixY[i] = suffixY[i + 1] + (arr[i].equals("Y") ? 1 : 0);
        }

        // Find minimum penalty
        int minPenalty = Integer.MAX_VALUE;
        int bestTime = 0;

        for (int i = 0; i <= n; i++) {
            int penalty = prefixN[i] + suffixY[i];
            if (penalty < minPenalty) {
                minPenalty = penalty;
                bestTime = i;
            }
        }

        return bestTime;
    }


    public static List<Integer> getAllClosing(String log) {
        String[] arr = log.split(" ");
        List<Integer> closings = new ArrayList<>();
        Stack<Integer> beginIndexes = new Stack<>();
        int i = 0;
        while (i < arr.length) {
            if (arr[i].equals("BEGIN")) {
                beginIndexes.push(i);
            } else if (arr[i].equals("END") && !beginIndexes.isEmpty()) {
                int start = beginIndexes.pop() + 1;
                int end = i;
                StringBuilder subLog = new StringBuilder();

                for (int j = start; j < end; j++) {
                    subLog.append(arr[j]).append(" ");
                }

                // Trim and process
                int closingTime = getClosingWithMinPenalty(subLog.toString().trim());
                closings.add(closingTime);
            }
            i++;
        }

        return closings;
    }


    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        while (true) {
            String inp = scanner.nextLine();
            inp = inp.trim();
            String[] inpArr = inp.split("~");
            try {
                switch (inpArr[0]) {
                }
            } catch (RuntimeException runtimeException) {
                System.out.println(runtimeException);
            }
        }

    }


}