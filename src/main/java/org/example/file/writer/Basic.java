package org.example.file.writer;

import java.io.File;
import java.io.IOException;

public class Basic {
    static void main() throws IOException {
        File file = new File("logs.txt");
        System.out.println(file.exists());
        System.out.println(file.getAbsoluteFile().getAbsolutePath());
        System.out.println(file.length());
    }
}
