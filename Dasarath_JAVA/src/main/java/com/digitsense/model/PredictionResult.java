package com.digitsense.model;

/** Result returned by a digit-recognition model. */
public record PredictionResult(int digit, double confidence, String modelVersion,
                               int[] topDigits, double[] topConfidences) {

    public PredictionResult(int digit, double confidence, String modelVersion) {
        this(digit, confidence, modelVersion,
                new int[]{digit}, new double[]{confidence});
    }

    public boolean isValidDigit() {
        return digit >= 0 && digit <= 9;
    }

    public String topSummary() {
        StringBuilder summary = new StringBuilder();
        for (int i = 0; i < topDigits.length; i++) {
            if (i > 0) {
                summary.append(", ");
            }
            summary.append(topDigits[i]).append(" (")
                    .append(Math.round(topConfidences[i] * 100)).append("%)");
        }
        return summary.toString();
    }

    public String alternativesSummary() {
        if (topDigits.length <= 1) {
            return "";
        }
        StringBuilder summary = new StringBuilder();
        for (int i = 1; i < topDigits.length; i++) {
            if (i > 1) {
                summary.append(", ");
            }
            summary.append(topDigits[i]).append(" (")
                    .append(Math.round(topConfidences[i] * 100)).append("%)");
        }
        return summary.toString();
    }
}

