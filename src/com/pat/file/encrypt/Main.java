package com.pat.file.encrypt;

import java.io.File;

public class Main {

    public static void main(String[] args) throws Exception {
        String password = "test";
        File file = new File("C:/Users/Patrick/Desktop/test.txt");
        Data data = new Data(file, "password");
        data.encrypt();
    }
}


