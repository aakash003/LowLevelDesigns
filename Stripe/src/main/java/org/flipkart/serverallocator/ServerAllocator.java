package org.flipkart.serverallocator;

import java.util.*;

public class ServerAllocator {

    public static int nextServerNumber(List<Number> allocated) {
        Set<Integer> used = new HashSet<>();

        // Only consider positive integers
        for (Number num : allocated) {
            if (num instanceof Integer && num.intValue() > 0) {
                used.add(num.intValue());
            }
        }

        // Find the smallest missing positive integer
        int i = 1;
        while (used.contains(i)) {
            i++;
        }

        return i;
    }

    public static void main(String[] args) {
        System.out.println(nextServerNumber(Arrays.asList(5, 3, 1))); // 2
        System.out.println(nextServerNumber(Arrays.asList(5, 4, 1, 2))); // 3
        System.out.println(nextServerNumber(Arrays.asList(3, 2, 1))); // 4
        System.out.println(nextServerNumber(Arrays.asList(2, 3))); // 1
        System.out.println(nextServerNumber(new ArrayList<>())); // 1
        System.out.println(nextServerNumber(Arrays.asList(1,1.5,2,2.5,3,3.5,4,5,5.5))); // 6
        System.out.println(nextServerNumber(Arrays.asList(2.5))); // 1
    }
}