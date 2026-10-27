package com.digitsense.app;

import com.digitsense.ui.DigitSenseFrame;

import java.awt.EventQueue;

/** Starts the DigitSense desktop application. */
public class Main {

    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            DigitSenseFrame frame = new DigitSenseFrame();
            frame.setVisible(true);
        });
    }
}

