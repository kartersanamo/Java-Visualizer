package com.kartersanamo.visualizer.Sorting.Algorithms;

import com.kartersanamo.visualizer.Sorting.SortingAlgorithm;
import com.kartersanamo.visualizer.Sorting.SortingStats;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class InsertionSort implements SortingAlgorithm {
    private int[] array;
    private Map<Integer, Color> highlights;
    private final Map<Integer, Color> permHighlights = new HashMap<>();
    private int outerIndex;
    private int innerIndex;
    private boolean isSorted;
    private int slidingIndex = -1;

    private com.kartersanamo.visualizer.Sorting.SortingStats stats;

    private static final Color ORANGE = new Color(255, 165, 0);
    private static final Color SWAP_COLOR = Color.CYAN;
    private static final Color SORTED_COLOR = new Color(0, 128, 0);
    private static final Color MAGENTA = Color.MAGENTA;

    @Override
    public void setArray(int[] array) {
        this.array = array;
        reset();
    }

    public void setStats(com.kartersanamo.visualizer.Sorting.SortingStats stats) {
        this.stats = stats;
    }

    @Override
    public void step() {
        if (isSorted || array == null || array.length < 2) return;
        highlights = new HashMap<>();
        if (outerIndex >= array.length) {
            for (int i = 0; i < array.length; i++) permHighlights.put(i, SORTED_COLOR);
            isSorted = true;
            slidingIndex = -1;
            return;
        }
        // If just starting a new outer pass
        if (innerIndex == 0) {
            innerIndex = outerIndex;
            slidingIndex = outerIndex;
        }
        // Highlight the sliding element
        if (slidingIndex >= 0) highlights.put(slidingIndex, ORANGE);
        // Highlight the compared element
        if (innerIndex > 0) highlights.put(innerIndex - 1, ORANGE);
        if (stats != null && innerIndex > 0) stats.comparisons++;
        // Compare and swap if needed
        if (innerIndex > 0 && array[innerIndex] < array[innerIndex - 1]) {
            int temp = array[innerIndex];
            array[innerIndex] = array[innerIndex - 1];
            array[innerIndex - 1] = temp;
            if (stats != null) stats.swaps++;
            innerIndex--;
            slidingIndex--;
        } else {
            permHighlights.put(outerIndex, SORTED_COLOR);
            outerIndex++;
            innerIndex = 0;
            slidingIndex = -1;
        }
    }

    @Override
    public boolean isSorted() {
        return isSorted;
    }

    @Override
    public int[] getArray() {
        return array;
    }

    @Override
    public void reset() {
        outerIndex = 0;
        innerIndex = 0;
        slidingIndex = -1;
        isSorted = false;
        highlights = null;
        permHighlights.clear();
    }

    @Override
    public String getName() {
        return "Insertion Sort";
    }

    @Override
    public Map<Integer, Color> getHighlights() {
        Map<Integer, Color> out = new HashMap<>();
        if (highlights != null) out.putAll(this.highlights);
        out.putAll(this.permHighlights);
        return out;
    }

    @Override
    public String getTimeComplexity() {
        return "O(n^2)";
    }

    @Override
    public String getSpaceComplexity() {
        return "O(1)";
    }
}