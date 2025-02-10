package org.flipkart.coding.fileanddirectories;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
@Data
public class DirectoryNode {
    private String dirName;
    private double dirSize;
    private List<DirectoryNode> subDirectoriesList;
    private List<FileNode> fileList;
    private DirectoryNode parentDir;

    public DirectoryNode (String name) {
        this.dirName = name;
        dirSize = 0;
        subDirectoriesList = new ArrayList<>();
        fileList = new ArrayList<>();
        this.parentDir = null;
    }

    public DirectoryNode(String name, DirectoryNode parent) {
        this.dirName = name;
        dirSize = 0;
        subDirectoriesList = new ArrayList<>();
        fileList = new ArrayList<>();
        this.parentDir = parent;
    }

    public void addFile(String fileName, double fileSize) {
        FileNode file = new FileNode(fileName, fileSize, this);
        this.fileList.add(file);

        DirectoryNode p = this;
        while (p != null) {
            p.dirSize += fileSize;
            p = p.parentDir;
        }
    }

    public void removeFile(String fileName) {
        for(FileNode file : fileList) {
            if(file.getFileName().equals(fileName)) {
                double fileSize = file.getFileSize();

                fileList.remove(file);

                DirectoryNode p = this;
                while (p != null) {
                    p.dirSize -= fileSize;
                    p = p.parentDir;
                }
            }
        }
    }

    public DirectoryNode addSubDir(String dirName) {
        DirectoryNode subDir = new DirectoryNode(dirName, this);
        this.subDirectoriesList.add(subDir);
        return subDir;
    }

    public void removeSubDir(String dirName) {
        for(DirectoryNode dir : subDirectoriesList) {
            if(dir.dirName.equals(dirName)) {
                double dirSize = dir.dirSize;

                subDirectoriesList.remove(dirSize);

                DirectoryNode p = this;
                while (p != null) {
                    p.dirSize -= dirSize;
                    p = p.parentDir;
                }
            }
        }
    }

    public double getDirSize() {
        return this.dirSize;
    }

}