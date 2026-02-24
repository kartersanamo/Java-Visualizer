package com.kartersanamo.visualizer.Sorting;

import com.kartersanamo.visualizer.Sorting.Algorithms.BubbleSort;
import com.kartersanamo.visualizer.Sorting.Algorithms.InsertionSort;
import com.kartersanamo.visualizer.Sorting.Algorithms.SelectionSort;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

public class SortingAlgorithmFactory {
    private static final Map<String, Supplier<SortingAlgorithm>> algorithms = new LinkedHashMap<>();

    static {
        algorithms.put("Bubble Sort", BubbleSort::new);
        algorithms.put("Selection Sort", SelectionSort::new);
        algorithms.put("Insertion Sort", InsertionSort::new);
    }

    public static Map<String, Supplier<SortingAlgorithm>> getAlgorithms() {
        return algorithms;
    }
}
