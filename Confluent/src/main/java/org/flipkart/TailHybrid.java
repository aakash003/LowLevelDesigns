package org.flipkart;

import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.nio.file.*;

public class TailHybrid {

    private static final long MAP_THRESHOLD = 50 * 1024 * 1024; // 50 MB

    public static void tail(String fileName, int n) throws IOException {
        Path path = Paths.get(fileName);
        long fileSize = Files.size(path);

        if (n <= 0) return;

        if (fileSize > MAP_THRESHOLD) {
            // For large files → use MappedByteBuffer
            tailUsingMappedBuffer(path, n, fileSize);
        } else {
            // For smaller files → use RandomAccessFile
            tailUsingRandomAccess(path.toFile(), n);
        }
    }

    /** For smaller files (simple + reliable) */
    private static void tailUsingRandomAccess(File file, int n) throws IOException {
        try (RandomAccessFile raf = new RandomAccessFile(file, "r")) {
            long filePointer = raf.length() - 1;
            int newlines = 0;
            StringBuilder sb = new StringBuilder();

            for (; filePointer >= 0; filePointer--) {
                raf.seek(filePointer);
                int readByte = raf.readByte();

                if (readByte == '\n') {
                    newlines++;
                    if (newlines > n) break;
                }
                sb.append((char) readByte);
            }

            System.out.println(sb.reverse());
        }
    }

    /** For large files (zero-copy using FileChannel) */
    private static void tailUsingMappedBuffer(Path path, int n, long fileSize) throws IOException {
        try (FileChannel channel = FileChannel.open(path, StandardOpenOption.READ)) {
            long mapSize = Math.min(fileSize, 4 * 1024 * 1024); // map last 4 MB
            long position = Math.max(0, fileSize - mapSize);

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

            System.out.println(sb.reverse());
        }
    }

    /** ✅ Demo */
    public static void main(String[] args) throws IOException {
        String fileName = "demo_tail.txt";

        // Create a sample file
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
            for (int i = 1; i <= 20; i++) {
                writer.write("Line " + i);
                writer.newLine();
            }
        }

        System.out.println("=== Last 5 lines ===");
        tail(fileName, 5);

        System.out.println("=== Last 25 lines (more than file) ===");
        tail(fileName, 25);

        System.out.println("=== Last 0 lines ===");
        tail(fileName, 0);
    }
}


/**
 * Metric	Complexity
 * Time	O(k) where k = bytes from end until N newlines
 * Space	O(N) for the last N lines only
 * File I/O	Efficient: backward seek (or mmap)
 */