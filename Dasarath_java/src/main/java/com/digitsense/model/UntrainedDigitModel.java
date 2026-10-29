package com.digitsense.model;

import java.awt.image.BufferedImage;

/** Temporary model used until the trained CNN is added. */
public class UntrainedDigitModel implements DigitModel {

    @Override
    public PredictionResult predict(BufferedImage image) {
        return new PredictionResult(-1, 0.0, "pre-cnn");
    }
}

