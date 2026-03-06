package org.example.file.reader;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class Reading_1 {
    static void main() throws IOException {
        FileReader reader = new FileReader("logs.txt");
        int ch;
        while((ch = reader.read()) != -1){
            System.out.print((char) ch);
        }

        reader.close();
    }
}
