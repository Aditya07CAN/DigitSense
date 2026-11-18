package com.digitsense.ml;

import com.digitsense.image.ImagePreprocessor;
import com.digitsense.letter.LetterClassifier;
import com.digitsense.model.DigitModel;
import com.digitsense.model.ModelFactory;
import com.digitsense.model.PredictionResult;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * Recogniser combining digit recognition (0-9) and letter recognition (A-Z)
 * under a unified interface as outlined in the DigitSense Alphabet Recogniser specification.
 */
public class Recogniser {

    private MultiLayerNetwork digitModel;
    private MultiLayerNetwork letterModel;
    private final DigitModel fallbackDigitModel;

    public record Result(String label, double confidence, boolean confident, String alternatives) {
        public Result(String label, double confidence) {
            this(label, confidence, confidence >= 0.60, "");
        }
    }

    public Recogniser() {
        this("digits_model.zip", "letters_model.zip");
    }

    public Recogniser(String digitModelPath, String letterModelPath) {
        fallbackDigitModel = ModelFactory.loadAvailableModel();

        File dFile = new File(digitModelPath);
        if (dFile.exists()) {
            try {
                digitModel = MultiLayerNetwork.load(dFile, false);
            } catch (IOException e) {
                System.err.println("Could not load digit model from " + digitModelPath + ": " + e.getMessage());
            }
        }

        File lFile = new File(letterModelPath);
        if (lFile.exists()) {
            try {
                letterModel = MultiLayerNetwork.load(lFile, false);
            } catch (IOException e) {
                System.err.println("Could not load letter model from " + letterModelPath + ": " + e.getMessage());
            }
        }
    }

    /**
     * Predict from 784-element float array (28x28 normalized pixels).
     *
     * @param pixels  784 float values (0.0 to 1.0)
     * @param letters true for Letter recognition (A-Z), false for Digit recognition (0-9)
     * @return Result containing recognized label and confidence
     */
    public Result predict(float[] pixels, boolean letters) {
        if (letters) {
            if (letterModel != null) {
                try {
                    INDArray input = Nd4j.create(pixels, new int[]{1, 784});
                    INDArray out = letterModel.output(input);
                    int idx = out.argMax(1).getInt(0);
                    double confidence = out.getDouble(idx);
                    // EMNIST letters: 1-indexed (1=A, 26=Z) or 0-indexed (0=A, 25=Z)
                    char letter = (char) ('A' + (idx < 26 ? idx : idx - 1));
                    return new Result(String.valueOf(letter), confidence);
                } catch (Exception e) {
                    // Fall back to dedicated classifier
                }
            }
            LetterClassifier.LetterResult lr = LetterClassifier.classify(pixels);
            return new Result(lr.letterString(), lr.confidence(), lr.confident(), lr.topSummary());
        } else {
            if (digitModel != null) {
                try {
                    INDArray input = Nd4j.create(pixels, new int[]{1, 784});
                    INDArray out = digitModel.output(input);
                    int idx = out.argMax(1).getInt(0);
                    double confidence = out.getDouble(idx);
                    return new Result(String.valueOf(idx), confidence);
                } catch (Exception e) {
                    // Fall back to fallbackDigitModel
                }
            }
            BufferedImage img = pixelsToImage(pixels);
            PredictionResult pr = fallbackDigitModel.predict(img);
            return new Result(String.valueOf(pr.digit()), pr.confidence(), pr.confidence() >= 0.60, pr.alternativesSummary());
        }
    }

    /**
     * Predict from a preprocessed BufferedImage (28x28).
     */
    public Result predict(BufferedImage processedImage, boolean letters) {
        if (letters) {
            LetterClassifier.LetterResult lr = LetterClassifier.classify(processedImage);
            return new Result(lr.letterString(), lr.confidence(), lr.confident(), lr.topSummary());
        } else {
            PredictionResult pr = fallbackDigitModel.predict(processedImage);
            return new Result(String.valueOf(pr.digit()), pr.confidence(), pr.confidence() >= 0.60, pr.alternativesSummary());
        }
    }

    private static BufferedImage pixelsToImage(float[] pixels) {
        BufferedImage img = new BufferedImage(28, 28, BufferedImage.TYPE_BYTE_GRAY);
        int idx = 0;
        for (int y = 0; y < 28; y++) {
            for (int x = 0; x < 28; x++) {
                int gray = (int) Math.round((1.0 - pixels[idx]) * 255);
                gray = Math.max(0, Math.min(255, gray));
                int rgb = (gray << 16) | (gray << 8) | gray;
                img.setRGB(x, y, rgb);
                idx++;
            }
        }
        return img;
    }
}
