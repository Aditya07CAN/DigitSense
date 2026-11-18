package com.digitsense.roman;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Converts decimal numbers to Roman numerals and provides a step-by-step
 * place-value breakdown, following standard and extended Roman numeral rules.
 * 
 * Contributor: sanjeevtondepu-oss
 */
public final class RomanNumeralConverter {

    private static final LinkedHashMap<Integer, String> ROMAN_NUMERALS = new LinkedHashMap<>();

    static {
        ROMAN_NUMERALS.put(1000, "M");
        ROMAN_NUMERALS.put(900, "CM");
        ROMAN_NUMERALS.put(500, "D");
        ROMAN_NUMERALS.put(400, "CD");
        ROMAN_NUMERALS.put(100, "C");
        ROMAN_NUMERALS.put(90, "XC");
        ROMAN_NUMERALS.put(50, "L");
        ROMAN_NUMERALS.put(40, "XL");
        ROMAN_NUMERALS.put(10, "X");
        ROMAN_NUMERALS.put(9, "IX");
        ROMAN_NUMERALS.put(5, "V");
        ROMAN_NUMERALS.put(4, "IV");
        ROMAN_NUMERALS.put(1, "I");
    }

    private RomanNumeralConverter() {
    }

    /**
     * Converts an integer to Roman numerals.
     * - 0 maps to 'N' (nulla)
     * - 1 to 3999 uses standard Roman numerals
     * - 4000+ uses extended vinculum bracket notation: e.g. 15110 -> [XV]CX (15k + 110)
     */
    public static Conversion toRoman(int number) {
        if (number < 0) {
            throw new IllegalArgumentException("Roman numerals cannot represent negative numbers: " + number);
        }

        if (number == 0) {
            return new Conversion(0, "N", "0 (nulla — classical Latin for none/zero)");
        }

        StringBuilder roman = new StringBuilder();
        StringBuilder breakdown = new StringBuilder();
        boolean first = true;
        int remainder = number;

        // For large numbers >= 4000, represent thousands in brackets: [XV] = 15,000
        if (remainder >= 4000) {
            int thousands = remainder / 1000;
            remainder = remainder % 1000;
            Conversion thousandsConv = toStandardRoman(thousands);
            roman.append('[').append(thousandsConv.roman()).append(']');
            breakdown.append(thousands * 1000).append("([").append(thousandsConv.roman()).append("] × 1000)");
            first = false;
        }

        if (remainder > 0) {
            for (Map.Entry<Integer, String> entry : ROMAN_NUMERALS.entrySet()) {
                int value = entry.getKey();
                String symbol = entry.getValue();

                while (remainder >= value) {
                    roman.append(symbol);
                    if (!first) {
                        breakdown.append(" + ");
                    }
                    breakdown.append(value).append('(').append(symbol).append(')');
                    remainder -= value;
                    first = false;
                }
            }
        }

        return new Conversion(number, roman.toString(), breakdown.toString());
    }

    private static Conversion toStandardRoman(int number) {
        int remainder = number;
        StringBuilder roman = new StringBuilder();
        StringBuilder breakdown = new StringBuilder();
        boolean first = true;

        for (Map.Entry<Integer, String> entry : ROMAN_NUMERALS.entrySet()) {
            int value = entry.getKey();
            String symbol = entry.getValue();

            while (remainder >= value) {
                roman.append(symbol);
                if (!first) {
                    breakdown.append(" + ");
                }
                breakdown.append(value).append('(').append(symbol).append(')');
                remainder -= value;
                first = false;
            }
        }
        return new Conversion(number, roman.toString(), breakdown.toString());
    }

    /**
     * Converts a numeric digit string to Roman numeral representation.
     */
    public static Conversion toRoman(String digits) {
        if (digits == null || digits.isBlank() || !digits.matches("\\d+")) {
            throw new IllegalArgumentException("Input must be decimal digits (0-9)");
        }
        try {
            int value = Integer.parseInt(digits);
            return toRoman(value);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Number is too large to convert: " + digits);
        }
    }

    /**
     * Converts a Roman numeral string back to its decimal value.
     */
    public static int toDecimal(String roman) {
        if (roman == null || roman.isBlank()) {
            throw new IllegalArgumentException("Roman numeral cannot be null or empty");
        }

        String upper = roman.trim().toUpperCase();
        if ("N".equals(upper) || "NULLA".equals(upper)) {
            return 0;
        }

        if (!upper.matches("^M{0,3}(CM|CD|D?C{0,3})(XC|XL|L?X{0,3})(IX|IV|V?I{0,3})$") || upper.isEmpty()) {
            throw new IllegalArgumentException("Invalid Roman numeral string: " + roman);
        }

        int total = 0;
        int prevValue = 0;

        for (int i = upper.length() - 1; i >= 0; i--) {
            int current = charValue(upper.charAt(i));
            if (current < prevValue) {
                total -= current;
            } else {
                total += current;
            }
            prevValue = current;
        }

        return total;
    }

    private static int charValue(char ch) {
        return switch (ch) {
            case 'I' -> 1;
            case 'V' -> 5;
            case 'X' -> 10;
            case 'L' -> 50;
            case 'C' -> 100;
            case 'D' -> 500;
            case 'M' -> 1000;
            default -> throw new IllegalArgumentException("Unexpected Roman numeral character: " + ch);
        };
    }

    public record Conversion(int decimal, String roman, String breakdown) {
        public String displayText() {
            return "Roman: " + roman + "    (" + breakdown + " = " + decimal + ")";
        }
    }
}
