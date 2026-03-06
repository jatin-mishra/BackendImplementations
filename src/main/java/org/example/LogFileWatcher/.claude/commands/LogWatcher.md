
You are senior software engineer expert at spring boot and java 21.
You will be building a spring boot web application here in current directory.

# Requirements:
Monitor a log file for new lines (append-only)
Serve a web page at http://localhost/log showing the last n lines on load
Stream new lines to all connected clients in real-time, no page refresh
File can be several GB — you cannot read it from the beginning each poll

You task is to build log watcher system.
tools to use:
spring web for SSE
thymeleaf for ui part.
for reading files do two implementation:


# Solution
- make these things configurable
    - log file path,
    - number of logs to show
    - thymeleaf template html path



FileLogReader
- tailLines
- bufferSize
+ readLastN(channel)
+ readNewLogs(channel, offset) -> maintain


SSEHandler
- []Emitters
+ add(emitter)
+ remove(emitter)
+ broadcast([]lines)

Watcher:
- logFilePath
- fileOffset
- FileChannel
- FileLogReader

- SSEHandler

- WatchService
- VirtualThreadExecutor

+ Watcher(reader, handler, executor)
    - schedule virtual thread (with while loop inside until interrupted)
        - watcher function
            - poll events
                - if overflow then broadcast new (fileOffset)
                - else if entry create || channel == null -> reopen file channel and broadcast new (fileOffset)
                - else if entry modify -> broadcast new (fileOffset)


Controller:
/log
- creates channel
- read last N and return html (which script connects to another sse endpoint)
/log/sse
- registers emitter


Templates (Thymeleaf):
- header that says -> Logs
- body divs with logs
- on connect -> show connection established
- on message -> maintains limit, on new elements provided, maintains size = n, adds new, if size breaches then removes older divs.
