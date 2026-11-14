package com.digitsense.ui;

import org.junit.jupiter.api.Test;
import java.awt.Color;
import java.awt.GraphicsEnvironment;

import static org.junit.jupiter.api.Assertions.*;

class UiComponentsTest {

    @Test
    void testThemeColorsNotNull() {
        assertNotNull(UITheme.BG_MAIN);
        assertNotNull(UITheme.BG_SURFACE);
        assertNotNull(UITheme.PRIMARY);
        assertNotNull(UITheme.SUCCESS);
        assertNotNull(UITheme.DANGER);
        assertNotNull(UITheme.WARNING);
        assertNotNull(UITheme.CYAN);
    }

    @Test
    void testDigitCanvasLifecycle() {
        if (GraphicsEnvironment.isHeadless()) {
            return;
        }
        DigitCanvas canvas = new DigitCanvas(300, 300);
        assertNotNull(canvas.getDrawingImage());
        assertEquals(Color.BLACK, canvas.getPenColor());

        canvas.setPenColor(Color.BLUE);
        assertEquals(Color.BLUE, canvas.getPenColor());

        canvas.setStrokeWidth(24);
        assertEquals(24, canvas.getStrokeWidth());

        canvas.setShowGrid(true);
        assertTrue(canvas.isShowGrid());

        canvas.clear();
        assertNotNull(canvas.getDrawingImage());
    }

    @Test
    void testQuickPredictPanelCreation() {
        if (GraphicsEnvironment.isHeadless()) {
            return;
        }
        QuickPredictPanel panel = new QuickPredictPanel();
        assertNotNull(panel);
        assertNotNull(panel.getMiniCanvas());
    }
}
