package com.digitsense.model;

import java.awt.image.BufferedImage;

/** Contract used by the application to ask a model to recognize a digit. */
public interface DigitModel {

    PredictionResult predict(BufferedImage image);
}

