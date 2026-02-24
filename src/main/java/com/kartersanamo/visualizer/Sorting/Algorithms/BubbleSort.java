package com.kartersanamo.visualizer.Sorting.Algorithms;

import com.kartersanamo.visualizer.Sorting.SortingAlgorithm;
import com.kartersanamo.visualizer.Sorting.SortingStats;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class BubbleSort implements SortingAlgorithm {
    private int[] array;
    private Map<Integer, Color> highlights;
    private final Map<Integer, Color> permHighlights = new HashMap<>();
    private int outerIndex;
    private int innerIndex;
    private boolean isSorted;
    private SortingStats stats;

    @Override
    public void setArray(int[] array) {
        this.array = array;
        reset();
    }

    public void setStats(SortingStats stats) {
        this.stats = stats;
    }

    @Override
    public void step() {
        if (isSorted || array == null) return;
        highlights = new HashMap<>();
        if (array.length == 1) {
            permHighlights.put(0, DARK_GREEN);
            isSorted = true;
            return;
        }
        if (array.length == 2 && innerIndex == 0) {
            highlights.put(0, ORANGE);
            highlights.put(1, ORANGE);
            if (stats != null) stats.comparisons++;
            if (array[0] > array[1]) {
                int temp = array[0];
                array[0] = array[1];
                array[1] = temp;
                highlights.put(0, SWAP_COLOR);
                highlights.put(1, SWAP_COLOR);
                if (stats != null) stats.swaps++;
            }
            permHighlights.put(1, DARK_GREEN);
            permHighlights.put(0, DARK_GREEN);
            isSorted = true;
            return;
        }
        if (innerIndex < array.length - outerIndex - 1) {
            boolean didSwap = false;
            if (stats != null) stats.comparisons++;
            if (array[innerIndex] > array[innerIndex + 1]) {
                int temp = array[innerIndex];
                array[innerIndex] = array[innerIndex + 1];
                array[innerIndex + 1] = temp;
                didSwap = true;
                if (stats != null) stats.swaps++;
            }
            if (didSwap) {
                highlights.put(innerIndex, SWAP_COLOR);
                highlights.put(innerIndex + 1, SWAP_COLOR);
            } else {
                highlights.put(innerIndex, ORANGE);
                highlights.put(innerIndex + 1, ORANGE);
            }
            innerIndex++;
        } else {
            permHighlights.put(array.length - outerIndex - 1, DARK_GREEN);
            innerIndex = 0;
            outerIndex++;
            if (outerIndex >= array.length - 1) {
                isSorted = true;
                for (int i = 0; i < array.length; i++) {
                    permHighlights.put(i, DARK_GREEN);
                }
            }
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
        isSorted = false;
        highlights = null;
        permHighlights.clear();
    }

    @Override
    public String getName() {
        return "Bubble Sort";
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