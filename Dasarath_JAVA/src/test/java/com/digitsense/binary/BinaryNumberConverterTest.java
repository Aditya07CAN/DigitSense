package com.digitsense.binary;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class BinaryNumberConverterTest {

    @Test
    void convertsUsingEightFourTwoOneWeights() {
        BinaryNumberConverter.Conversion conversion = BinaryNumberConverter.convert("0001");

        assertEquals("1", conversion.decimal().toString());
        assertEquals("(0 × 8) + (0 × 4) + (0 × 2) + (1 × 1)",
                conversion.calculation());
    }

    @Test
    void convertsTenFromBinary1010() {
        assertEquals("10", BinaryNumberConverter.convert("1010").decimal().toString());
    }

    @Test
    void rejectsNonBinaryValues() {
        assertThrows(IllegalArgumentException.class,
                () -> BinaryNumberConverter.convert("1021"));
    }
}
