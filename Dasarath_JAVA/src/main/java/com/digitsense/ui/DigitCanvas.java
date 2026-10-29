package com.digitsense.ui;

import java.awt.BasicStroke;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

/** Drawing area where the user writes a digit with the mouse. */
public class DigitCanvas extends Canvas implements MouseMotionListener {

    private static final int STROKE_WIDTH = 18;

    private BufferedImage drawingImage;
    private Color penColor = Color.BLACK;
    private int previousX = -1;
    private int previousY = -1;

    public DigitCanvas(int width, int height) {
        setBackground(Color.WHITE);
        drawingImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        clear();
        addMouseMotionListener(this);
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent event) {
                resizeDrawingImage(getWidth(), getHeight());
            }
        });
    }

    @Override
    public void paint(Graphics graphics) {
        resizeDrawingImage(getWidth(), getHeight());
        graphics.drawImage(drawingImage, 0, 0, this);
    }

    @Override
    public void update(Graphics graphics) {
        paint(graphics);
    }

    @Override
    public void mouseDragged(MouseEvent event) {
        drawStroke(event.getX(), event.getY());
    }

    @Override
    public void mouseMoved(MouseEvent event) {
        previousX = -1;
        previousY = -1;
    }

    private void drawStroke(int x, int y) {
        Graphics2D graphics = drawingImage.createGraphics();
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        graphics.setColor(penColor);
        graphics.setStroke(new BasicStroke(STROKE_WIDTH, BasicStroke.CAP_ROUND,
                BasicStroke.JOIN_ROUND));

        if (previousX >= 0 && previousY >= 0) {
            graphics.drawLine(previousX, previousY, x, y);
        } else {
            graphics.fillOval(x - STROKE_WIDTH / 2, y - STROKE_WIDTH / 2,
                    STROKE_WIDTH, STROKE_WIDTH);
        }

        graphics.dispose();
        previousX = x;
        previousY = y;
        repaint();
    }

    public void clear() {
        resizeDrawingImage(getWidth(), getHeight());
        Graphics2D graphics = drawingImage.createGraphics();
        graphics.setColor(Color.WHITE);
        graphics.fillRect(0, 0, drawingImage.getWidth(), drawingImage.getHeight());
        graphics.dispose();
        previousX = -1;
        previousY = -1;
        repaint();
    }

    public BufferedImage getDrawingImage() {
        return drawingImage;
    }

    /** Replaces the drawing area with an imported image fitted to the canvas. */
    public void loadImage(BufferedImage source) {
        if (source == null) {
            return;
        }
        resizeDrawingImage(getWidth(), getHeight());
        Graphics2D graphics = drawingImage.createGraphics();
        graphics.setColor(Color.WHITE);
        graphics.fillRect(0, 0, drawingImage.getWidth(), drawingImage.getHeight());
        graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        double scale = Math.min((double) drawingImage.getWidth() / source.getWidth(),
                (double) drawingImage.getHeight() / source.getHeight());
        int width = Math.max(1, (int) Math.round(source.getWidth() * scale));
        int height = Math.max(1, (int) Math.round(source.getHeight() * scale));
        int x = (drawingImage.getWidth() - width) / 2;
        int y = (drawingImage.getHeight() - height) / 2;
        graphics.drawImage(source.getScaledInstance(width, height, Image.SCALE_SMOOTH),
                x, y, null);
        graphics.dispose();
        previousX = -1;
        previousY = -1;
        repaint();
    }

    public void setPenColor(Color penColor) {
        this.penColor = penColor == null ? Color.BLACK : penColor;
    }

    private void resizeDrawingImage(int width, int height) {
        if (width <= 0 || height <= 0 || drawingImage.getWidth() == width
                && drawingImage.getHeight() == height) {
            return;
        }

        BufferedImage resized = new BufferedImage(width, height,
                BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = resized.createGraphics();
        graphics.setColor(Color.WHITE);
        graphics.fillRect(0, 0, width, height);
        graphics.drawImage(drawingImage, 0, 0, null);
        graphics.dispose();
        drawingImage = resized;
    }
}

