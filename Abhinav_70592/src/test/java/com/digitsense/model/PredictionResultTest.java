package com.digitsense.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class PredictionResultTest {

    @Test
    void formatsAlternativePredictions() {
        PredictionResult result = new PredictionResult(5, 0.8, "test",
                new int[]{5, 6, 8}, new double[]{0.8, 0.15, 0.05});

        assertEquals("6 (15%), 8 (5%)", result.alternativesSummary());
    }
}

