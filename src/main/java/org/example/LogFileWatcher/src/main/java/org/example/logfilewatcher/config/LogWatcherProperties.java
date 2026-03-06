package org.example.logfilewatcher.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "logwatcher")
public record LogWatcherProperties(
        @DefaultValue("/tmp/app.log") String filePath,
        @DefaultValue("100") int tailLines,
        @DefaultValue("logs") String template
) {}
