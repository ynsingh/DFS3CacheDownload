package com.ehelpy.brihaspati4.dfs3Ufs1Core.dfs3CacheMgr;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import com.ehelpy.brihaspati4.dfs3Ufs1Core.dfs3Mgr.DFS3Config;
import com.ehelpy.brihaspati4.simulateGC.communication.Receiver;
import org.apache.log4j.Logger;

public class CacheSizeDelete {

    static DFS3Config dfs3_ufs1 = DFS3Config.getInstance();
    private static final Logger log = Logger.getLogger(Receiver.class.getName());

    public static int sizedelete(Path dir, long cacheSize) throws IOException, InterruptedException {

        //System.out.println("folder: "+dir);
        //System.out.println("-- before deleting old files--");
        getSortedFilesByDateCreated(dir,  true);

        //System.out.println("-- deleting old files --");
        int count = deleteOldFiles(dir, cacheSize);

        //System.out.println("--Files remaining  after cache  clear operation--");
        getSortedFilesByDateCreated(dir, true);
        return count;
    }


    public static int deleteOldFiles(Path parentFolder, long limit) {
        List<Path> files = getSortedFilesByDateCreated(parentFolder, true);
        long cacheOccupied = dfs3_ufs1.getCacheOccupied();
        int count=0;

        if(cacheOccupied <=limit*0.9){

            log.debug("Sufficient cache available");
            return 0;
        }
        else
        {
            while(dfs3_ufs1.getCacheOccupied() > (limit*0.75))
            {
                //System.out.println("Oldest file is:"+files.get(0));
                try {
                    Files.delete(files.get(0));
                    //System.out.println("Oldest file deleted");
                } catch (IOException e) {
                    e.printStackTrace();
                }
                files.remove(0);
                count++;
                dfs3_ufs1.setCacheOccupied();
            }
        }
        log.debug("Now DFS cache size is: "+ dfs3_ufs1.getCacheOccupied());
        return count;
    }

    public static List<Path> getSortedFilesByDateCreated(Path parentFolder, boolean ascendingOrder) {
        try {
            Comparator<Path> pathComparator = Comparator.comparingLong(p -> getFileModificationEpoch((p).toFile()));
            return Files.list(parentFolder)
                    .filter(Files::isRegularFile)
                    .sorted(ascendingOrder? pathComparator :
                            pathComparator.reversed())
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static long getFileModificationEpoch(File file) {
        try {
            BasicFileAttributes attr = Files.readAttributes(file.toPath(),
                    BasicFileAttributes.class);
            return attr.lastModifiedTime()
                    .toInstant().toEpochMilli();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static LocalDateTime getFileModificationDateTime(File file) {
        try {
            BasicFileAttributes attr = Files.readAttributes(file.toPath(),
                    BasicFileAttributes.class);
            return attr.lastModifiedTime()
                    .toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}