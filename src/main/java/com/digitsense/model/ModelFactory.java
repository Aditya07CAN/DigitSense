package com.digitsense.model;

import java.io.File;
import java.io.IOException;

/** Selects the trained model when it exists and otherwise keeps the app runnable. */
public final class ModelFactory {

    private static final File MODEL_FILE = new File("models/digitsense-cnn.zip");

    private ModelFactory() {
    }

    public static DigitModel loadAvailableModel() {
        if (!MODEL_FILE.isFile()) {
            return new UntrainedDigitModel();
        }

        try {
            return new Dl4jDigitModel(MODEL_FILE, "digitsense-cnn-v1");
        } catch (IOException | RuntimeException exception) {
            System.err.println("CNN model could not be loaded: " + exception.getMessage());
            return new UntrainedDigitModel();
        }
    }
}

