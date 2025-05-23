package com.javaweb.utils;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileUtil {
    public static int getFileSize(String url) {
        File file = new File(url);
        return getFileSizeInBytes(file);
    }

    public static int getFileSize(File file) {
        if (file.exists()) {
            long fileSizeInBytes = file.length();
            double fileSizeInKB = (double) fileSizeInBytes / 1024;
            return (int) fileSizeInKB;
        }
        return 0;
    }

    public static int getFileSizeInBytes(File file) {
        if (file.exists()) {
            long fileSizeInBytes = file.length();
            return (int) fileSizeInBytes;
        }
        return 0;
    }

    public static String getRelativeFilePath(String absolutePath) {
        Path path = Paths.get(absolutePath);
        if (!path.isAbsolute()) {
            return normalizePath(absolutePath);
        }
        Path base = Paths.get("").toAbsolutePath();
        try {
            Path relativePath = base.relativize(path);
            return normalizePath(relativePath.toString());
        } catch (IllegalArgumentException e) {
            System.err.println("Cannot relativize the paths: " + e.getMessage());
            return normalizePath(absolutePath);
        }
    }

    public static String getRelativeFilePath(File selectedFile) {
        try {
            Path absolutePath = selectedFile.toPath();
            Path basePath = Paths.get("").toAbsolutePath();
            Path relativePath = basePath.relativize(absolutePath);
            return normalizePath(relativePath.toString());
        } catch (IllegalArgumentException e) {
            System.err.println("Cannot relativize the paths: " + e.getMessage());
            return normalizePath(selectedFile.getPath());
        }
    }

    private static String normalizePath(String path) {
        return path.replace('\\', '/');
    }


}
