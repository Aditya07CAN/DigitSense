package com.digitsense.image;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import org.junit.jupiter.api.Test;

class ImagePreprocessorTest {

    @Test
    void blankImageProduces28By28Output() {
        BufferedImage source = new BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = source.createGraphics();
        graphics.setColor(Color.WHITE);
        graphics.fillRect(0, 0, 100, 100);
        graphics.dispose();

        BufferedImage result = ImagePreprocessor.preprocess(source);

        assertEquals(28, result.getWidth());
        assertEquals(28, result.getHeight());
    }
}

