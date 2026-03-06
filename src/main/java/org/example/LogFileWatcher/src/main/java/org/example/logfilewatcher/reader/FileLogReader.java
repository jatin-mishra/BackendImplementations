package org.example.logfilewatcher.reader;

import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
public class FileLogReader {

    private static final int BUFFER_SIZE = 8 * 1024; // 8KB for backward scan

    public record ReadResult(List<String> lines, long newOffset) {}

    /**
     * Reads the last n lines by scanning backwards through the file.
     * Efficient for large files — only the tail is actually read forward.
     */
    public List<String> readLastN(FileChannel channel, int n) throws IOException {
        long size = channel.size();
        if (size == 0 || n <= 0) return List.of();

        long pos = size;
        int linesFound = 0;
        long lineStart = 0;
        ByteBuffer buf = ByteBuffer.allocateDirect(BUFFER_SIZE);

        outer:
        while (pos > 0) {
            long from = Math.max(0, pos - BUFFER_SIZE);
            int len = (int) (pos - from);
            buf.clear();
            buf.limit(len);
            channel.read(buf, from);
            buf.flip();

            for (int i = len - 1; i >= 0; i--) {
                if (buf.get(i) == '\n') {
                    linesFound++;
                    if (linesFound > n) {
                        lineStart = from + i + 1;
                        break outer;
                    }
                }
            }
            pos = from;
        }

        long readSize = size - lineStart;
        ByteBuffer readBuf = ByteBuffer.allocate((int) readSize);
        channel.read(readBuf, lineStart);
        readBuf.flip();

        String content = StandardCharsets.UTF_8.decode(readBuf).toString();
        List<String> lines = new ArrayList<>(Arrays.asList(content.split("\n", -1)));

        // drop trailing empty element when file ends with newline
        if (!lines.isEmpty() && lines.getLast().isEmpty()) {
            lines.removeLast();
        }
        return lines.size() > n ? lines.subList(lines.size() - n, lines.size()) : lines;
    }

    /**
     * Reads all complete lines written after {@code offset}.
     * Only returns lines terminated by '\n' — a partial final line is left
     * for the next poll so the client never sees a half-written entry.
     *
     * @return ReadResult with the new lines and the updated byte offset
     */
    public ReadResult readNewLogs(FileChannel channel, long offset) throws IOException {
        long size = channel.size();
        if (size <= offset) return new ReadResult(List.of(), offset);

        long remaining = size - offset;
        // cap single read at 10 MB; next poll will catch the rest
        ByteBuffer buf = ByteBuffer.allocate((int) Math.min(remaining, 10 * 1024 * 1024L));
        int read = channel.read(buf, offset);
        if (read <= 0) return new ReadResult(List.of(), offset);
        buf.flip();

        byte[] bytes = new byte[buf.remaining()];
        buf.get(bytes);

        // Find the last complete line (byte-level, safe for UTF-8)
        int lastNL = -1;
        for (int i = bytes.length - 1; i >= 0; i--) {
            if (bytes[i] == '\n') {
                lastNL = i;
                break;
            }
        }
        if (lastNL < 0) return new ReadResult(List.of(), offset); // no complete line yet

        long newOffset = offset + lastNL + 1;
        String content = new String(bytes, 0, lastNL, StandardCharsets.UTF_8);

        List<String> lines = Arrays.stream(content.split("\n", -1))
                .filter(l -> !l.isEmpty())
                .toList();

        return new ReadResult(lines, newOffset);
    }
}
