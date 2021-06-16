package dfsUfsCore.dfs3xmlHandler;

import java.util.Base64;

public class WriteObject {
    private int id;
    private String hashedInode;
    private String data;
    private String publicKeyStr;

    public WriteObject(int id, String hashedInode, byte[] data, String publicKeyStr){
        this.id = id;
        this.hashedInode = hashedInode;
        // base 64 encoding
        byte[] encoded = Base64.getEncoder().encode(data);
        this.data = new String(encoded);
        this.publicKeyStr=publicKeyStr;

    }
    // method to getId
    public int getId() {
        return id;
    }
    // method to put Id
    public void setId(int id) {
        this.id = id;
    }
    // method to get Inode
    public String getInode() {
        return hashedInode;
    }
    // method to put Inode
    public void setInode(String hashedInode) {
        this.hashedInode = hashedInode;
    }
    // method to get Data
    public String getData() {
        return data;
    }
    // method to put Data
    public void setData(String data) {
        this.data = data;
    }
    //method to get public key in string format
    public String getPublicKeyStr() { return publicKeyStr; }
    //method to set public key in string format
    public WriteObject setPublicKeyStr(String publicKeyStr) { this.publicKeyStr = publicKeyStr;
        return this;
    }
}
