package com.javaweb.utils;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

public class StringUtils {
    public static boolean isBlank(String str) {
        int strLen;
        if (str == null || (strLen = str.length()) == 0) {
            return true;
        }
        for (int i = 0; i < strLen; i++) {
            if ((!Character.isWhitespace(str.charAt(i)))) {
                return false;
            }
        }
        return true;
    }

    public static boolean isNotBlank(String str) {
        return !isBlank(str);
    }

    public static String getRelativeFilePath(File selectedFile) {
        try {
            Path absolutePath = selectedFile.toPath();
            Path basePath = Paths.get("").toAbsolutePath();
            Path relativePath = basePath.relativize(absolutePath);
            return relativePath.toString();
        } catch (IllegalArgumentException e) {
            System.err.println("Cannot relativize the paths: " + e.getMessage());
            return selectedFile.getPath();
        }
    }

    public static String getRelativeFilePath(String absolutePath) {
        Path path = Paths.get(absolutePath);
        if (!path.isAbsolute()) {
            return absolutePath;
        }
        Path base = Paths.get("").toAbsolutePath();
        try {
            Path relativePath = base.relativize(path);
            return relativePath.toString();
        } catch (IllegalArgumentException e) {
            System.err.println("Cannot relativize the paths: " + e.getMessage());
            return absolutePath;
        }
    }

}
