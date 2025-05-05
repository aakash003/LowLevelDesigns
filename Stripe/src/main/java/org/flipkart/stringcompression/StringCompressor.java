
import java.util.*;

public class StringCompressor {
    // Compress a string: firstChar + (length - 2) + lastChar
    private static String compressMinor(String minor) {
        int len = minor.length();
        return len <= 2 ? minor : minor.charAt(0) + String.valueOf(len - 2) + minor.charAt(len - 1);
    }

    public static String compress(String input, int minorParts) {
        String[] majorParts = input.split("/");
        List<String> part1Strings = new ArrayList<>();
        List<String[]> originalMinorParts = new ArrayList<>();
        List<List<String>> compressedMinorParts = new ArrayList<>();

        // Part 1 compression
        for (String major : majorParts) {
            String[] minors = major.split("\\\\.");
            originalMinorParts.add(minors);

            List<String> compressed = new ArrayList<>();
            for (String minor : minors) {
                if (!minor.isEmpty()) {
                    compressed.add(compressMinor(minor));
                }
            }

            compressedMinorParts.add(compressed);
            part1Strings.add(String.join(".", compressed));
        }

        String part1Result = String.join("/", part1Strings);

        // Part 2 compression
        List<String> part2Strings = new ArrayList<>();
        for (int i = 0; i < compressedMinorParts.size(); i++) {
            List<String> compressed = compressedMinorParts.get(i);
            String[] original = originalMinorParts.get(i);

            if (compressed.size() <= minorParts) {
                part2Strings.add(String.join(".", compressed));
            } else {
                List<String> resultMinors = new ArrayList<>(compressed.subList(0, minorParts - 1));

                StringBuilder merged = new StringBuilder();
                for (int j = minorParts - 1; j < original.length; j++) {
                    if (!original[j].isEmpty()) {
                        merged.append(original[j]);
                    }
                }

                resultMinors.add(compressMinor(merged.toString()));
                part2Strings.add(String.join(".", resultMinors));
            }
        }

        String part2Result = String.join("/", part2Strings);

        // Output
        System.out.println("After Part 1 compression:");
        System.out.println(part1Result);
        System.out.println("After Part 2 compression:");
        System.out.println(part2Result);

        return part2Result;
    }

    public static void main(String[] args) {
        System.out.println("### Your Test Case:");
        compress("stripe.com/payments/checkout/customer.john.doe", 2);
    }

}