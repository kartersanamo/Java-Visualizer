package com.kartersanamo.visualizer.Sorting;

import javax.swing.*;
import java.awt.*;
import java.util.Collections;
import java.util.Map;

public class SortingVisualizerPanel extends JPanel {
    private int[] array;
    private Map<Integer, Color> highlights = Collections.emptyMap();

    public void setArray(int[] array) {
        this.array = array;
        repaint();
    }

    public void setHighlights(Map<Integer, Color> highlights) {
        this.highlights = highlights != null ? highlights : Collections.emptyMap();
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (array == null) return;
        int width = getWidth();
        int height = getHeight();
        int barWidth = Math.max(1, width / array.length);
        int max = 1;
        for (int v : array) max = Math.max(max, v);
        for (int i = 0; i < array.length; i++) {
            int barHeight = (int) ((array[i] / (double) max) * (height - 20));
            Color color = highlights.getOrDefault(i, Color.BLUE);
            g.setColor(color);
            g.fillRect(i * barWidth, height - barHeight, barWidth, barHeight);
        }
    }
}