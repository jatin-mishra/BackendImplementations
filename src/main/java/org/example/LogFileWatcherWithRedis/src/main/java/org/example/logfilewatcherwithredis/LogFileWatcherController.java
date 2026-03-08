package org.example.logfilewatcherwithredis;

import org.example.logfilewatcherwithredis.dto.ReadRecord;
import org.example.logfilewatcherwithredis.reader.LogReader;
import org.example.logfilewatcherwithredis.sse.SSERegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.nio.channels.FileChannel;

@Controller
public class LogFileWatcherController {
    private static final Logger log = LoggerFactory.getLogger(LogFileWatcherController.class);

    private final LogReader logReader;
    private final SSERegistry sseRegistry;

    @Autowired
    public LogFileWatcherController(LogReader logReader, SSERegistry sseRegistry) {
        this.logReader = logReader;
        this.sseRegistry = sseRegistry;
    }

    @GetMapping("/log")
    public String logPage(Model model) throws IOException {
        log.info("received request");
        try (FileChannel channel = logReader.openChannel()) {
            ReadRecord record = logReader.readFromLastN(channel, 0);
            log.info("received {}", record.lines());
            model.addAttribute("lines", record.lines());
        }
        log.info("returning template");
        return "log";
    }

    @GetMapping("/log/stream")
    @ResponseBody
    public SseEmitter stream() {
        log.info("connecting on server...");
        SseEmitter emitter = new SseEmitter(0L);
        sseRegistry.add(emitter);
        emitter.onCompletion(() -> sseRegistry.remove(emitter));
        emitter.onTimeout(() -> sseRegistry.remove(emitter));
        emitter.onError(e -> sseRegistry.remove(emitter));
        log.info("connection established");
        return emitter;
    }
}
