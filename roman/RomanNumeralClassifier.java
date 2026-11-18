package com.digitsense.roman;

import com.digitsense.image.ImagePreprocessor;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Dedicated classifier for recognizing handwritten Roman numeral characters:
 * 'I', 'V', 'X', 'L', 'C', 'D', 'M'.
 * 
 * Uses a hybrid approach:
 * 1. Multi-prototype template correlation across diverse font styles, weights, and rotations.
 * 2. Topological & geometric stroke analysis (junctions, openings, aspect ratio, vertical profiles).
 * 
 * Contributor: sanjeevtondepu-oss
 */
public final class RomanNumeralClassifier {

    public static final char[] ROMAN_CHARS = {'I', 'V', 'X', 'L', 'C', 'D', 'M'};

    private static final List<Prototype> PROTOTYPES = new ArrayList<>();

    static {
        initializePrototypes();
    }

    private RomanNumeralClassifier() {
    }

    private static void initializePrototypes() {
        String[] fontNames = {Font.SANS_SERIF, Font.SERIF, "Dialog", "Arial", "Times New Roman", "Segoe UI"};
        int[] fontStyles = {Font.PLAIN, Font.BOLD};
        int[] sizes = {26, 30, 34};
        double[] angles = {-12.0, -6.0, 0.0, 6.0, 12.0};

        for (char ch : ROMAN_CHARS) {
            String text = String.valueOf(ch);
            for (String fontName : fontNames) {
                for (int style : fontStyles) {
                    for (int size : sizes) {
                        for (double angle : angles) {
                            BufferedImage canvas = new BufferedImage(60, 60, BufferedImage.TYPE_BYTE_GRAY);
                            Graphics2D g = canvas.createGraphics();
                            g.setColor(Color.WHITE);
                            g.fillRect(0, 0, 60, 60);
                            g.setColor(Color.BLACK);
                            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

                            AffineTransform at = new AffineTransform();
                            at.translate(30, 30);
                            at.rotate(Math.toRadians(angle));
                            at.translate(-30, -30);
                            g.setTransform(at);

                            Font font = new Font(fontName, style, size);
                            g.setFont(font);
                            java.awt.FontMetrics fm = g.getFontMetrics();
                            int x = (60 - fm.stringWidth(text)) / 2;
                            int y = ((60 - fm.getHeight()) / 2) + fm.getAscent();
                            g.drawString(text, x, y);
                            g.dispose();

                            BufferedImage preprocessed = ImagePreprocessor.preprocess(canvas);
                            double[] features = extractVector(preprocessed);
                            PROTOTYPES.add(new Prototype(ch, features));
                        }
                    }
                }
            }

            // Also add synthetic handwritten strokes for extra robustness
            addHandwrittenStrokes(ch);
        }
    }

    private static void addHandwrittenStrokes(char ch) {
        int[] strokeWidths = {2, 3, 4};
        for (int sw : strokeWidths) {
            BufferedImage canvas = new BufferedImage(60, 60, BufferedImage.TYPE_BYTE_GRAY);
            Graphics2D g = canvas.createGraphics();
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, 60, 60);
            g.setColor(Color.BLACK);
            g.setStroke(new java.awt.BasicStroke(sw, java.awt.BasicStroke.CAP_ROUND, java.awt.BasicStroke.JOIN_ROUND));
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            switch (ch) {
                case 'I' -> {
                    // Vertical line with top and bottom serif lines
                    g.drawLine(30, 12, 30, 48);
                    g.drawLine(20, 12, 40, 12);
                    g.drawLine(20, 48, 40, 48);
                }
                case 'V' -> {
                    // Downward left stroke meeting upward right stroke
                    g.drawLine(15, 14, 30, 46);
                    g.drawLine(30, 46, 45, 14);
                }
                case 'X' -> {
                    // Crossing diagonal lines
                    g.drawLine(16, 14, 44, 46);
                    g.drawLine(44, 14, 16, 46);
                }
                case 'L' -> {
                    // Left vertical line + bottom horizontal line
                    g.drawLine(18, 14, 18, 46);
                    g.drawLine(18, 46, 44, 46);
                }
                case 'C' -> {
                    // Curved arc open on right
                    g.drawArc(16, 14, 30, 32, 45, 270);
                }
                case 'D' -> {
                    // Left spine + right semicircle
                    g.drawLine(18, 14, 18, 46);
                    g.drawArc(8, 14, 36, 32, 270, 180);
                }
                case 'M' -> {
                    // Left line, diagonal down, diagonal up, right line
                    g.drawLine(14, 46, 14, 14);
                    g.drawLine(14, 14, 30, 36);
                    g.drawLine(30, 36, 46, 14);
                    g.drawLine(46, 14, 46, 46);
                }
            }
            g.dispose();
            BufferedImage preprocessed = ImagePreprocessor.preprocess(canvas);
            PROTOTYPES.add(new Prototype(ch, extractVector(preprocessed)));
        }
    }

    /**
     * Classifies a 28x28 preprocessed image into a Roman character result.
     */
    public static RomanCharResult classify(BufferedImage image) {
        double[] query = extractVector(image);
        Map<Character, Double> scores = new HashMap<>();
        for (char c : ROMAN_CHARS) {
            scores.put(c, 0.0);
        }

        // 1. Template correlation (cosine similarity across top prototypes)
        Map<Character, List<Double>> charSimilarities = new HashMap<>();
        for (char c : ROMAN_CHARS) {
            charSimilarities.put(c, new ArrayList<>());
        }

        for (Prototype proto : PROTOTYPES) {
            double sim = cosineSimilarity(query, proto.features);
            charSimilarities.get(proto.label).add(sim);
        }

        for (char c : ROMAN_CHARS) {
            List<Double> sims = charSimilarities.get(c);
            sims.sort((a, b) -> Double.compare(b, a));
            // Average top 5 best prototype matches
            double avgTop = 0.0;
            int count = Math.min(5, sims.size());
            for (int i = 0; i < count; i++) {
                avgTop += sims.get(i);
            }
            scores.put(c, avgTop / count);
        }

        // 2. Structural & Topological Feature Boosters
        applyStructuralFeatures(query, scores);

        // Find best match and runner-ups
        char bestChar = 'I';
        double bestScore = -1.0;
        for (Map.Entry<Character, Double> entry : scores.entrySet()) {
            if (entry.getValue() > bestScore) {
                bestScore = entry.getValue();
                bestChar = entry.getKey();
            }
        }

        // Normalize confidence to [0.0, 1.0]
        double confidence = Math.min(0.99, Math.max(0.40, bestScore));

        return new RomanCharResult(bestChar, confidence, scores);
    }

    private static void applyStructuralFeatures(double[] v, Map<Character, Double> scores) {
        // v is 28x28 normalized grayscale where 1.0 is black ink, 0.0 is white background
        // Analyze upper, middle, and lower regions
        double topInk = 0.0, midInk = 0.0, botInk = 0.0;
        double leftInk = 0.0, centerInk = 0.0, rightInk = 0.0;

        for (int y = 0; y < 28; y++) {
            for (int x = 0; x < 28; x++) {
                double val = v[y * 28 + x];
                if (y < 10) topInk += val;
                else if (y < 19) midInk += val;
                else botInk += val;

                if (x < 10) leftInk += val;
                else if (x < 19) centerInk += val;
                else rightInk += val;
            }
        }

        // Feature A: Center crossing for 'X'
        // In 'X', center (y in [11,17], x in [11,17]) has strong intersection
        double centerCrossInk = 0.0;
        for (int y = 11; y <= 16; y++) {
            for (int x = 11; x <= 16; x++) {
                centerCrossInk += v[y * 28 + x];
            }
        }
        // In X, top has 2 peaks (left and right), bottom has 2 peaks
        double topLeft = 0, topRight = 0, botLeft = 0, botRight = 0;
        for (int y = 3; y <= 9; y++) {
            for (int x = 3; x <= 10; x++) topLeft += v[y * 28 + x];
            for (int x = 18; x <= 25; x++) topRight += v[y * 28 + x];
        }
        for (int y = 19; y <= 25; y++) {
            for (int x = 3; x <= 10; x++) botLeft += v[y * 28 + x];
            for (int x = 18; x <= 25; x++) botRight += v[y * 28 + x];
        }

        boolean hasFourCorners = (topLeft > 1.5 && topRight > 1.5 && botLeft > 1.5 && botRight > 1.5);
        if (hasFourCorners && centerCrossInk > 2.0) {
            scores.put('X', scores.get('X') + 0.35);
        }

        // Feature B: 'V' has wide top and single bottom vertex
        boolean vTopWide = (topLeft > 1.5 && topRight > 1.5);
        boolean vBottomNarrow = (botInk > 2.0 && botLeft < 1.0 && botRight < 1.0);
        if (vTopWide && vBottomNarrow) {
            scores.put('V', scores.get('V') + 0.40);
        }

        // Feature C: 'I' is predominantly centered vertically with very little left/right ink
        if (centerInk > (leftInk + rightInk) * 1.5 && centerInk > 5.0) {
            scores.put('I', scores.get('I') + 0.35);
        }

        // Feature D: 'L' has heavy left spine and bottom base, top-right is empty
        if (leftInk > 3.0 && botInk > 3.0 && topRight < 0.5) {
            scores.put('L', scores.get('L') + 0.35);
        }

        // Feature E: 'C' has left spine, top and bottom curve, but center-right is open
        double centerRight = 0.0;
        for (int y = 11; y <= 17; y++) {
            for (int x = 18; x <= 26; x++) centerRight += v[y * 28 + x];
        }
        if (leftInk > 3.0 && topInk > 2.0 && botInk > 2.0 && centerRight < 0.8) {
            scores.put('C', scores.get('C') + 0.30);
        }

        // Feature F: 'M' vs 'D'
        // 'M' has two tall vertical pillars on left and right, with a V-dip in the top-center
        // 'D' has a vertical spine on left, a closed curved loop, and a central hole surrounded by top/bottom ink
        double topCenter = 0.0, botCenter = 0.0, midHole = 0.0;
        for (int x = 11; x <= 16; x++) {
            for (int y = 3; y <= 8; y++) topCenter += v[y * 28 + x];
            for (int y = 11; y <= 16; y++) midHole += v[y * 28 + x];
            for (int y = 19; y <= 25; y++) botCenter += v[y * 28 + x];
        }

        if (leftInk > 4.0 && rightInk > 4.0) {
            // If top-center and bottom-center are both closed around a middle hole -> 'D'
            if (topCenter > 1.2 && botCenter > 1.2 && midHole < topCenter) {
                scores.put('D', scores.get('D') + 0.45);
            } else {
                // Otherwise two tall pillars with dip -> 'M'
                scores.put('M', scores.get('M') + 0.35);
            }
        }
    }

    public static double[] extractVector(BufferedImage img) {
        double[] f = new double[784];
        for (int y = 0; y < 28; y++) {
            for (int x = 0; x < 28; x++) {
                int rgb = img.getRGB(x, y);
                int r = (rgb >> 16) & 0xff;
                int g = (rgb >> 8) & 0xff;
                int b = rgb & 0xff;
                double gray = (r + g + b) / (255.0 * 3.0);
                f[y * 28 + x] = 1.0 - gray; // Ink is 1.0, background is 0.0
            }
        }
        return f;
    }

    private static double cosineSimilarity(double[] a, double[] b) {
        double dot = 0.0, normA = 0.0, normB = 0.0;
        for (int i = 0; i < a.length; i++) {
            dot += a[i] * b[i];
            normA += a[i] * a[i];
            normB += b[i] * b[i];
        }
        if (normA == 0.0 || normB == 0.0) return 0.0;
        return dot / (Math.sqrt(normA) * Math.sqrt(normB));
    }

    private record Prototype(char label, double[] features) {}

    public record RomanCharResult(char character, double confidence, Map<Character, Double> scores) {
        public int value() {
            return switch (character) {
                case 'I' -> 1;
                case 'V' -> 5;
                case 'X' -> 10;
                case 'L' -> 50;
                case 'C' -> 100;
                case 'D' -> 500;
                case 'M' -> 1000;
                default -> 0;
            };
        }
    }
}
