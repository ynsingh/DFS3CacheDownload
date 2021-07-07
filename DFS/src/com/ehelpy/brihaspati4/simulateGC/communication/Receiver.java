package com.ehelpy.brihaspati4.simulateGC.communication;

import com.ehelpy.brihaspati4.dfs3Ufs1Core.dfs3Mgr.DFS3BufferMgr;
import com.ehelpy.brihaspati4.dfs3Ufs1Core.dfs3xmlHandler.XMLReader;
import org.apache.log4j.Logger;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.UUID;

public class Receiver {
    public static DFS3BufferMgr inputbuffer = DFS3BufferMgr.getInstance();
    private static final Logger log = Logger.getLogger(Receiver.class.getName());

    public static void start() throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        ServerSocket serverSocket = null;

        try {
            serverSocket = new ServerSocket(4445);
            log.debug("Server started");
        } catch (IOException ex) {
            log.debug("Can't setup server on this port number. ");
        }

        Socket socket = null;
        InputStream in = null;
        OutputStream out = null;
        while(true){
            try {
                assert serverSocket != null;
                socket = serverSocket.accept();
            } catch (IOException ex) {
                log.debug("Can't accept client connection.");
            }
            assert socket != null;
            if(socket.isConnected()){
                try {
                    in = socket.getInputStream();
                } catch (IOException ex) {
                    log.debug("Can't get socket input stream.");
                }
                String fileName = getName();
                File file = new File(fileName);
                file.createNewFile();
                try {
                    out = new FileOutputStream(fileName);
                } catch (FileNotFoundException ex) {
                    ex.printStackTrace();
                    log.debug("File not found.");
                }

                byte[] bytes = new byte[16*1024];
                int count;
                while (true) {
                    assert in != null;
                    if (!((count = in.read(bytes)) > 0)) break;
                    assert out != null;
                    out.write(bytes, 0, count);
                }
                assert out != null;
                out.close();
                in.close();
                inputbuffer.addToInputBuffer(new File(fileName));
                XMLReader.reader(inputbuffer.fetchFromInputBuffer());
            }
            else
                break;
        }
        //socket.close();
        //serverSocket.close();
    }
    public static String getName(){
        Path fileName = Paths.get(UUID.randomUUID() + ".xml");
        return String.valueOf(fileName);
    }
}
