package org.example.logfilewatcher;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class LogFileWatcherApplication {

    public static void main(String[] args) {
        SpringApplication.run(LogFileWatcherApplication.class, args);
    }

}
