package org.flipkart.coding;

import java.util.Arrays;

public class PaintersPartitionProblem {
    public static int splitArray(int[] arr, int k) {
        int low = Arrays.stream(arr).max().getAsInt();
        int high = Arrays.stream(arr).sum();
        int max = 0;
        int min = 0;
        while(low<=high) {
            int mid = low + (high-low)/2;
            int[] partial = isPossible(arr, k, mid);

            if(partial[0]==1) {
                max = mid;
                min = partial[1];
                high = mid-1;
            }
            else{
                low = mid+1;
            }
        }
        System.out.println("Max and Min -> " +max+" "+min);
        return max-min;
    }

    private static int[] isPossible(int[] arr, int k, int mid) {

        int count = 1;
        int sum = 0;
        int min = Integer.MAX_VALUE;
        for(int b : arr) {
            sum += b;
            if(sum>mid) {
                min = Math.min(min, sum-b);
                count++;
                sum = b;
            }
        }
        return new int[]{count<=k ? 1 : 0, Math.min(min, sum)};
    }
}
