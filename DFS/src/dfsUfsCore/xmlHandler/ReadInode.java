package dfsUfsCore.xmlHandler;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.security.spec.X509EncodedKeySpec;
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
    private final HashMap<String, String> splitParts = new HashMap<>();

    public PublicKey getPubKey() {
        return pubKey;
    }

    public ReadInode setPubKey(String publicKey) throws NoSuchAlgorithmException, InvalidKeySpecException {
        Base64.Decoder decoder = Base64.getDecoder();
        byte[] byteKey = decoder.decode(publicKey.getBytes());
        X509EncodedKeySpec spec = new X509EncodedKeySpec(byteKey);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        this.pubKey=kf.generatePublic(spec);
        return this;
    }

    private PublicKey pubKey;

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