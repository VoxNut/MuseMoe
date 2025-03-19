package com.javaweb.utils;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.Random;

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
        // Check if the priceText contains a percentage sign
        boolean isPercentage = priceText.contains("%");

        // Remove non-numeric characters except for the decimal separator
        String cleanedPrice = priceText.replaceAll("[^\\d,]", "").replace(",", ".");

        // Parse the cleaned price
        Double parsedPrice = Double.parseDouble(cleanedPrice);

        // If it was a percentage, return the parsed price as is
        if (isPercentage) {
            return parsedPrice;
        }

        return parsedPrice;
    }

}
