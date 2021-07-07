package com.ehelpy.brihaspati4.simulateGC.indexing;

import com.ehelpy.brihaspati4.dfs3Ufs1Core.dfs3Util.TLVParser;
import com.ehelpy.brihaspati4.dfs3Ufs1Core.dfs3Mgr.DFS3Config;
import com.ehelpy.brihaspati4.simulateGC.encrypt.GenerateKeys;
import com.ehelpy.brihaspati4.simulateGC.encrypt.Hash;

import java.io.File;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;

import static com.ehelpy.brihaspati4.dfs3Ufs1Core.dfs3Util.file.*;

/**
 * Class responsible for storing the uploaded data.
 * This class stores the segment of uploaded file
 * into the local disk through database package
 * <p><b>Functions:</b> At the cloud end of DFS</p>
 * <b>Note:</b> Change this file to change functionality
 * related to the storing function
 * @author <a href="https://t.me/sidharthiitk">Sidharth Patra</a>
 * @since   15th Feb 2020
 */
public class Store {
    static DFS3Config dfs3_ufs1 = DFS3Config.getInstance();
    /**
     * Starts the store process
     * <p> This method executes all the functions related to storing
     * of a segment after verifying signed hash within the segment
     * This method runs at the root node</p>
     * @param dataInbound segment data including signed hash
     * @param hashedInode hashed segment inode (DFS://emailID//segmentpath)
     * used as the file name for storing into the local disk
     * @param publicKey public key of sender extracted from xml file.
     * @throws NoSuchAlgorithmException used in hashing if no algorithm found as specified
     * @throws IOException for input output exception
     * @throws SignatureException exception related to Signed Hash
     * @throws InvalidKeyException Key provided for hashing is not valid
     * @throws InvalidKeySpecException KeySpec provided is not valid
     */
    public static void start(byte[] dataInbound, String hashedInode, PublicKey publicKey)
            throws IOException, NoSuchAlgorithmException, SignatureException,
            InvalidKeyException, InvalidKeySpecException {
        //TODO - receive the inode and hash from Xml handler with request to store
        //retrieve the signed hash and encrypted File
        String writepath = dfs3_ufs1.getDfsSrvr()+hashedInode;
        byte[] signedHash = TLVParser.startParsing(dataInbound, 1);
        //retrieve the encrypted file by removing the length of signed hash + tag + length
        byte[] encFile = deconcat(dataInbound,signedHash.length+8);
        //compute the hash
        String hashComputed = Hash.hashgenerator(encFile);
        // verify the hash against signed hash
        boolean match = GenerateKeys.verifyHashK(hashComputed.getBytes(),signedHash,publicKey);
        // when the hash matches write the segment data with segment inode as filename
        if (match) {
            writeData(encFile, writepath);//
            index1(hashedInode,"root_index.csv");
            // initialise replication
            //initReplicate(encFile,hashedInode);
            File segment = new File(writepath);
            dfs3_ufs1.setLocalOccupied();
            dfs3_ufs1.setLocalBalance();
        } else
          System.out.println("hash mismatch cant store file");
    }//end of start

}
