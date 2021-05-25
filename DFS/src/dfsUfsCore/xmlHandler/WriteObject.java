package dfsUfsCore.xmlHandler;

import java.util.Base64;

public class WriteObject {
    private int id;
    private String hashedInode;
    private String data;
    private boolean isInode;

    public WriteObject(int id, String hashedInode,byte[] data, boolean isInode){
        this.id = id;
        this.hashedInode = hashedInode;
        // base 64 encoding
        byte[] encoded = Base64.getEncoder().encode(data);
        this.data = new String(encoded);
        this.isInode = isInode;
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
    //method to getIsInode
    public boolean getIsInode(){return isInode;}
    //method to setIsInode
    public void setInode(boolean isInode){this.isInode=isInode;}
}