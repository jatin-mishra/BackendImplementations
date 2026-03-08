package org.example.logfilewatcherwithredis.redis;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.logfilewatcherwithredis.constant.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Component
public class RedisService {

    private static final Logger log = LoggerFactory.getLogger(RedisService.class);
    private final ObjectMapper objectMapper;
    RedisTemplate<String, String> redis;

    public RedisService(RedisTemplate<String, String> redis, ObjectMapper objectMapper){
        this.redis = redis;
        this.objectMapper = objectMapper;
    }

    public void add(String key, String value, long expiry){
        redis.opsForValue().set(key, value, Duration.of(expiry, ChronoUnit.MINUTES));
    }

    public void publish(List<String> message){
        log.info("publishing {}", message);
        try {
            String json = objectMapper.writeValueAsString(message);
            redis.convertAndSend(Channel.logChannel.getChannelName(), json);
            log.info("published");
        } catch (JsonProcessingException e) {
            log.error("failed to serialize message", e);
        }
    }
}
