package com.kartersanamo.visualizer.Sorting;

import java.awt.Color;
import java.util.Map;

public interface SortingAlgorithm {
    Color ORANGE = new Color(255, 165, 0);
    Color LIGHT_GREEN = new Color(144, 238, 144);
    Color DARK_GREEN = new Color(0, 128, 0);
    Color SWAP_COLOR = Color.CYAN;

    void setArray(int[] array);
    void step(); // Perform one step of the algorithm
    boolean isSorted();
    int[] getArray();
    void reset();
    String getName();
    // New: Return a map of index to color for highlighting
    Map<Integer, Color> getHighlights();
    String getTimeComplexity();
    String getSpaceComplexity();
}