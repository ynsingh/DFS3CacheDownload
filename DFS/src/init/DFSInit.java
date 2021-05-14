package init;

import dfs3test.communication.Receiver;
import java.io.*;
import java.security.GeneralSecurityException;


import javax.swing.*;

public class DFSInit {

    public static void main(String[] args) throws IOException, GeneralSecurityException, InterruptedException {

        DFSConfig dfsconfig = DFSConfig.getInstance();
        Thread rx = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Receiver.start();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        rx.start();
        dfsCacheMgr.CacheScheduler.scheduler();
        DFSUI DUI = new DFSUI();
    }
}