package org.example.logfile;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

public class Tail {

    public static List<String> tail(Path path, int n) throws IOException {
       List<String> lastNLines = new ArrayList<>();
       try(FileChannel channel = FileChannel.open(path, StandardOpenOption.READ)){
           long position = channel.size() - 1;
           ByteBuffer buffer =  ByteBuffer.allocate(1);

           StringBuilder builder = new StringBuilder();
           while(position >= 0 && lastNLines.size() < n){
               buffer.clear();
               channel.position(position);
               channel.read(buffer);
               buffer.flip();
               char c = (char) buffer.get();
               if(c == '\n'){
                   lastNLines.addFirst(builder.reverse().toString());
                   builder.setLength(0);
               }else{
                   builder.append(c);
               }
               position--;
           }

           if(!builder.isEmpty()){
               lastNLines.add(builder.reverse().toString());
           }
       }
       return lastNLines;
    }

    public static List<String> optimizedTail(Path path, int n) throws IOException {
        List<String> ans = new ArrayList<>();
        try(FileChannel channel = FileChannel.open(path, StandardOpenOption.READ)){

            ByteBuffer buffer = ByteBuffer.allocate(4096);
            long position = channel.size();

            StringBuilder builder = new StringBuilder();
            outer: while(position > 0){
                long chunkToRead = (position < buffer.limit()) ? position : buffer.limit();
                position = position - chunkToRead;

                buffer.clear();
                buffer.limit((int) chunkToRead);
                channel.position(position);
                channel.read(buffer);
                buffer.flip();

                for(int i = (int) chunkToRead - 1; i >= 0; i--){
                    char c = (char) buffer.get(i);
                    if(c == '\n'){
                        ans.addFirst(builder.reverse().toString());
                        builder.setLength(0);
                        if(ans.size() == n) break outer;
                    }else{
                        builder.append(c);
                    }
                }

                if(!builder.isEmpty() && ans.size() < n){
                    ans.addFirst(builder.reverse().toString());
                    builder.setLength(0);
                }
            }
        }
        return ans;
    }

    static void main() throws IOException {
        for(String line : optimizedTail(Path.of("logs.txt"), 4)){
            System.out.println(line);
        }
    }
}
