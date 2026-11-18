package com.digitsense.ui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import javax.swing.JPanel;

/**
 * Modern double-buffered interactive drawing canvas for handwritten digit sketch.
 */
public class DigitCanvas extends JPanel implements MouseMotionListener {

    public static final int DEFAULT_STROKE_WIDTH = 18;

    private BufferedImage drawingImage;
    private Color penColor = Color.BLACK;
    private int strokeWidth = DEFAULT_STROKE_WIDTH;
    private int previousX = -1;
    private int previousY = -1;
    private boolean showGrid = false;
    private boolean isEraser = false;

    public DigitCanvas(int width, int height) {
        setDoubleBuffered(true);
        setBackground(Color.WHITE);
        setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
        setPreferredSize(new Dimension(width, height));
        drawingImage = new BufferedImage(Math.max(1, width), Math.max(1, height), BufferedImage.TYPE_INT_ARGB);
        clear();

        addMouseMotionListener(this);
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                previousX = e.getX();
                previousY = e.getY();
                drawStroke(e.getX(), e.getY());
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                previousX = -1;
                previousY = -1;
            }
        });

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent event) {
                resizeDrawingImage(getWidth(), getHeight());
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        resizeDrawingImage(getWidth(), getHeight());

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        // Draw the white paper canvas
        g2.drawImage(drawingImage, 0, 0, this);

        // Optional subtle guide grid
        if (showGrid) {
            g2.setColor(new Color(220, 225, 235, 120));
            int step = 40;
            for (int x = step; x < getWidth(); x += step) {
                g2.drawLine(x, 0, x, getHeight());
            }
            for (int y = step; y < getHeight(); y += step) {
                g2.drawLine(0, y, getWidth(), y);
            }

            // Center crosshair
            g2.setColor(new Color(180, 195, 220, 160));
            g2.drawLine(getWidth() / 2, 0, getWidth() / 2, getHeight());
            g2.drawLine(0, getHeight() / 2, getWidth(), getHeight() / 2);
        }

        // Clean border
        g2.setColor(new Color(180, 195, 215));
        g2.drawRect(0, 0, getWidth() - 1, getHeight() - 1);
        g2.dispose();
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
        if (drawingImage == null) return;
        Graphics2D graphics = drawingImage.createGraphics();
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

        Color drawColor = isEraser ? Color.WHITE : penColor;
        graphics.setColor(drawColor);
        int currentStroke = isEraser ? strokeWidth * 2 : strokeWidth;
        graphics.setStroke(new BasicStroke(currentStroke, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

        if (previousX >= 0 && previousY >= 0) {
            graphics.drawLine(previousX, previousY, x, y);
        } else {
            graphics.fillOval(x - currentStroke / 2, y - currentStroke / 2, currentStroke, currentStroke);
        }

        graphics.dispose();
        previousX = x;
        previousY = y;
        repaint();
    }

    public void clear() {
        if (drawingImage == null || getWidth() <= 0 || getHeight() <= 0) {
            int w = Math.max(1, getWidth() > 0 ? getWidth() : (int) getPreferredSize().getWidth());
            int h = Math.max(1, getHeight() > 0 ? getHeight() : (int) getPreferredSize().getHeight());
            drawingImage = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        }
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
        graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        double scale = Math.min((double) drawingImage.getWidth() / source.getWidth(),
                (double) drawingImage.getHeight() / source.getHeight());
        int width = Math.max(1, (int) Math.round(source.getWidth() * scale));
        int height = Math.max(1, (int) Math.round(source.getHeight() * scale));
        int x = (drawingImage.getWidth() - width) / 2;
        int y = (drawingImage.getHeight() - height) / 2;
        graphics.drawImage(source.getScaledInstance(width, height, Image.SCALE_SMOOTH), x, y, null);
        graphics.dispose();
        previousX = -1;
        previousY = -1;
        repaint();
    }

    public void setPenColor(Color penColor) {
        this.penColor = (penColor == null) ? Color.BLACK : penColor;
        this.isEraser = false;
    }

    public Color getPenColor() {
        return penColor;
    }

    public void setEraser(boolean eraser) {
        this.isEraser = eraser;
    }

    public boolean isEraser() {
        return isEraser;
    }

    public void setStrokeWidth(int strokeWidth) {
        this.strokeWidth = Math.max(4, Math.min(60, strokeWidth));
    }

    public int getStrokeWidth() {
        return strokeWidth;
    }

    public void setShowGrid(boolean showGrid) {
        this.showGrid = showGrid;
        repaint();
    }

    public boolean isShowGrid() {
        return showGrid;
    }

    private void resizeDrawingImage(int width, int height) {
        if (width <= 0 || height <= 0 || (drawingImage != null && drawingImage.getWidth() == width
                && drawingImage.getHeight() == height)) {
            return;
        }

        BufferedImage resized = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = resized.createGraphics();
        graphics.setColor(Color.WHITE);
        graphics.fillRect(0, 0, width, height);
        if (drawingImage != null) {
            graphics.drawImage(drawingImage, 0, 0, null);
        }
        graphics.dispose();
        drawingImage = resized;
    }
}
