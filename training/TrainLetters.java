package com.digitsense.training;

import org.deeplearning4j.datasets.iterator.impl.EmnistDataSetIterator;
import org.eclipse.deeplearning4j.resources.utils.EMnistSet;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.inputs.InputType;
import org.deeplearning4j.nn.conf.layers.ConvolutionLayer;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.conf.layers.PoolingType;
import org.deeplearning4j.nn.conf.layers.SubsamplingLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.learning.config.Adam;
import org.nd4j.linalg.lossfunctions.LossFunctions;

import java.io.File;

/**
 * Trains the Letter Recognition CNN model using EMNIST Letters dataset.
 * Part of DigitSense: Adding an Alphabet Recogniser (Section 6).
 */
public class TrainLetters {

    public static void main(String[] args) throws Exception {
        System.out.println("Initializing EMNIST Letters dataset iterator...");
        int batch = 128;
        EMnistSet set = EMnistSet.LETTERS;
        DataSetIterator train = new EmnistDataSetIterator(set, batch, true);
        DataSetIterator test  = new EmnistDataSetIterator(set, batch, false);
        int classes = EmnistDataSetIterator.numLabels(set);

        System.out.println("Building Convolutional Neural Network architecture (" + classes + " classes)...");
        MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
                .seed(123)
                .updater(new Adam(1e-3))
                .list()
                .layer(new ConvolutionLayer.Builder(5, 5).nIn(1).nOut(32)
                        .activation(Activation.RELU).build())
                .layer(new SubsamplingLayer.Builder(PoolingType.MAX)
                        .kernelSize(2, 2).stride(2, 2).build())
                .layer(new ConvolutionLayer.Builder(5, 5).nOut(64)
                        .activation(Activation.RELU).build())
                .layer(new SubsamplingLayer.Builder(PoolingType.MAX)
                        .kernelSize(2, 2).stride(2, 2).build())
                .layer(new DenseLayer.Builder().nOut(256)
                        .activation(Activation.RELU).build())
                .layer(new OutputLayer.Builder(LossFunctions.LossFunction.MCXENT)
                        .nOut(classes).activation(Activation.SOFTMAX).build())
                .setInputType(InputType.convolutionalFlat(28, 28, 1))
                .build();

        MultiLayerNetwork model = new MultiLayerNetwork(conf);
        model.init();
        model.setListeners(new ScoreIterationListener(100));

        System.out.println("Training model across 5 epochs...");
        for (int epoch = 0; epoch < 5; epoch++) {
            model.fit(train);
            System.out.println("Epoch " + epoch + " done");
        }

        System.out.println("Evaluating model on test dataset:");
        System.out.println(model.evaluate(test).stats());

        File outputFile = new File("letters_model.zip");
        model.save(outputFile);
        System.out.println("Saved letter recognition model to: " + outputFile.getAbsolutePath());
    }
}
