package org.example.logfilewatcher.controller;

import org.example.logfilewatcher.config.LogWatcherProperties;
import org.example.logfilewatcher.reader.FileLogReader;
import org.example.logfilewatcher.sse.SSEHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;

@Controller
public class LogController {

    private static final Logger log = LoggerFactory.getLogger(LogController.class);

    private final LogWatcherProperties props;
    private final FileLogReader reader;
    private final SSEHandler sseHandler;

    public LogController(LogWatcherProperties props, FileLogReader reader, SSEHandler sseHandler) {
        this.props = props;
        this.reader = reader;
        this.sseHandler = sseHandler;
    }

    @GetMapping("/log")
    public String log(Model model) {
        List<String> lines = List.of();
        Path filePath = Path.of(props.filePath());

        if (Files.exists(filePath)) {
            try (FileChannel channel = FileChannel.open(filePath, StandardOpenOption.READ)) {
                lines = reader.readLastN(channel, props.tailLines());
            } catch (IOException e) {
                log.error("Failed to read log file", e);
            }
        }

        model.addAttribute("lines", lines);
        model.addAttribute("maxLines", props.tailLines());
        return props.template();
    }

    @GetMapping("/log/sse")
    @ResponseBody
    public SseEmitter sse() {
        SseEmitter emitter = new SseEmitter(0L); // no timeout
        sseHandler.add(emitter);
        emitter.onCompletion(() -> sseHandler.remove(emitter));
        emitter.onTimeout(() -> sseHandler.remove(emitter));
        emitter.onError(e -> sseHandler.remove(emitter));
        return emitter;
    }
}
