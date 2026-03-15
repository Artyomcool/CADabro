package com.github.artyomcool.cadabro;

import jdk.jfr.Recording;
import jdk.jfr.consumer.RecordedEvent;
import jdk.jfr.consumer.RecordedFrame;
import jdk.jfr.consumer.RecordedStackTrace;
import jdk.jfr.consumer.RecordingFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public class SamplingProfiler {

    private static Recording recording;
    private static Path tempFile;

    public static void start(long intervalMs) {
        if (recording != null) return;
        try {
            tempFile = Files.createTempFile("cadabro-profile", ".jfr");
            recording = new Recording();
            recording.enable("jdk.ExecutionSample").with("period", intervalMs + " ms");
            recording.setDestination(tempFile);
            recording.start();
            System.out.println("[SamplingProfiler] Started JFR profiling, interval: " + intervalMs + "ms");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void stop() {
        if (recording == null) return;
        recording.stop();
        System.out.println("[SamplingProfiler] JFR profiling stopped");
        processJfrFile();
        try {
            Files.deleteIfExists(tempFile);
        } catch (IOException e) {
            // ignore
        }
        recording = null;
    }

    private static void processJfrFile() {
        Map<String, Long> samples = new HashMap<>();
        try (RecordingFile rf = new RecordingFile(tempFile)) {
            while (rf.hasMoreEvents()) {
                RecordedEvent event = rf.readEvent();
                if ("jdk.ExecutionSample".equals(event.getEventType().getName())) {
                    RecordedStackTrace stackTrace = event.getStackTrace();
                    if (stackTrace != null) {
                        List<RecordedFrame> frames = stackTrace.getFrames();
                        StringBuilder sb = new StringBuilder();
                        for (int i = 0; i < Math.min(frames.size(), 5); i++) {
                            RecordedFrame frame = frames.get(i);
                            if (i > 0) sb.append(" -> ");
                            sb.append(frame.getMethod().getType().getName())
                                    .append(".")
                                    .append(frame.getMethod().getName())
                                    .append(":")
                                    .append(frame.getLineNumber());
                        }
                        samples.merge(sb.toString(), 1L, Long::sum);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        printResults(samples);
    }

    public static void printResults(Map<String, Long> samples) {
        System.out.println("--- JFR Profiling Results (Top Stack Traces) ---");
        List<Map.Entry<String, Long>> sortedSamples = samples.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(30)
                .collect(Collectors.toList());

        long totalSamples = samples.values().stream().mapToLong(Long::longValue).sum();
        if (totalSamples == 0) {
            System.out.println("No samples collected.");
            return;
        }

        for (Map.Entry<String, Long> entry : sortedSamples) {
            double percent = (entry.getValue() * 100.0) / totalSamples;
            System.out.printf("%6.2f%% (%d) %s%n", percent, entry.getValue(), entry.getKey());
        }
        System.out.println("--------------------------------------");
    }
}
