package org.flipkart;

/**
 * Implement a function tail(file_name, n) that prints the last n lines of a given text file.
 * You should not load the entire file into memory if it is very large.
 * The function should correctly handle cases where:
 * n = 0 → prints nothing
 * n is greater than the total number of lines → prints all lines
 * You may assume the file is text-based and contains multiple lines separated by newline characters (\n).
 * Demonstrate the function with test cases by writing content to a file and then calling tail with different values of n.
 */
import lombok.var;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.Deque;

public class TailReader {

    public static void tail(String fileName, int n) throws IOException {
        if (n <= 0) return;

        File file = new File(fileName);
        if (!file.exists() || file.isDirectory()) {
            System.out.println("Invalid file path.");
            return;
        }

        try (RandomAccessFile raf = new RandomAccessFile(file, "r")) {
            long fileLength = raf.length();
            if (fileLength == 0) return;

            long pointer = fileLength - 1;
            int lineCount = 0;
            StringBuilder lineBuilder = new StringBuilder();
            Deque<String> lines = new ArrayDeque<>();

            // Traverse file from end to start
            while (pointer >= 0) {
                raf.seek(pointer);
                int readByte = raf.readByte();

                if (readByte == '\n') {
                    // Capture a complete line (reverse because we are reading backward)
                    if (lineBuilder.length() > 0) {
                        lines.addFirst(lineBuilder.reverse().toString());
                        lineBuilder.setLength(0);
                        lineCount++;
                        if (lineCount == n) break;
                    }
                } else {
                    lineBuilder.append((char) readByte);
                }
                pointer--;
            }

            // Add the last line if file doesn’t end with '\n'
            if (lineBuilder.length() > 0 && lineCount < n) {
                lines.addFirst(lineBuilder.reverse().toString());
            }

            // Print the collected lines
            for (String line : lines) {
                System.out.println(line);
            }
        }
    }

    // --- Demo ---
    public static void main(String[] args) throws IOException {
        String fileName = "sample.txt";

        // Create a sample file
        createTestFile(fileName);

        System.out.println("---- tail(fileName, 3) ----");
        tail(fileName, 3);

        System.out.println("\n---- tail(fileName, 0) ----");
        tail(fileName, 0);

        System.out.println("\n---- tail(fileName, 20) ----");
        tail(fileName, 20);
    }

    private static void createTestFile(String fileName) throws IOException {
        try (var writer = java.nio.file.Files.newBufferedWriter(
                java.nio.file.Paths.get(fileName), StandardCharsets.UTF_8)) {
            for (int i = 1; i <= 10; i++) {
                writer.write("Line " + i);
                writer.newLine();
            }
        }
    }

}
