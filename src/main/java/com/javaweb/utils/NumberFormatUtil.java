package com.javaweb.utils;

import java.text.NumberFormat;
import java.util.Locale;

public class NumberFormatUtil {
    public static String formatPrice(Double price) {
        NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.GERMANY);
        return numberFormat.format(price) + "đ";
    }

    public static String formatValue(Double price) {
        NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.JAPANESE);
        return numberFormat.format(price) + "%";
    }

    public static Double parsePrice(String priceText) {
        if (priceText.isEmpty()) {
            return 0.0;
        }
        boolean isPercentage = priceText.contains("%");

        String cleanedPrice = priceText.replaceAll("[^\\d,]", "").replace(",", ".");

        Double parsedPrice = Double.parseDouble(cleanedPrice);

        return parsedPrice;
    }

    public static String formatWithCommas(Integer number) {
        if (number == null) {
            return "-";
        }
        NumberFormat formatter = NumberFormat.getNumberInstance(Locale.US);
        return formatter.format(number);
    }

}
