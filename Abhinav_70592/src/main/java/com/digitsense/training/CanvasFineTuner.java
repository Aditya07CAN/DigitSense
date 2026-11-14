package com.digitsense.training;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;
import org.deeplearning4j.datasets.iterator.utilty.ListDataSetIterator;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.factory.Nd4j;

/** Fine-tunes the MNIST CNN with consented, corrected canvas samples. */
public class CanvasFineTuner {

    private static final File MODEL_FILE = new File("models/digitsense-cnn.zip");
    private static final File BASE_MODEL_FILE = new File("models/digitsense-cnn-base.zip");
    private static final File SAMPLE_ROOT = new File("data/canvas-training");
    private static final int FINE_TUNE_EPOCHS = 5;

    public static void main(String[] args) throws Exception {
        if (!BASE_MODEL_FILE.isFile()) {
            throw new IllegalStateException("Train the MNIST model before fine-tuning");
        }

        List<DataSet> samples = loadSamples();
        if (samples.isEmpty()) {
            throw new IllegalStateException(
                    "No canvas samples found. Use Correct with Consent first.");
        }

        for (int digit = 0; digit <= 9; digit++) {
            File digitDirectory = new File(SAMPLE_ROOT, String.valueOf(digit));
            File[] files = digitDirectory.listFiles((directory, name) ->
                    name.toLowerCase().endsWith(".png"));
            if (files == null || files.length < 5) {
                throw new IllegalStateException(
                        "Need at least 5 corrected samples for every digit before fine-tuning. "
                                + "Missing digit: " + digit);
            }
        }

        MultiLayerNetwork network = MultiLayerNetwork.load(BASE_MODEL_FILE, true);
        ListDataSetIterator<DataSet> iterator = new ListDataSetIterator<>(samples, 16);

        for (int epoch = 1; epoch <= FINE_TUNE_EPOCHS; epoch++) {
            iterator.reset();
            network.fit(iterator);
            System.out.println("Completed canvas fine-tuning epoch " + epoch
                    + " of " + FINE_TUNE_EPOCHS);
        }

        network.save(MODEL_FILE, true);
        System.out.println("Updated model: " + MODEL_FILE.getPath());
    }

    private static List<DataSet> loadSamples() throws IOException {
        List<DataSet> dataSets = new ArrayList<>();
        for (int digit = 0; digit <= 9; digit++) {
            File digitDirectory = new File(SAMPLE_ROOT, String.valueOf(digit));
            File[] files = digitDirectory.listFiles((directory, name) ->
                    name.toLowerCase().endsWith(".png"));
            if (files == null) {
                continue;
            }

            for (File file : files) {
                BufferedImage image = ImageIO.read(file);
                if (image != null) {
                    addAugmentedSamples(dataSets, image, digit);
                }
            }
        }
        return dataSets;
    }

    private static void addAugmentedSamples(List<DataSet> dataSets,
                                             BufferedImage image, int digit) {
        int[][] shifts = {{0, 0}, {-1, 0}, {1, 0}, {0, -1}, {0, 1}};
        for (int[] shift : shifts) {
            BufferedImage shifted = shiftImage(image, shift[0], shift[1]);
            var features = Nd4j.create(1, 1, 28, 28);
            var labels = Nd4j.zeros(1, 10);

            for (int y = 0; y < 28; y++) {
                for (int x = 0; x < 28; x++) {
                    int gray = shifted.getRGB(x, y) & 0xff;
                    features.putScalar(new int[]{0, 0, y, x}, 1.0 - gray / 255.0);
                }
            }
            labels.putScalar(0, digit, 1.0);
            dataSets.add(new DataSet(features, labels));
        }
    }

    private static BufferedImage shiftImage(BufferedImage source, int shiftX, int shiftY) {
        BufferedImage shifted = new BufferedImage(28, 28, BufferedImage.TYPE_BYTE_GRAY);
        Graphics2D graphics = shifted.createGraphics();
        graphics.setColor(java.awt.Color.WHITE);
        graphics.fillRect(0, 0, 28, 28);
        graphics.drawImage(source, shiftX, shiftY, null);
        graphics.dispose();
        return shifted;
    }
}

