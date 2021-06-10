package simulateGC.init;

import dfsUfsCore.dfsMgr.DFS3Config;
import simulateGC.communication.Receiver;

import java.io.*;
import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

/**
 * This class is the entry point for DFS-UFS when running is stand alone mode.
 *<p>1. It creates a singleton of DFS3Config responsible for initiating and maintaining the state of DFS</p>
 *<p>2. It starts a thread for receiver which constantly listens for any incoming messages/segments from other nodes</p>
 *<p>3. It initiates a thread to monitor and manage local cache created for DFS</p>
 *<p>4. Lastly, displays the GUI for the user for further user actions.</p>
 */
public class DFSInit {
   /**
     * Main method to enter the stand alone DFS code.
     * @param args - not used
     * @throws IOException in case of IO exception
     * @throws GeneralSecurityException in case of general security exception
     * @throws InterruptedException interrupt exception
     */
    public static void main(String[] args) throws IOException, GeneralSecurityException, InterruptedException {

        //Create singleton object of DFS3Config.
        DFS3Config dfs = DFS3Config.getInstance();
        Thread rx;
        rx = new Thread(() -> {
            try {
                Receiver.start();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            } catch (InvalidKeySpecException e) {
                e.printStackTrace();
            }
        });
        //Start receiving server
        rx.start();
        //Initialize thread to monitor and manage local cache for DFS
        dfsUfsCore.dfsCacheMgr.CacheScheduler.scheduler();
        DFSUI DUI = new DFSUI();
    }
}