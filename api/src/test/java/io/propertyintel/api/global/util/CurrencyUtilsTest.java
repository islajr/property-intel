package io.propertyintel.api.global.util;

import org.junit.jupiter.api.Test;
import java.text.NumberFormat;
import static org.junit.jupiter.api.Assertions.*;

class CurrencyUtilsTest {

    @Test
    void testFormatKoboToNaira() {
        assertNull(CurrencyUtils.formatKoboToNaira(null));
        
        String expected = NumberFormat.getCurrencyInstance(CurrencyUtils.NIGERIA_LOCALE).format(1.00);
        assertEquals(expected, CurrencyUtils.formatKoboToNaira(100L));

        String expectedLarge = NumberFormat.getCurrencyInstance(CurrencyUtils.NIGERIA_LOCALE).format(12345.67);
        assertEquals(expectedLarge, CurrencyUtils.formatKoboToNaira(1234567L));

        String expectedZero = NumberFormat.getCurrencyInstance(CurrencyUtils.NIGERIA_LOCALE).format(0.00);
        assertEquals(expectedZero, CurrencyUtils.formatKoboToNaira(0L));
    }

    @Test
    void testFormatDoubleKoboToNaira() {
        assertNull(CurrencyUtils.formatDoubleKoboToNaira(null));

        String expected = NumberFormat.getCurrencyInstance(CurrencyUtils.NIGERIA_LOCALE).format(1.23);
        assertEquals(expected, CurrencyUtils.formatDoubleKoboToNaira(123.0));

        String expectedDecimal = NumberFormat.getCurrencyInstance(CurrencyUtils.NIGERIA_LOCALE).format(12.3456);
        assertEquals(expectedDecimal, CurrencyUtils.formatDoubleKoboToNaira(1234.56));
    }

    @Test
    void testFormatRawNairaNoDecimals() {
        assertNull(CurrencyUtils.formatRawNairaNoDecimals(null));

        NumberFormat formatter = NumberFormat.getCurrencyInstance(CurrencyUtils.NIGERIA_LOCALE);
        formatter.setMaximumFractionDigits(0);

        String expected = formatter.format(1000L);
        assertEquals(expected, CurrencyUtils.formatRawNairaNoDecimals(1000L));
    }
}
