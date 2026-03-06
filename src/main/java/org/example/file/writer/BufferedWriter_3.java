package org.example.file.writer;

import java.io.BufferedWriter;
import java.io.FileWriter;

public class BufferedWriter_3 {
    static void main() {
        try(BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter("logs.txt", true))){
            System.out.println("this is started");
            bufferedWriter.newLine();
            bufferedWriter.write("from buffered write before sleep");
            bufferedWriter.newLine();
            System.out.println("waiting...");
            Thread.sleep(10000);
            bufferedWriter.write("from buffer write after");
            System.out.println("waiting...");
            Thread.sleep(10000);
            System.out.println("finished!");
        }catch (Exception exception){

        }
    }
}
