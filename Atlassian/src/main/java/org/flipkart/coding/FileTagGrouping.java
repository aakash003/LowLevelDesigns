package org.flipkart.coding;

import java.util.*;

public class FileTagGrouping {

    static class File {
        String name;
        long size;
        Set<String> tags;

        File(String name, long size, Set<String> tags) {
            this.name = name;
            this.size = size;
            this.tags = tags;
        }
    }

    public static List<String> getTopTags(List<File> files, int topN) {
        // Map to store the total file size for each tag
        Map<String, Long> tagSizeMap = new HashMap<>();

        // Iterate through each file
        for (File file : files) {
            // Iterate through each tag of the file
            for (String tag : file.tags) {
                // Update the total size for the tag
                tagSizeMap.put(tag, tagSizeMap.getOrDefault(tag, 0L) + file.size);
            }
        }

        // Convert the map to a list of entries
        List<Map.Entry<String, Long>> tagSizeList = new ArrayList<>(tagSizeMap.entrySet());

        // Sort the list in descending order of file size
        tagSizeList.sort((entry1, entry2) -> entry2.getValue().compareTo(entry1.getValue()));

        // Extract the top N tags
        List<String> topTags = new ArrayList<>();
        for (int i = 0; i < Math.min(topN, tagSizeList.size()); i++) {
            topTags.add(tagSizeList.get(i).getKey());
        }

        return topTags;
    }

    public static void main(String[] args) {
        // Example files with tags
        List<File> files = new ArrayList<>();
        files.add(new File("file1.txt", 100, new HashSet<>(Arrays.asList("tag1", "tag2"))));
        files.add(new File("file2.txt", 200, new HashSet<>(Arrays.asList("tag2", "tag3"))));
        files.add(new File("file3.txt", 150, new HashSet<>(Arrays.asList("tag1", "tag3"))));
        files.add(new File("file4.txt", 300, new HashSet<>(Arrays.asList("tag3"))));

        // Get top 2 tags based on file size
        List<String> topTags = getTopTags(files, 2);

        // Print the result
        System.out.println("Top tags: " + topTags);
    }
}