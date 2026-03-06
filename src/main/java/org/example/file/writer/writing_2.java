package org.example.file.writer;

import java.io.FileWriter;
import java.io.IOException;

public class writing_2 {
    static void main(String[] args) throws IOException, InterruptedException {
        FileWriter bufferedWriter = new FileWriter("logs.txt", true);
        System.out.println("this is started");
        bufferedWriter.write("\nfrom file write before sleep");
        System.out.println("waiting...");
        bufferedWriter.flush();
        Thread.sleep(10000);
        bufferedWriter.write("\nfrom file write after");
        System.out.println("waiting...");
        Thread.sleep(10000);
        System.out.println("finished!");
        bufferedWriter.close();
    }
}
