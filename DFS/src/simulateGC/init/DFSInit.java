package simulateGC.init;

import dfs3Ufs1Core.dfs3Mgr.DFS3Config;
import simulateGC.communication.Receiver;

import java.io.*;
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
    static DFS3Config dfs3_ufs1 = DFS3Config.getInstance();
   /**
     * Main method to enter the stand alone DFS code.
     * @param args - not used
     */
    public static void main(String[] args)  {

        //Create singleton object of DFS3Config.
        Thread rx;
        rx = new Thread(DFSInit::run);
        //Start receiving server
        rx.start();
        //Initialize thread to monitor and manage local cache for DFS
        dfs3Ufs1Core.dfs3CacheMgr.CacheScheduler.scheduler();
        DFSUI DUI = new DFSUI();
    }

    private static void run() {
        try {
            Receiver.start();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        }
    }
}