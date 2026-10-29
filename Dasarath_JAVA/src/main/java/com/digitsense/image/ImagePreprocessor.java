package com.digitsense.image;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

/** Converts a canvas drawing into the 28 x 28 format expected by the model. */
public final class ImagePreprocessor {

    private static final int OUTPUT_SIZE = 28;
    private static final int DIGIT_SIZE = 20;
    private static final int WHITE_THRESHOLD = 245;

    private ImagePreprocessor() {
    }

    public static BufferedImage preprocess(BufferedImage source) {
        Bounds bounds = findInkBounds(source);
        BufferedImage output = createWhiteImage(OUTPUT_SIZE, OUTPUT_SIZE);

        if (bounds == null) {
            return output;
        }

        BufferedImage cropped = source.getSubimage(bounds.left, bounds.top,
                bounds.width(), bounds.height());

        int longerSide = Math.max(cropped.getWidth(), cropped.getHeight());
        int scaledWidth = Math.max(1, Math.round(cropped.getWidth()
                * (float) DIGIT_SIZE / longerSide));
        int scaledHeight = Math.max(1, Math.round(cropped.getHeight()
                * (float) DIGIT_SIZE / longerSide));

        Image scaled = cropped.getScaledInstance(scaledWidth, scaledHeight,
                Image.SCALE_SMOOTH);
        Graphics2D graphics = output.createGraphics();
        configureQuality(graphics);
        graphics.drawImage(scaled,
                (OUTPUT_SIZE - scaledWidth) / 2,
                (OUTPUT_SIZE - scaledHeight) / 2,
                null);
        graphics.dispose();

        return centerByMass(output);
    }

    private static Bounds findInkBounds(BufferedImage image) {
        int left = image.getWidth();
        int top = image.getHeight();
        int right = -1;
        int bottom = -1;

        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int gray = getGray(image.getRGB(x, y));
                if (gray < WHITE_THRESHOLD) {
                    left = Math.min(left, x);
                    top = Math.min(top, y);
                    right = Math.max(right, x);
                    bottom = Math.max(bottom, y);
                }
            }
        }

        return right < 0 ? null : new Bounds(left, top, right, bottom);
    }

    private static BufferedImage centerByMass(BufferedImage image) {
        double totalWeight = 0;
        double weightedX = 0;
        double weightedY = 0;

        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int darkness = 255 - getGray(image.getRGB(x, y));
                totalWeight += darkness;
                weightedX += x * darkness;
                weightedY += y * darkness;
            }
        }

        if (totalWeight == 0) {
            return image;
        }

        int shiftX = (int) Math.round(13.5 - weightedX / totalWeight);
        int shiftY = (int) Math.round(13.5 - weightedY / totalWeight);
        BufferedImage centered = createWhiteImage(OUTPUT_SIZE, OUTPUT_SIZE);

        Graphics2D graphics = centered.createGraphics();
        configureQuality(graphics);
        graphics.drawImage(image, shiftX, shiftY, null);
        graphics.dispose();
        return centered;
    }

    private static BufferedImage createWhiteImage(int width, int height) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
        Graphics2D graphics = image.createGraphics();
        graphics.setColor(java.awt.Color.WHITE);
        graphics.fillRect(0, 0, width, height);
        graphics.dispose();
        return image;
    }

    private static void configureQuality(Graphics2D graphics) {
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BILINEAR);
    }

    private static int getGray(int rgb) {
        int red = (rgb >> 16) & 0xff;
        int green = (rgb >> 8) & 0xff;
        int blue = rgb & 0xff;
        return (red + green + blue) / 3;
    }

    private record Bounds(int left, int top, int right, int bottom) {
        private int width() {
            return right - left + 1;
        }

        private int height() {
            return bottom - top + 1;
        }
    }
}

