package org.example.logfilewatcherwithredis.sse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.HashSet;
import java.util.Set;

@Component
public class SSERegistry {
    private static final Logger log = LoggerFactory.getLogger(SSERegistry.class);
    private final List<SseEmitter> emitters;

    public SSERegistry(){
        this.emitters = new ArrayList<>();
    }

    public void add(SseEmitter emitter){
        this.emitters.add(emitter);
    }

    public void remove(SseEmitter emitter){
        this.emitters.remove(emitter);
    }

    public void removeAll(){
        this.emitters.clear();
    }

    public void onMessage(String message){
        log.info("received message: {}, emitters: {}", message, this.emitters.size());
        Set<SseEmitter> dead = new HashSet<>();
        for(SseEmitter emitter : emitters){
            try {
                emitter.send(message);
            }catch (IOException e){
                log.info(e.getMessage());
                dead.add(emitter);
            }
        }
        this.emitters.removeAll(dead);
    }
}
