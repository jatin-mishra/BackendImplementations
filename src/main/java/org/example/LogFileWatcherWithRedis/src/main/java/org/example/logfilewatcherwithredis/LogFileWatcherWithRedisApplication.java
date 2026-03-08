package org.example.logfilewatcherwithredis;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class LogFileWatcherWithRedisApplication {

    public static void main(String[] args) {
        SpringApplication.run(LogFileWatcherWithRedisApplication.class, args);
    }
}
