package org.example.logfilewatcherwithredis.reader;

import org.example.logfilewatcherwithredis.dto.ReadRecord;
import org.example.logfilewatcherwithredis.config.LogConfig;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;

@Component
public class LogReader {
    private final LogConfig logConfig;

    public LogReader(LogConfig config){
        this.logConfig = config;
    }

    public FileChannel openChannel() throws IOException {
        return FileChannel.open(Path.of(this.logConfig.path()), StandardOpenOption.READ);
    }

    public ReadRecord readFromLastN(FileChannel channel, long offset) throws IOException {
        int n = this.logConfig.lines();
        n++;
        if(channel == null){
            throw new RuntimeException("channel can't be null");
        }
        ByteBuffer buffer = ByteBuffer.allocate(this.logConfig.bufferSize());
        StringBuilder builder = new StringBuilder();
        long size = channel.size();
        long position = size;

        Deque<String> ans = new LinkedList<>();
        outer: while(position > offset && ans.size() < n){
            long bytesToRead = (position - offset < buffer.limit()) ? position - offset : buffer.limit();
            buffer.clear();
            buffer.limit((int) bytesToRead);
            channel.position(position - bytesToRead);
            channel.read(buffer);
            buffer.flip();

            for(int i = (int)bytesToRead - 1; i >= 0; i--){
                char c = (char) buffer.get(i);
                if(c == '\n'){
                    ans.addFirst(builder.reverse().toString());
                    builder.setLength(0);
                    if(ans.size() == n) break outer;
                }else{
                    builder.append(c);
                }
            }
            position -= bytesToRead;
        }
        if(!builder.isEmpty()){
            ans.addFirst(builder.reverse().toString());
            builder.setLength(0);
        }
        if(!ans.isEmpty()) {
            String s = ans.removeLast();
            size -= s.length();
        }
        return new ReadRecord(new ArrayList<>(ans), size);
    }
}
