package com.digitsense.binary;

import java.math.BigInteger;

/** Converts a binary digit string using the weighted place-value method. */
public final class BinaryNumberConverter {

    private BinaryNumberConverter() {
    }

    public static Conversion convert(String bits) {
        if (bits == null || bits.isBlank() || !bits.matches("[01]+")) {
            throw new IllegalArgumentException("Binary value must contain only 0 and 1");
        }

        BigInteger decimal = BigInteger.ZERO;
        StringBuilder calculation = new StringBuilder();
        for (int index = 0; index < bits.length(); index++) {
            int bit = bits.charAt(index) - '0';
            int power = bits.length() - index - 1;
            BigInteger placeValue = BigInteger.ONE.shiftLeft(power);
            decimal = decimal.add(placeValue.multiply(BigInteger.valueOf(bit)));

            if (index > 0) {
                calculation.append(" + ");
            }
            calculation.append('(').append(bit).append(" × ")
                    .append(placeValue).append(')');
        }

        return new Conversion(bits, decimal, calculation.toString());
    }

    public record Conversion(String binary, BigInteger decimal, String calculation) {
        public String displayText() {
            return "Binary: " + binary + "    " + calculation + " = " + decimal;
        }
    }
}
