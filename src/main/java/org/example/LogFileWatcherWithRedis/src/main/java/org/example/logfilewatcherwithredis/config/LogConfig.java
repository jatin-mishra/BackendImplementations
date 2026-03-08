package org.example.logfilewatcherwithredis.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "log.file")
public record LogConfig(
        String path,
        int lines,
        int bufferSize
) {
}
