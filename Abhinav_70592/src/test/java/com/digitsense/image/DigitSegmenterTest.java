package com.digitsense.image;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import org.junit.jupiter.api.Test;

class DigitSegmenterTest {

    @Test
    void separatedInkRegionsBecomeTwoSegments() {
        BufferedImage source = new BufferedImage(100, 60, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = source.createGraphics();
        graphics.setColor(Color.WHITE);
        graphics.fillRect(0, 0, 100, 60);
        graphics.setColor(Color.BLACK);
        graphics.fillRect(15, 10, 20, 40);
        graphics.fillRect(65, 10, 20, 40);
        graphics.dispose();

        assertEquals(2, DigitSegmenter.segment(source).size());
    }
}

