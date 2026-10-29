package com.digitsense.image;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

/** Splits a canvas image into separate digits using vertical ink gaps. */
public final class DigitSegmenter {

    private static final int WHITE_THRESHOLD = 245;
    private static final int MIN_SEGMENT_WIDTH = 3;

    private DigitSegmenter() {
    }

    public static List<BufferedImage> segment(BufferedImage source) {
        List<ColumnRange> ranges = findColumnRanges(source);
        List<BufferedImage> digits = new ArrayList<>();

        for (ColumnRange range : ranges) {
            int top = source.getHeight();
            int bottom = -1;
            for (int y = 0; y < source.getHeight(); y++) {
                for (int x = range.start; x <= range.end; x++) {
                    if (gray(source.getRGB(x, y)) < WHITE_THRESHOLD) {
                        top = Math.min(top, y);
                        bottom = Math.max(bottom, y);
                    }
                }
            }

            if (bottom >= top) {
                digits.add(copyRegion(source, range.start, top,
                        range.end - range.start + 1, bottom - top + 1));
            }
        }
        return digits;
    }

    private static List<ColumnRange> findColumnRanges(BufferedImage source) {
        List<ColumnRange> ranges = new ArrayList<>();
        int start = -1;

        for (int x = 0; x < source.getWidth(); x++) {
            boolean hasInk = false;
            for (int y = 0; y < source.getHeight(); y++) {
                if (gray(source.getRGB(x, y)) < WHITE_THRESHOLD) {
                    hasInk = true;
                    break;
                }
            }

            if (hasInk && start < 0) {
                start = x;
            } else if (!hasInk && start >= 0) {
                int end = x - 1;
                if (end - start + 1 >= MIN_SEGMENT_WIDTH) {
                    ranges.add(new ColumnRange(start, end));
                }
                start = -1;
            }
        }

        if (start >= 0) {
            int end = source.getWidth() - 1;
            if (end - start + 1 >= MIN_SEGMENT_WIDTH) {
                ranges.add(new ColumnRange(start, end));
            }
        }
        return ranges;
    }

    private static BufferedImage copyRegion(BufferedImage source,
                                            int left, int top,
                                            int width, int height) {
        BufferedImage copy = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = copy.createGraphics();
        graphics.setColor(Color.WHITE);
        graphics.fillRect(0, 0, width, height);
        graphics.drawImage(source, -left, -top, null);
        graphics.dispose();
        return copy;
    }

    private static int gray(int rgb) {
        int red = (rgb >> 16) & 0xff;
        int green = (rgb >> 8) & 0xff;
        int blue = rgb & 0xff;
        return (red + green + blue) / 3;
    }

    private record ColumnRange(int start, int end) {
    }
}

