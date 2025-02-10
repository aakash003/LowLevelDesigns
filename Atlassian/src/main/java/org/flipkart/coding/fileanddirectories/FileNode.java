package org.flipkart.coding.fileanddirectories;

public class FileNode {
    private String fileName;
    private double fileSize;
    private DirectoryNode parentDir;

    public FileNode(String name, double size, DirectoryNode parentDir) {
        this.fileName = name;
        this.fileSize = size;
        this.parentDir = parentDir;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public double getFileSize() {
        return fileSize;
    }

    public void setFileSize(double fileSize) {
        this.fileSize = fileSize;
    }

    public DirectoryNode getParentDir() {
        return parentDir;
    }

    public void setParentDir(DirectoryNode parentDir) {
        this.parentDir = parentDir;
    }
}
