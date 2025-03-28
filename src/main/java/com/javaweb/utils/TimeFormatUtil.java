package com.javaweb.utils;

public class TimeFormatUtil {

    public static String formatDuration(int seconds) {
        int hours = seconds / 3600;
        int minutes = (seconds % 3600) / 60;
        int remainingSeconds = seconds % 60;

        String formattedTime;

        if (hours > 0) {
            formattedTime = String.format("%02d:%02d:%02d", hours, minutes, remainingSeconds);
        } else {
            formattedTime = String.format("%02d:%02d", minutes, remainingSeconds);
        }

        return formattedTime;
    }

    /**
     * Overloaded method that accepts long value for seconds
     */
    public static String formatDuration(long seconds) {
        return formatDuration((int) seconds);
    }
}