package org.example.logfilewatcher.watcher;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.example.logfilewatcher.config.LogWatcherProperties;
import org.example.logfilewatcher.reader.FileLogReader;
import org.example.logfilewatcher.sse.SSEHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import static java.nio.file.StandardWatchEventKinds.*;

@Component
public class Watcher {

    private static final Logger log = LoggerFactory.getLogger(Watcher.class);

    private final Path logFilePath;
    private volatile long fileOffset;
    private volatile FileChannel fileChannel;

    private final FileLogReader reader;
    private final SSEHandler handler;
    private final ExecutorService executor;

    private WatchService watchService;
    private Thread watchThread;

    public Watcher(FileLogReader reader, SSEHandler handler, ExecutorService executor,
                   LogWatcherProperties props) {
        this.reader = reader;
        this.handler = handler;
        this.executor = executor;
        this.logFilePath = Path.of(props.filePath()).toAbsolutePath();
    }

    @PostConstruct
    public void start() throws IOException {
        watchService = FileSystems.getDefault().newWatchService();
        Path dir = logFilePath.getParent();
        dir.register(watchService, ENTRY_CREATE, ENTRY_MODIFY, OVERFLOW);

        // Open channel if file already exists and park offset at end
        // (we don't replay existing content — the /log endpoint handles that)
        if (Files.exists(logFilePath)) {
            openChannel();
            fileOffset = fileChannel.size();
        }

        watchThread = Thread.ofVirtual().name("log-watcher").start(this::watchLoop);
        log.info("Watching: {}", logFilePath);
    }

    @PreDestroy
    public void stop() {
        if (watchThread != null) watchThread.interrupt();
        closeChannel();
        try {
            if (watchService != null) watchService.close();
        } catch (IOException e) {
            log.warn("Error closing WatchService", e);
        }
        executor.shutdownNow();
    }

    // ── watch loop ──────────────────────────────────────────────────────────

    private void watchLoop() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                WatchKey key = watchService.poll(1, TimeUnit.SECONDS);
                if (key == null) continue;

                for (WatchEvent<?> event : key.pollEvents()) {
                    handleEvent(event);
                }
                key.reset();

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (IOException e) {
                log.error("Watch loop error", e);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void handleEvent(WatchEvent<?> event) throws IOException {
        WatchEvent.Kind<?> kind = event.kind();

        if (kind == OVERFLOW) {
            // Events were lost; just broadcast whatever is new from current offset
            broadcastNew();
            return;
        }

        Path changed = ((WatchEvent<Path>) event).context();
        if (!changed.getFileName().equals(logFilePath.getFileName())) return;

        if (kind == ENTRY_CREATE || fileChannel == null) {
            reopenChannel();
        }
        broadcastNew();
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private void broadcastNew() throws IOException {
        if (fileChannel == null) return;
        FileLogReader.ReadResult result = reader.readNewLogs(fileChannel, fileOffset);
        fileOffset = result.newOffset();
        if (!result.lines().isEmpty()) {
            handler.broadcast(result.lines());
        }
    }

    private void openChannel() throws IOException {
        fileChannel = FileChannel.open(logFilePath, StandardOpenOption.READ);
    }

    private void reopenChannel() throws IOException {
        closeChannel();
        openChannel();
        fileOffset = 0;
        log.info("Reopened channel for {}", logFilePath.getFileName());
    }

    private void closeChannel() {
        if (fileChannel != null && fileChannel.isOpen()) {
            try {
                fileChannel.close();
            } catch (IOException e) {
                log.warn("Error closing FileChannel", e);
            }
        }
        fileChannel = null;
    }
}
