package init;

import dfs3test.communication.Receiver;
import java.io.*;
import java.security.GeneralSecurityException;


import javax.swing.*;

public class DFSInit {

    public static void main(String[] args) throws IOException, GeneralSecurityException, InterruptedException {

        JFrame frame;
        frame = new JFrame();
        JOptionPane.showMessageDialog(frame,"Welcome to B4 DFS!","Brihaspati-4 DFS/UFS",JOptionPane.PLAIN_MESSAGE);
        //System.out.println("Welcome to B4 DFS!");
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
        String[] duoptions = { "DFS", "UFS"};
        String options = (String) JOptionPane.showInputDialog(null, "Please choose DFS or UFS:", "DFS/UFS  Option",
                JOptionPane.PLAIN_MESSAGE, null, duoptions, duoptions[0]);
        switch (options) {
            case "DFS":
                DFSUI DUI = new DFSUI();
                break;

            case "UFS":
                UFSUI UUI = new UFSUI();
                break;
        }

    }

}