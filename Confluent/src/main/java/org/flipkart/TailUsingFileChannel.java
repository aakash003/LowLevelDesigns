package org.flipkart;

import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.nio.file.*;

public class TailUsingFileChannel {

    public static void tail(String fileName, int n) throws IOException {
        Path path = Paths.get(fileName);
        long fileSize = Files.size(path);

        // Open file channel for reading
        try (FileChannel channel = FileChannel.open(path, StandardOpenOption.READ)) {
            long mapSize = Math.min(fileSize, 4 * 1024 * 1024); // map last 4MB
            long position = fileSize - mapSize;

            // Map only the last 4MB of file
            MappedByteBuffer buffer = channel.map(FileChannel.MapMode.READ_ONLY, position, mapSize);

            int newlines = 0;
            StringBuilder sb = new StringBuilder();

            for (int i = (int) mapSize - 1; i >= 0; i--) {
                char c = (char) buffer.get(i);
                if (c == '\n') {
                    newlines++;
                    if (newlines > n) break;
                }
                sb.append(c);
            }

            // Reverse the collected characters to get correct order
            System.out.println(sb.reverse());
        }
    }

    public static void main(String[] args) throws IOException {
        String fileName = "test_large.log";
        tail(fileName, 5);
    }
}
