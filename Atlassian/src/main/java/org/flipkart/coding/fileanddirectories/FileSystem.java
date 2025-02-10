package org.flipkart.coding.fileanddirectories;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

public class FileSystem {
    public static void main(String[] args) {
        // Add new Dir - Dir1
        DirectoryNode dir1 = new DirectoryNode("Dir1");

        // Add new file - file1
        dir1.addFile("file1", 100.5);
        dir1.addFile("file2", 150);
        DirectoryNode dir2 = dir1.addSubDir("dir2");

        DirectoryNode dir5 = new DirectoryNode("Dir5");
        DirectoryNode dir51 = dir5.addSubDir("dir51");
        DirectoryNode dir52 = dir5.addSubDir("dir52");

        dir51.addFile("file51", 1100);
        dir51.addFile("file52", 100.100);

        DirectoryNode dir53 = dir52.addSubDir("dir53");
        dir53.addFile("file54", 10);

        dir52.addFile("file53", 1000);


        System.out.println(dir1.getDirSize());
        System.out.println(dir2.getDirSize());
        System.out.println(dir5.getDirSize());
        System.out.println(dir51.getDirSize());
        System.out.println(dir52.getDirSize());
        System.out.println(dir53.getDirSize());

        List<DirectoryNode> allDirs = new ArrayList<>();
        collectAllDirectories(dir1, allDirs);
        collectAllDirectories(dir5, allDirs);

        PriorityQueue<DirectoryNode> pq = new PriorityQueue<>(Comparator.comparingDouble(DirectoryNode::getDirSize).reversed());
        pq.addAll(allDirs);

        System.out.println("Top 10 directories by size:");
        for (int i = 0; i < 5 && !pq.isEmpty(); i++) {
            DirectoryNode dir = pq.poll();
            System.out.println(dir.getDirName() + ": " + dir.getDirSize());
        }


    }
    private static void collectAllDirectories(DirectoryNode dir, List<DirectoryNode> allDirs) {
        if (dir == null) return;
        allDirs.add(dir);
        for (DirectoryNode subDir : dir.getSubDirectoriesList()) {
            collectAllDirectories(subDir, allDirs);
        }
    }

}