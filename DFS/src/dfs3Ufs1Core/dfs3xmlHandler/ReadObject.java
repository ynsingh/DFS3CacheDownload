package dfs3Ufs1Core.dfs3xmlHandler;

import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class ReadObject {
    private int id;
    private String hashedInode;
    private String data;
    private PublicKey pubKey;
    // method to getId
    public int getId() {return id;}
    // method to put id
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
        // byte[] decoded = Base64.getDecoder().decode(data.getBytes());
        this.data = data;}
    public PublicKey getPubKey() {
        return pubKey;
    }

    public void setPubKey(String publicKey) throws NoSuchAlgorithmException, InvalidKeySpecException {
        Base64.Decoder decoder = Base64.getDecoder();
        byte[] byteKey = decoder.decode(publicKey.getBytes());
        X509EncodedKeySpec spec = new X509EncodedKeySpec(byteKey);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        this.pubKey=kf.generatePublic(spec);
    }
}

