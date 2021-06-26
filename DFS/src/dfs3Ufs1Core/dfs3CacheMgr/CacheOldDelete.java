package dfs3Ufs1Core.dfs3CacheMgr;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.concurrent.atomic.AtomicInteger;

public class CacheOldDelete {
    public static int oldDelete(final Path destination,
                                final Integer daysToKeep) throws IOException {

        final Instant retentionFilePeriod = ZonedDateTime.now()
                .minusDays(daysToKeep).toInstant();

        final AtomicInteger countDeletedFiles = new AtomicInteger();
        Files.find(destination, 1,
                (path, basicFileAttrs) -> basicFileAttrs.lastModifiedTime()
                        .toInstant().isBefore(retentionFilePeriod))
                .forEach(fileToDelete -> {
                    try {
                        if (!Files.isDirectory(fileToDelete)) {
                            Files.delete(fileToDelete);
                            countDeletedFiles.incrementAndGet();
                        }
                    } catch (IOException e) {
                        throw new UncheckedIOException(e);
                    }
                });

        return countDeletedFiles.get();
    }
}

