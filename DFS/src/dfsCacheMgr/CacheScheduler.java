package dfsCacheMgr;
import init.DFSConfig;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Timer;
import java.util.TimerTask;
import java.awt.Toolkit;

/**
 * Schedule a task that executes once every second.
 */

public class CacheScheduler {
    Toolkit toolkit;
    Timer timer;

    public CacheScheduler() {
        toolkit = Toolkit.getDefaultToolkit();
        timer = new Timer();
        timer.schedule(new RemindTask(),
                0,        //initial delay
                1000*60*60*24);  //subsequent rate - 24 hours
    }

    static class RemindTask extends TimerTask {

        public void run() {
            Path cachePath= Paths.get(DFSConfig.dfsCache);
            try {
                //int i = CacheOldDelete.oldDelete(cachePath,15);
                //System.out.println(i+" Files older than 15 days deleted from Cache");
                int j = CacheSizeDelete.sizedelete(cachePath, DFSConfig.getLocalCacheSize());
                System.out.println(j+" Older files exceeding cache size deleted from Cache");
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }

        }
    }

    public static void scheduler() {
        //System.out.println("About to schedule task.");
        new CacheScheduler();
        System.out.println("Task scheduled.");
    }
}