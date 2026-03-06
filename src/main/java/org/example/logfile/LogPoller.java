package org.example.logfile;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LogPoller {
    private final Path path;
    private long offset = 0;

    public LogPoller(String path) throws Exception {
        this.path = Path.of(path);
        this.offset = Files.size(this.path);
    }

    public List<String> readNew() throws IOException {
        ByteBuffer buffer =  ByteBuffer.allocate(4096);
        List<String> lines = new ArrayList<>();
        try(FileChannel channel = FileChannel.open(this.path, StandardOpenOption.READ)){
            buffer.clear();
            channel.position(offset);
            int bytes = channel.read(buffer);
            if(bytes > 0){
                buffer.flip();
                String data = StandardCharsets.UTF_8.decode(buffer).toString();
                lines = Arrays.asList(data.split("\n"));
                offset += bytes;
            }
        }
        return lines;
    }

}
