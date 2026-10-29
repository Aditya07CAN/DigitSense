package com.digitsense.training;

import com.digitsense.database.DatabaseManager;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import org.deeplearning4j.datasets.iterator.impl.MnistDataSetIterator;
import org.deeplearning4j.eval.Evaluation;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.inputs.InputType;
import org.deeplearning4j.nn.conf.layers.ConvolutionLayer;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.conf.layers.SubsamplingLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.learning.config.Adam;
import org.nd4j.linalg.lossfunctions.LossFunctions;

/** Trains and saves the first DigitSense CNN using the MNIST dataset. */
@SuppressWarnings("deprecation")
public class MnistCnnTrainer {

    private static final int BATCH_SIZE = 64;
    private static final int EPOCHS = 3;
    private static final int SEED = 12345;
    private static final File MODEL_FILE = new File("models/digitsense-cnn.zip");
    private static final File BASE_MODEL_FILE = new File("models/digitsense-cnn-base.zip");

    public static void main(String[] args) throws Exception {
        System.out.println("Preparing MNIST data...");

        MnistDataSetIterator trainIterator = new MnistDataSetIterator(
                BATCH_SIZE, true, SEED);
        MnistDataSetIterator testIterator = new MnistDataSetIterator(
                BATCH_SIZE, false, SEED);

        MultiLayerNetwork network = new MultiLayerNetwork(buildConfiguration());
        network.init();
        network.setListeners(new TrainingListener());

        System.out.println("Training CNN...");
        for (int epoch = 1; epoch <= EPOCHS; epoch++) {
            trainIterator.reset();
            network.fit(trainIterator);
            System.out.println("Completed epoch " + epoch + " of " + EPOCHS);
        }

        System.out.println("Evaluating CNN...");
        testIterator.reset();
        Evaluation evaluation = network.evaluate(testIterator);
        System.out.println(evaluation.stats());

        DatabaseManager databaseManager = new DatabaseManager();
        databaseManager.saveModelRun("digitsense-cnn-v1", evaluation.accuracy(),
                evaluation.f1(), null, null, "MNIST", SEED);
        System.out.println("Logged model metrics in data/digitsense.db");
        
        File parent = MODEL_FILE.getParentFile();
        if (parent != null) {
            parent.mkdirs();
        }
        network.save(MODEL_FILE, true);
        Files.copy(MODEL_FILE.toPath(), BASE_MODEL_FILE.toPath(),
                StandardCopyOption.REPLACE_EXISTING);
        System.out.println("Saved model to " + MODEL_FILE.getPath());
        System.out.println("Saved untouched base model to " + BASE_MODEL_FILE.getPath());
    }

    private static MultiLayerConfiguration buildConfiguration() {
        return new NeuralNetConfiguration.Builder()
                .seed(SEED)
                .weightInit(WeightInit.XAVIER)
                .updater(new Adam(0.001))
                .list()
                .layer(new ConvolutionLayer.Builder(5, 5)
                        .nIn(1)
                        .nOut(20)
                        .stride(1, 1)
                        .activation(Activation.RELU)
                        .build())
                .layer(new SubsamplingLayer.Builder(SubsamplingLayer.PoolingType.MAX)
                        .kernelSize(2, 2)
                        .stride(2, 2)
                        .build())
                .layer(new DenseLayer.Builder()
                        .nOut(100)
                        .activation(Activation.RELU)
                        .build())
                .layer(new OutputLayer.Builder(LossFunctions.LossFunction.NEGATIVELOGLIKELIHOOD)
                        .nOut(10)
                        .activation(Activation.SOFTMAX)
                        .build())
                .setInputType(InputType.convolutionalFlat(28, 28, 1))
                .build();
    }

    private static class TrainingListener
            extends org.deeplearning4j.optimize.listeners.ScoreIterationListener {

        private TrainingListener() {
            super(1000);
        }
    }
}

