package com.kartersanamo.visualizer.Sorting.Algorithms;

import com.kartersanamo.visualizer.Sorting.SortingAlgorithm;
import com.kartersanamo.visualizer.Sorting.SortingStats;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class SelectionSort implements SortingAlgorithm {
    private int[] array;
    private Map<Integer, Color> highlights;
    private final Map<Integer, Color> permHighlights = new HashMap<>();
    private int outerIndex;
    private int innerIndex;
    private int minIndex;
    private boolean isSorted;
    private static final Color ORANGE = new Color(255, 165, 0);
    private static final Color SWAP_COLOR = Color.CYAN;
    private static final Color SORTED_COLOR = new Color(0, 128, 0);
    private com.kartersanamo.visualizer.Sorting.SortingStats stats;

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
        if (outerIndex >= array.length - 1) {
            // Mark all as sorted
            for (int i = 0; i < array.length; i++) permHighlights.put(i, SORTED_COLOR);
            isSorted = true;
            return;
        }
        // If just starting a new pass
        if (innerIndex == 0) {
            minIndex = outerIndex;
            innerIndex = outerIndex + 1;
        }
        // Highlight current comparison
        highlights.put(outerIndex, ORANGE);
        highlights.put(innerIndex, ORANGE);
        highlights.put(minIndex, SWAP_COLOR);
        if (stats != null) stats.comparisons++;
        // Compare
        if (array[innerIndex] < array[minIndex]) {
            minIndex = innerIndex;
        }
        innerIndex++;
        // If finished inner loop, do the swap
        if (innerIndex >= array.length) {
            if (minIndex != outerIndex) {
                int temp = array[outerIndex];
                array[outerIndex] = array[minIndex];
                array[minIndex] = temp;
                if (stats != null) stats.swaps++;
            }
            permHighlights.put(outerIndex, SORTED_COLOR);
            outerIndex++;
            innerIndex = 0;
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
        minIndex = 0;
        isSorted = false;
        highlights = null;
        permHighlights.clear();
    }

    @Override
    public String getName() {
        return "Selection Sort";
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