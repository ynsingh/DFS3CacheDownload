package simulateGC.communication;

import dfs3Ufs1Core.dfs3Mgr.DFS3BufferMgr;
import dfs3Ufs1Core.dfs3xmlHandler.XMLReader;

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
    public static void start() throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        ServerSocket serverSocket = null;

        try {
            serverSocket = new ServerSocket(4445);
            System.out.println("Server started");
        } catch (IOException ex) {
            System.out.println("Can't setup server on this port number. ");
        }

        Socket socket = null;
        InputStream in = null;
        OutputStream out = null;
        while(true){
            try {
                socket = serverSocket.accept();
            } catch (IOException ex) {
                System.out.println("Can't accept client connection.");
            }
            if(socket.isConnected()){
                try {
                    in = socket.getInputStream();
                } catch (IOException ex) {
                    System.out.println("Can't get socket input stream.");
                }
                String fileName = getName();
                File file = new File(fileName);
                file.createNewFile();
                try {
                    out = new FileOutputStream(fileName);
                } catch (FileNotFoundException ex) {
                    ex.printStackTrace();
                    System.out.println("File not found.");
                }

                byte[] bytes = new byte[16*1024];
                int count;
                while ((count = in.read(bytes)) > 0) {
                    out.write(bytes, 0, count);
                }
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
