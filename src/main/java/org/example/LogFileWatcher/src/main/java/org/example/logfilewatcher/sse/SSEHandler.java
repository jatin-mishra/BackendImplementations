package org.example.logfilewatcher.sse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
public class SSEHandler {

    private static final Logger log = LoggerFactory.getLogger(SSEHandler.class);

    private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();

    public void add(SseEmitter emitter) {
        emitters.add(emitter);
        log.debug("Emitter added — active: {}", emitters.size());
    }

    public void remove(SseEmitter emitter) {
        emitters.remove(emitter);
        log.debug("Emitter removed — active: {}", emitters.size());
    }

    public void broadcast(List<String> lines) {
        if (lines.isEmpty() || emitters.isEmpty()) return;

        String data = String.join("\n", lines);
        List<SseEmitter> dead = new java.util.ArrayList<>();

        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event().data(data));
            } catch (Exception e) {
                log.debug("Removing dead emitter: {}", e.getMessage());
                dead.add(emitter);
            }
        }
        emitters.removeAll(dead);
    }
}
