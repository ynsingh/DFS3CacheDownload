package com.ehelpy.brihaspati4.simulateGC.communication;

import java.io.*;
import java.net.Socket;

public class Sender {
    public static void start(File xmlFile, String IP) throws IOException {
        try (Socket socket = new Socket(IP, 4445)) {
            byte[] bytes = new byte[16 * 1024];
            InputStream in = new FileInputStream(xmlFile);
            OutputStream out = socket.getOutputStream();
            int count;
            while ((count = in.read(bytes)) > 0) {
                out.write(bytes, 0, count);
            }
            out.close();
            in.close();
        }
    }
}
