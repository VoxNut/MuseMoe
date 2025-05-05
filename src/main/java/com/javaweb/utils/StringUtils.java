package com.javaweb.utils;

import javax.swing.*;
import java.awt.*;

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

    public static String getTruncatedText(String text, int maxLength) {
        if (text == null || text.length() <= maxLength) {
            return text;
        }
        return text.substring(0, maxLength - 3) + "...";
    }

    public static String getTruncatedText(String text) {
        return getTruncatedText(text, 15);
    }

    public static String getTruncatedTextByWidth(String text, Font font) {
        return getTruncatedTextByWidth(text, font, 100);
    }

    public static String getTruncatedTextByWidth(String text, Font font, int maxWidth) {
        if (text == null || text.isEmpty()) {
            return "";
        }

        FontMetrics metrics = new JLabel().getFontMetrics(font);
        String ellipsis = "…";
        int ellipsisWidth = metrics.stringWidth(ellipsis);

        if (metrics.stringWidth(text) <= maxWidth) {
            return text;
        }

        int low = 0;
        int high = text.length();

        while (low <= high) {
            int mid = (low + high) / 2;
            String candidate = text.substring(0, mid);
            int width = metrics.stringWidth(candidate + ellipsis);

            if (width <= maxWidth) {
                low = mid + 1;
            } else {
                high = mid - 1;
            }
        }

        if (high <= 0) {
            return ellipsis;
        }

        return text.substring(0, high) + ellipsis;
    }


}
