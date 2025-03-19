package com.javaweb.utils;

import java.io.File;

public class FileUtil {
    public static int getFileSize(String url) {
        File file = new File(url);
        if(file.exists()) {
            long fileSizeInBytes = file.length();
            double fileSizeInKB = (double) fileSizeInBytes / 1024;
            return (int) fileSizeInKB;
        }

        return 0;
    }
}
