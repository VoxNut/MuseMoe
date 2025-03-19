package com.javaweb.utils;

import org.jdatepicker.impl.JDatePickerImpl;

import javax.swing.*;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;

public class DateUtil {

    public static String formatDate(Date date) {
        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss dd-MM-yyyy");
        return formatter.format(date);
    }

    public static LocalDate convertToLocalDate(JDatePickerImpl datePicker) {
        if (datePicker == null) return null;
        if (datePicker.getModel().getValue() != null) {
            Object value = datePicker.getModel().getValue();
            if (value instanceof java.sql.Date) {
                java.sql.Date sqlDate = (java.sql.Date) value;
                return sqlDate.toLocalDate();
            } else if (value instanceof java.util.Date) {
                java.util.Date utilDate = (java.util.Date) value;
                return utilDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            } else {
                // Handle other types if necessary
                throw new IllegalArgumentException("Unsupported date type: " + value.getClass().getName());
            }
        }
        return null;
    }

    public static boolean isSameDay(LocalDate date1, LocalDate date2) {
        return date1.getDayOfMonth() == date2.getDayOfMonth();
    }

    public static boolean isSameMonth(LocalDate date1, LocalDate date2) {
        return date1.getMonth() == date2.getMonth();
    }

    public static boolean isSameMonthDay(LocalDate date1, LocalDate date2) {
        return isSameMonth(date1, date2) && isSameDay(date2, date1);
    }

    public static boolean isSameYear(LocalDate date1, LocalDate date2) {
        return date1.getYear() == date2.getYear();
    }

    public static boolean isSameDayMonthYear(LocalDate date1, LocalDate date2) {
        return isSameDay(date1, date2) && isSameMonth(date2, date1) && isSameYear(date2, date1);
    }

    public static boolean isSameMonthYear(LocalDate date1, LocalDate date2) {
        return isSameMonth(date1, date2) && isSameYear(date2, date1);
    }


    public static boolean isInWeek(LocalDate dat1, LocalDate dat2) {
        return Math.abs(dat1.getDayOfMonth() - dat2.getDayOfMonth()) <= 7;
    }

    public static Date truncateMilliseconds(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    public static Date combineDateAndTime(JDatePickerImpl datePicker, JSpinner timeSpinner) {
        if (datePicker.getModel().getValue() == null) {
            throw new IllegalArgumentException("Date must be selected");
        }

        Calendar calendar = Calendar.getInstance();
        calendar.setTime((Date) datePicker.getModel().getValue());

        Calendar timeCal = Calendar.getInstance();
        timeCal.setTime((Date) timeSpinner.getValue());

        calendar.set(Calendar.HOUR_OF_DAY, timeCal.get(Calendar.HOUR_OF_DAY));
        calendar.set(Calendar.MINUTE, timeCal.get(Calendar.MINUTE));
        calendar.set(Calendar.SECOND, timeCal.get(Calendar.SECOND));

        return calendar.getTime();
    }
}
