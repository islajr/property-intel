package io.propertyintel.api.global.util;

import java.text.NumberFormat;
import java.util.Locale;

public class CurrencyUtils {
    public static final Locale NIGERIA_LOCALE = Locale.forLanguageTag("en-NG");

    public static String formatKoboToNaira(Long kobo) {
        if (kobo == null) return null;

        double naira = kobo / 100.0;
        NumberFormat formatter = NumberFormat.getCurrencyInstance(NIGERIA_LOCALE);

        return formatter.format(naira);
    }

    public static String formatDoubleKoboToNaira(Double kobo) {
        if (kobo == null) return null;

        double naira = kobo / 100.0;
        NumberFormat formatter = NumberFormat.getCurrencyInstance(NIGERIA_LOCALE);

        return formatter.format(naira);
    }



    public static String formatRawNairaNoDecimals(Long rawNaira) {
        if (rawNaira == null) return null;

        NumberFormat formatter = NumberFormat.getCurrencyInstance(NIGERIA_LOCALE);
        formatter.setMaximumFractionDigits(0);

        return formatter.format(rawNaira);
    }
}
