package org.example.udp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.nio.charset.StandardCharsets;

import static java.lang.IO.println;

public class UDPServer {
    private static final int PORT = 9876;
    private static final int BUFFER_SIZE = 1024;

    public static void main(String[] args){
        println("UDP Server is starting on port " + PORT);
        try(DatagramSocket socket = new DatagramSocket(PORT)) {

            byte[] buffer = new byte[BUFFER_SIZE];

            while(true){

                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);

                String message = new String(
                        packet.getData(),
                        packet.getOffset(),
                        packet.getLength(),
                        StandardCharsets.UTF_8
                );

                println("received message from " + packet.getAddress() + ":" + packet.getPort() + " - " + message);

                byte[] response = ("Echo: " + message).getBytes(StandardCharsets.UTF_8);
                DatagramPacket responsePacket = new DatagramPacket(
                        response,
                        response.length,
                        packet.getAddress(),
                        packet.getPort()
                );
                socket.send(responsePacket);
            }
        } catch (IOException exception){
            throw new RuntimeException("Error in UDP Server", exception);
        }
    }
}

// nc -u localhost 9876
