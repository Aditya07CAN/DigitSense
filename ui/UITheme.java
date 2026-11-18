package com.digitsense.ui;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

/**
 * Modern design system and color palette for DigitSense UI.
 */
public final class UITheme {

    // ── Modern Dark / Vibrant Theme Palette ──
    public static final Color BG_MAIN       = new Color(15, 23, 42);     // Deep midnight slate
    public static final Color BG_SURFACE    = new Color(30, 41, 59);     // Slate 800
    public static final Color BG_CARD       = new Color(24, 33, 47);     // Slightly darker slate
    public static final Color BG_CARD_LIGHT = new Color(39, 52, 73);     // Lighter card surface
    public static final Color BG_INPUT      = new Color(45, 59, 80);     // Input field background
    public static final Color BORDER        = new Color(71, 85, 105);    // Slate 700 border
    public static final Color BORDER_LIGHT  = new Color(100, 116, 139);  // Slate 600 border

    // ── Typography Colors ──
    public static final Color TEXT_MAIN     = new Color(248, 250, 252);  // Crisp white
    public static final Color TEXT_MUTED    = new Color(148, 163, 184);  // Cool grey
    public static final Color TEXT_FAINT    = new Color(100, 116, 139);  // Subtle grey

    // ── Vibrant Accent Colors ──
    public static final Color PRIMARY       = new Color(99, 102, 241);   // Electric Indigo
    public static final Color PRIMARY_HOVER = new Color(129, 140, 248);  // Indigo hover
    public static final Color SUCCESS       = new Color(16, 185, 129);   // Emerald Green
    public static final Color SUCCESS_HOVER = new Color(52, 211, 153);
    public static final Color DANGER        = new Color(239, 68, 68);    // Crimson / Rose
    public static final Color DANGER_HOVER  = new Color(248, 113, 113);
    public static final Color WARNING       = new Color(245, 158, 11);   // Vibrant Amber
    public static final Color WARNING_HOVER = new Color(251, 191, 36);
    public static final Color INFO          = new Color(59, 130, 246);   // Sky Blue
    public static final Color PURPLE        = new Color(139, 92, 246);   // Cyber Purple
    public static final Color CYAN          = new Color(6, 182, 212);    // Neon Cyan
    public static final Color PINK          = new Color(236, 72, 153);   // Bright Pink

    // ── Typography ──
    public static final Font FONT_BRAND  = new Font("Segoe UI", Font.BOLD, 17);
    public static final Font FONT_TITLE  = new Font("Segoe UI", Font.BOLD, 15);
    public static final Font FONT_HEADER = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font FONT_BODY   = new Font("Segoe UI", Font.PLAIN, 12);
    public static final Font FONT_BOLD   = new Font("Segoe UI", Font.BOLD, 12);
    public static final Font FONT_SMALL  = new Font("Segoe UI", Font.PLAIN, 11);
    public static final Font FONT_MONO   = new Font("Consolas", Font.BOLD, 12);

    private UITheme() {}

    /** Creates a rounded modern button with smooth hover effects. */
    public static JButton createButton(String text, Color baseColor, Color hoverColor) {
        JButton button = new JButton(text) {
            private boolean isHovered = false;
            private boolean isPressed = false;

            {
                setContentAreaFilled(false);
                setFocusPainted(false);
                setBorderPainted(false);
                setOpaque(false);
                setFont(FONT_BOLD);
                setForeground(Color.WHITE);
                setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                setMargin(new Insets(6, 14, 6, 14));

                addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseEntered(MouseEvent e) {
                        if (isEnabled()) {
                            isHovered = true;
                            repaint();
                        }
                    }

                    @Override
                    public void mouseExited(MouseEvent e) {
                        isHovered = false;
                        repaint();
                    }

                    @Override
                    public void mousePressed(MouseEvent e) {
                        if (isEnabled()) {
                            isPressed = true;
                            repaint();
                        }
                    }

                    @Override
                    public void mouseReleased(MouseEvent e) {
                        isPressed = false;
                        repaint();
                    }
                });
            }

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                Color currentBg = baseColor;
                if (!isEnabled()) {
                    currentBg = new Color(60, 75, 95);
                    g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.6f));
                } else if (isPressed) {
                    currentBg = baseColor.darker();
                } else if (isHovered) {
                    currentBg = hoverColor;
                }

                int arc = 8;
                g2.setColor(currentBg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), arc, arc);

                // Subtle top highlight border for depth
                if (isEnabled()) {
                    g2.setColor(new Color(255, 255, 255, 35));
                    g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, arc, arc);
                }

                g2.dispose();
                super.paintComponent(g);
            }
        };
        return button;
    }

    /** Creates a rounded badge chip label. */
    public static JLabel createBadge(String text, Color bgColor, Color textColor) {
        JLabel badge = new JLabel(text, JLabel.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(bgColor);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        badge.setFont(FONT_BOLD);
        badge.setForeground(textColor);
        badge.setBorder(new EmptyBorder(3, 10, 3, 10));
        return badge;
    }

    /** Creates a rounded container card panel. */
    public static JPanel createCard() {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BG_SURFACE);
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
                g2.setColor(BORDER);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        card.setOpaque(false);
        return card;
    }
}
