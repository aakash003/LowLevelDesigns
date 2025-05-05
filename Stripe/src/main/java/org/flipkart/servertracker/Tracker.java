

import java.util.*;

public class Tracker {
    private static class HostTracker {
        TreeSet<Integer> available = new TreeSet<>();
        int nextNumber = 1;
    }

    // Map of hostType -> HostTracker
    private final Map<String, HostTracker> trackerMap = new HashMap<>();

    // Allocate a new server name
    public String allocate(String hostType) {
        HostTracker ht = trackerMap.computeIfAbsent(hostType, k -> new HostTracker());

        int number;
        if (!ht.available.isEmpty()) {
            number = ht.available.pollFirst(); // Smallest available
        } else {
            number = ht.nextNumber++;
        }

        return hostType + number;
    }

    // Deallocate a previously allocated hostname
    public void deallocate(String hostname) {
        // Find the index where digits start
        int i = hostname.length() - 1;
        while (i >= 0 && Character.isDigit(hostname.charAt(i))) {
            i--;
        }

        if (i == hostname.length() - 1) {
            // No number found at end
            return;
        }

        String hostType = hostname.substring(0, i + 1);
        int number;
        try {
            number = Integer.parseInt(hostname.substring(i + 1));
        } catch (NumberFormatException e) {
            return;
        }

        if (number <= 0) return;

        HostTracker ht = trackerMap.computeIfAbsent(hostType, k -> new HostTracker());

        // Avoid adding duplicates (e.g., double deallocate)
        if (number < ht.nextNumber) {
            ht.available.add(number);
        }
    }

    // Optional: for debugging or testing
    public Map<String, List<Integer>> getAvailableMap() {
        Map<String, List<Integer>> result = new HashMap<>();
        for (var entry : trackerMap.entrySet()) {
            result.put(entry.getKey(), new ArrayList<>(entry.getValue().available));
        }
        return result;
    }
}


public class Main {
    public static void main(String[] args) {
        Tracker tracker = new Tracker();

        System.out.println(tracker.allocate("apibox")); // apibox1
        System.out.println(tracker.allocate("apibox")); // apibox2
        tracker.deallocate("apibox1");
        System.out.println(tracker.allocate("apibox")); // apibox1
        System.out.println(tracker.allocate("sitebox")); // sitebox1
        System.out.println(tracker.allocate("#$@%")); // #$@%1
    }
}
