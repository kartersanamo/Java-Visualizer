package com.kartersanamo.visualizer;

import com.kartersanamo.visualizer.Sorting.SortingAlgorithm;
import com.kartersanamo.visualizer.Sorting.SortingAlgorithmFactory;
import com.kartersanamo.visualizer.Sorting.SortingVisualizerPanel;
import com.kartersanamo.visualizer.Sorting.SortingStats;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Map;
import java.util.function.Supplier;

public class Visualizer {
    private enum AlgorithmState { NOT_STARTED, RUNNING, PAUSED, STOPPED, FINISHED }
    private AlgorithmState state = AlgorithmState.NOT_STARTED;
    private JFrame frame;
    private JComboBox<String> algorithmComboBox;
    private SortingVisualizerPanel visualizerPanel;
    private JButton startPauseButton, resetButton, randomizeButton, stepButton;
    private JSlider speedSlider;
    private SortingAlgorithm currentAlgorithm;
    private Timer timer;
    private int[] array;
    private static final int ARRAY_SIZE = 50;
    private boolean isPaused = false;
    private int timerDelay = 50; // Default delay in ms

    // Statistics fields
    private JLabel comparisonsLabel, stepsLabel, swapsLabel, timeComplexityLabel, spaceComplexityLabel;
    private JLabel timeElapsedLabel;
    private long lastUpdateTime = 0;
    private long elapsedSimulatedTime = 0;
    private double currentSpeedMultiplier = 1.0;
    private Timer stopwatchTimer;
    private SortingStats stats;

    public Visualizer() {
        frame = new JFrame("Sorting Algorithm Visualizer");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        // Top panel: algorithm selection
        JPanel topPanel = new JPanel();
        algorithmComboBox = new JComboBox<>();
        for (String name : SortingAlgorithmFactory.getAlgorithms().keySet()) {
            algorithmComboBox.addItem(name);
        }
        topPanel.add(new JLabel("Algorithm:"));
        topPanel.add(algorithmComboBox);

        // Info panel for statistics
        JPanel infoPanel = new JPanel(new GridLayout(3, 3));
        comparisonsLabel = new JLabel("Comparisons: 0");
        stepsLabel = new JLabel("Steps: 0");
        swapsLabel = new JLabel("Swaps: 0");
        timeComplexityLabel = new JLabel("Time Complexity: ");
        spaceComplexityLabel = new JLabel("Space Complexity: ");
        timeElapsedLabel = new JLabel("Time: 00:00.000");
        infoPanel.add(comparisonsLabel);
        infoPanel.add(stepsLabel);
        infoPanel.add(swapsLabel);
        infoPanel.add(timeComplexityLabel);
        infoPanel.add(spaceComplexityLabel);
        infoPanel.add(timeElapsedLabel);

        // Use a vertical BoxLayout for the center region
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.add(topPanel);
        centerPanel.add(infoPanel);

        visualizerPanel = new SortingVisualizerPanel();
        visualizerPanel.setPreferredSize(new Dimension(800, 400));
        centerPanel.add(visualizerPanel);
        frame.add(centerPanel, BorderLayout.CENTER);

        // Bottom panel: controls
        JPanel bottomPanel = new JPanel();
        startPauseButton = new JButton("Start");
        resetButton = new JButton("Reset");
        randomizeButton = new JButton("Randomize Data");
        stepButton = new JButton("Step");
        speedSlider = new JSlider(JSlider.HORIZONTAL, 5, 100, 10); // 0.5x to 10x, default 1.0x
        speedSlider.setMajorTickSpacing(15);
        speedSlider.setMinorTickSpacing(5);
        speedSlider.setPaintTicks(true);
        speedSlider.setPaintLabels(true);
        speedSlider.setLabelTable(createSpeedLabelTable());
        bottomPanel.add(startPauseButton);
        bottomPanel.add(resetButton);
        bottomPanel.add(randomizeButton);
        bottomPanel.add(stepButton);
        bottomPanel.add(new JLabel("Speed:"));
        bottomPanel.add(speedSlider);
        frame.add(bottomPanel, BorderLayout.SOUTH);

        // Initial array and algorithm
        array = generateRandomArray(ARRAY_SIZE);
        visualizerPanel.setArray(array);
        setAlgorithm((String) algorithmComboBox.getSelectedItem());

        updateButtonStates();
        // Listeners
        algorithmComboBox.addActionListener(e -> setAlgorithm((String) algorithmComboBox.getSelectedItem()));
        startPauseButton.addActionListener(this::onStartPauseResume);
        resetButton.addActionListener(this::onReset);
        randomizeButton.addActionListener(e -> {
            array = generateRandomArray(ARRAY_SIZE);
            if (currentAlgorithm != null) {
                currentAlgorithm.setArray(array.clone());
                currentAlgorithm.reset();
            }
            visualizerPanel.setArray(array);
            state = AlgorithmState.NOT_STARTED;
            updateButtonStates();
        });
        stepButton.addActionListener(this::onStep);
        speedSlider.addChangeListener(e -> {
            double newMultiplier = speedSlider.getValue() / 10.0;
            // Before changing speed, update simulated time with old multiplier
            updateSimulatedTime();
            currentSpeedMultiplier = newMultiplier;
            timerDelay = (int)(50 / currentSpeedMultiplier); // 50ms base at 1x
            if (timer != null && timer.isRunning()) {
                timer.setDelay(Math.max(timerDelay, 1));
            }
        });

        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private void updateButtonStates() {
        switch (state) {
            case NOT_STARTED:
                startPauseButton.setEnabled(true);
                startPauseButton.setText("Start");

                resetButton.setEnabled(false);
                randomizeButton.setEnabled(true);
                stepButton.setEnabled(false);
                break;
            case RUNNING:
                startPauseButton.setEnabled(true);
                startPauseButton.setText("Pause");

                resetButton.setEnabled(false);
                randomizeButton.setEnabled(false);
                stepButton.setEnabled(false);
                break;
            case PAUSED:
                startPauseButton.setEnabled(true);
                startPauseButton.setText("Resume");

                resetButton.setEnabled(true);
                randomizeButton.setEnabled(false);
                stepButton.setEnabled(true);
                break;
            case STOPPED:
                startPauseButton.setEnabled(false);
                startPauseButton.setText("Start");

                resetButton.setEnabled(false);
                randomizeButton.setEnabled(true);
                stepButton.setEnabled(false);
                break;
            case FINISHED:
                startPauseButton.setEnabled(false);
                startPauseButton.setText("Start");

                resetButton.setEnabled(true);
                randomizeButton.setEnabled(false);
                stepButton.setEnabled(false);
                break;
        }
    }

    private void setAlgorithm(String name) {
        Map<String, Supplier<SortingAlgorithm>> algos = SortingAlgorithmFactory.getAlgorithms();
        if (algos.containsKey(name)) {
            if (timer != null && timer.isRunning()) timer.stop();
            if (stopwatchTimer != null) stopwatchTimer.stop();
            currentAlgorithm = algos.get(name).get();
            currentAlgorithm.setArray(array.clone());
            stats = new SortingStats();
            // Pass stats to algorithm if supported
            try {
                currentAlgorithm.getClass().getMethod("setStats", SortingStats.class).invoke(currentAlgorithm, stats);
            } catch (Exception ignored) {}
            updateVisualization();
            state = AlgorithmState.NOT_STARTED;
            lastUpdateTime = 0;
            elapsedSimulatedTime = 0;
            currentSpeedMultiplier = speedSlider.getValue() / 10.0;
            timeElapsedLabel.setText("Time: 00:00.000");
            updateButtonStates();
        } else {
            currentAlgorithm = null;
        }
    }

    private java.util.Hashtable<Integer, JLabel> createSpeedLabelTable() {
        java.util.Hashtable<Integer, JLabel> table = new java.util.Hashtable<>();
        table.put(5, new JLabel("0.5x"));
        table.put(100, new JLabel("10x"));
        return table;
    }

    private void updateVisualization() {
        if (currentAlgorithm != null) {
            visualizerPanel.setArray(currentAlgorithm.getArray());
            visualizerPanel.setHighlights(currentAlgorithm.getHighlights());
            updateStatsPanel();
        }
    }

    private void updateStatsPanel() {
        comparisonsLabel.setText("Comparisons: " + stats.comparisons);
        stepsLabel.setText("Steps: " + stats.steps);
        swapsLabel.setText("Swaps: " + stats.swaps);
        if (currentAlgorithm != null) {
            timeComplexityLabel.setText("Time Complexity: " + currentAlgorithm.getTimeComplexity());
            spaceComplexityLabel.setText("Space Complexity: " + currentAlgorithm.getSpaceComplexity());
        } else {
            timeComplexityLabel.setText("Time Complexity: ?");
            spaceComplexityLabel.setText("Space Complexity: ?");
        }
        timeElapsedLabel.setText("Time: " + formatElapsedTime(elapsedSimulatedTime));
    }

    private String formatElapsedTime(long ms) {
        long minutes = ms / 60000;
        long seconds = (ms % 60000) / 1000;
        long millis = ms % 1000;
        return String.format("%02d:%02d.%03d", minutes, seconds, millis);
    }

    private void onStartPauseResume(ActionEvent e) {
        if (state == AlgorithmState.NOT_STARTED) {
            if (currentAlgorithm == null) return;
            isPaused = false;
            state = AlgorithmState.RUNNING;
            lastUpdateTime = System.currentTimeMillis();
            currentSpeedMultiplier = speedSlider.getValue() / 10.0;
            stopwatchTimer = new Timer(30, evt2 -> updateSimulatedTime());
            stopwatchTimer.start();
            timer = new Timer(Math.max(timerDelay, 1), evt -> {
                if (!isPaused && !currentAlgorithm.isSorted()) {
                    updateSimulatedTime();
                    currentAlgorithm.step();
                    stats.steps++;
                    updateVisualization();
                } else if (currentAlgorithm.isSorted()) {
                    timer.stop();
                    if (stopwatchTimer != null) stopwatchTimer.stop();
                    updateSimulatedTime();
                    state = AlgorithmState.FINISHED;
                    updateButtonStates();
                }
            });
            timer.start();
            updateButtonStates();
        } else if (state == AlgorithmState.RUNNING) {
            isPaused = true;
            state = AlgorithmState.PAUSED;
            if (stopwatchTimer != null) stopwatchTimer.stop();
            updateSimulatedTime();
            updateButtonStates();
        } else if (state == AlgorithmState.PAUSED) {
            isPaused = false;
            state = AlgorithmState.RUNNING;
            lastUpdateTime = System.currentTimeMillis();
            stopwatchTimer = new Timer(30, evt2 -> updateSimulatedTime());
            stopwatchTimer.start();
            updateButtonStates();
        }
    }

    private void updateSimulatedTime() {
        long now = System.currentTimeMillis();
        long realElapsed = now - lastUpdateTime;
        elapsedSimulatedTime += (long)(realElapsed * currentSpeedMultiplier);
        lastUpdateTime = now;
        timeElapsedLabel.setText("Time: " + formatElapsedTime(elapsedSimulatedTime));
    }

    private void onReset(ActionEvent e) {
        if (state == AlgorithmState.PAUSED || state == AlgorithmState.FINISHED) {
            if (timer != null && timer.isRunning()) timer.stop();
            if (stopwatchTimer != null) stopwatchTimer.stop();
            array = generateRandomArray(ARRAY_SIZE);
            stats = new SortingStats();
            if (currentAlgorithm != null) {
                // Pass stats to algorithm if supported
                try {
                    currentAlgorithm.getClass().getMethod("setStats", SortingStats.class).invoke(currentAlgorithm, stats);
                } catch (Exception ignored) {}
                currentAlgorithm.setArray(array.clone());
                currentAlgorithm.reset();
            }
            updateVisualization();
            state = AlgorithmState.NOT_STARTED;
            lastUpdateTime = 0;
            elapsedSimulatedTime = 0;
            currentSpeedMultiplier = speedSlider.getValue() / 10.0;
            timeElapsedLabel.setText("Time: 00:00.000");
            updateButtonStates();
        }
    }

    private void onStep(ActionEvent e) {
        if (state == AlgorithmState.PAUSED && currentAlgorithm != null && !currentAlgorithm.isSorted()) {
            updateSimulatedTime();
            currentAlgorithm.step();
            stats.steps++;
            updateVisualization();
            if (currentAlgorithm.isSorted()) {
                state = AlgorithmState.FINISHED;
                updateButtonStates();
            }
        }
    }

    private int[] generateRandomArray(int size) {
        int[] arr = new int[size];
        for (int i = 0; i < size; i++) arr[i] = (int) (Math.random() * 100) + 1;
        return arr;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Visualizer::new);
    }
}