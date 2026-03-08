package org.example.logfilewatcherwithredis.sse;


import jakarta.annotation.PreDestroy;
import org.example.logfilewatcherwithredis.dto.ReadRecord;
import org.example.logfilewatcherwithredis.reader.LogReader;
import org.example.logfilewatcherwithredis.redis.RedisService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Component
public class Poller implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(Poller.class);
    private final ScheduledExecutorService executorService;
    private final LogReader reader;
    private final FileChannel channel;
    private final RedisService publisher;
    private long offset;

    public Poller(ScheduledExecutorService executorService, LogReader reader, RedisService publisher) throws IOException {
        this.executorService = executorService;
        this.offset = 0;
        this.reader = reader;
        this.channel = reader.openChannel();
        this.publisher = publisher;
        executorService.scheduleWithFixedDelay(this, 500, 2000, TimeUnit.MILLISECONDS);
    }

    @Override
    public void run() {
        try {
            log.info("started reading...");
            ReadRecord readRecord = reader.readFromLastN(channel, 0);
            log.info("read: {}", readRecord.lines());
            if (readRecord.lines() != null && !readRecord.lines().isEmpty()) {
                this.offset = readRecord.offset();
                publisher.publish(readRecord.lines());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @PreDestroy
    public void close() throws IOException {
        if(executorService == null || executorService.isTerminated()) return;
        if(!executorService.isShutdown()) this.executorService.shutdown();
        try{
            if(!executorService.awaitTermination(10, TimeUnit.SECONDS)){
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            log.info(e.getMessage());
        }
    }
}
