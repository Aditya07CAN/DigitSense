package com.digitsense.app;

import com.digitsense.ui.DigitSenseFrame;

import java.awt.EventQueue;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.concurrent.CountDownLatch;

/** Starts the DigitSense desktop application and keeps the main thread alive. */
public class Main {

    public static void main(String[] args) {
        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
            System.err.println("Uncaught exception in thread " + thread.getName() + ":");
            throwable.printStackTrace();
        });

        CountDownLatch latch = new CountDownLatch(1);

        EventQueue.invokeLater(() -> {
            try {
                DigitSenseFrame frame = new DigitSenseFrame();
                frame.addWindowListener(new WindowAdapter() {
                    @Override
                    public void windowClosing(WindowEvent e) {
                        latch.countDown();
                    }

                    @Override
                    public void windowClosed(WindowEvent e) {
                        latch.countDown();
                    }
                });
                frame.setVisible(true);
                System.out.println("DigitSense window is now visible and active!");
            } catch (Throwable t) {
                System.err.println("Failed to initialize DigitSense window:");
                t.printStackTrace();
                latch.countDown();
            }
        });

        try {
            latch.await();
        } catch (InterruptedException ignored) {
        }
    }
}
