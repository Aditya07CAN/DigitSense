package com.digitsense.model;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

/** Loads a saved Deeplearning4j CNN and uses it for digit prediction. */
public class Dl4jDigitModel implements DigitModel {

    private final MultiLayerNetwork network;
    private final String modelVersion;

    public Dl4jDigitModel(File modelFile, String modelVersion) throws IOException {
        network = MultiLayerNetwork.load(modelFile, true);
        this.modelVersion = modelVersion;
    }

    @Override
    public PredictionResult predict(BufferedImage image) {
        INDArray input = Nd4j.create(1, 1, 28, 28);

        for (int y = 0; y < 28; y++) {
            for (int x = 0; x < 28; x++) {
                int rgb = image.getRGB(x, y);
                int red = (rgb >> 16) & 0xff;
                int green = (rgb >> 8) & 0xff;
                int blue = rgb & 0xff;
                double gray = (red + green + blue) / (255.0 * 3.0);
                input.putScalar(new int[]{0, 0, y, x}, 1.0 - gray);
            }
        }

        INDArray probabilities = network.output(input, false);
        int[] topDigits = new int[3];
        double[] topConfidences = new double[3];
        boolean[] selected = new boolean[10];

        for (int rank = 0; rank < topDigits.length; rank++) {
            int bestDigit = 0;
            double bestProbability = -1.0;
            for (int candidate = 0; candidate < 10; candidate++) {
                double probability = probabilities.getDouble(0, candidate);
                if (!selected[candidate] && probability > bestProbability) {
                    bestDigit = candidate;
                    bestProbability = probability;
                }
            }
            selected[bestDigit] = true;
            topDigits[rank] = bestDigit;
            topConfidences[rank] = bestProbability;
        }

        return new PredictionResult(topDigits[0], topConfidences[0], modelVersion,
                topDigits, topConfidences);
    }
}

