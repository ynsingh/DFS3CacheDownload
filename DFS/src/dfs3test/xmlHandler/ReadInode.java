package dfs3test.xmlHandler;
import java.util.*;


public class ReadInode {
    private String fileName;
    private long fileSize;
    private String fileURI;
    private String author;
    private int noOfSegments;
    private String timestamp;
    private boolean fbit;
    private boolean isInode;
    private HashMap<String, String> splitParts = new HashMap<>();

    public String getFileName() {
        return fileName;
    }

    public ReadInode setFileName(String fileName) {
        this.fileName = fileName;
        return this;
    }

    public long getFileSize() {
        return fileSize;
    }

    public ReadInode setFileSize(long fileSize) {
        this.fileSize = fileSize;
        return this;
    }

    public String getFileURI() {
        return fileURI;
    }

    public ReadInode setFileURI(String fileURI) {
        this.fileURI = fileURI;
        return this;
    }

    public String getAuthor() {
        return author;
    }

    public ReadInode setAuthor(String author) {
        this.author = author;
        return this;
    }

    public int getNoOfSegments() {
        return noOfSegments;
    }

    public ReadInode setNoOfSegments(int noOfSegments) {
        this.noOfSegments = noOfSegments;
        return this;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public ReadInode setTimestamp(String timestamp) {
        this.timestamp = timestamp;
        return this;
    }

    public Boolean getFbit() {
        return fbit;
    }

    public ReadInode setFbit(Boolean fbit) {
        this.fbit = fbit;
        return this;
    }
    public Boolean getIsInode() { return isInode; }

    public ReadInode setIsInode(Boolean isInode) {
        this.isInode = isInode;
        return this;
    }

    public HashMap<String, String> getSplitParts() {
        return splitParts;
    }

    public ReadInode setSplitParts(String key, String value) {

        this.splitParts.put(key, value);
        return this;
    }
}