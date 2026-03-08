package org.example.socketservers.ws;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class SimpleServer_1 {

    private static final String MAGIC = "258EAFA5-E914-47DA-95CA-C5AB0DC85B11";

    public static void main(String[] args) throws Exception {
        ServerSocket server = new ServerSocket(8080);
        System.out.println("Websocket server listening to 8080");

        while(true){
            Socket socket = server.accept(); // gets file descriptor here
            new Thread(() -> handleClient(socket)).start();
        }
    }

    private static void handleClient(Socket socket) {
        try {
            // as web socket uses both
            InputStream in = socket.getInputStream();
            OutputStream out = socket.getOutputStream();

            BufferedReader reader = new BufferedReader(new InputStreamReader(in));

            String line;
            String wsKey = null;

            // traversed all first bytes until frames start
            while (!(line = reader.readLine()).isEmpty()) {
                if (line.startsWith("Sec-WebSocket-Key")) {
                    wsKey = line.split(": ")[1];
                }
            }

            // think what if there is no websocket key
            String acceptKey = generateKey(wsKey);

            String response =
                    "HTTP/1.1 101 Switching Protocols\r\n" +
                            "Upgrade: websocket\r\n" +
                            "Connection: Upgrade\r\n" +
                            "Sec-WebSocket-Accept: " + acceptKey + "\r\n\r\n";

            out.write(response.getBytes(StandardCharsets.UTF_8));
            out.flush();
            readFrames(in, out);
        }catch (IOException | NoSuchAlgorithmException exception){
            exception.printStackTrace();
        }
    }

    private static String generateKey(String clientId) throws NoSuchAlgorithmException {
        String combined = clientId + MAGIC;
        MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
        byte[] hash = sha1.digest(combined.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(hash);
    }

    private static void readFrames(InputStream in, OutputStream out) throws IOException {
        // this is where all message interaction is happening
        // read frames and build final input and then respond.
        while (true) {
            int b1 = in.read();
            int b2 = in.read();

            boolean fin = (b1 & 0x80) != 0;
            int opcode = b1 & 0x0F;

            boolean masked = (b2 & 0x80) != 0;
            int payloadLen = b2 & 0x7F;

            byte[] mask = new byte[4];
            in.read(mask);

            byte[] payload = new byte[payloadLen];
            in.read(payload);

            for (int i = 0; i < payloadLen; i++) {
                payload[i] ^= mask[i % 4];
            }

            if (opcode == 0x1) {
                String message = new String(payload, StandardCharsets.UTF_8);
                System.out.println("Received: " + message);
                sendText(out, "Echo: " + message);
            }
        }
    }

    private static void sendText(OutputStream out, String message) throws IOException {
        byte[] payload = message.getBytes(StandardCharsets.UTF_8);
        out.write(0x81);
        out.write(payload.length);
        out.write(payload);
        out.flush();
    }
}
