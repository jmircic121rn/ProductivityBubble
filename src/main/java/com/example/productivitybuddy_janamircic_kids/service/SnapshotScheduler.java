package com.example.productivitybuddy_janamircic_kids.service;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class SnapshotScheduler {

    private final FileService fileService;
    private final String folder;
    private final ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
    private final List<LocalTime> fixedTimes = new ArrayList<>();
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");

    public SnapshotScheduler(FileService fileService, long intervalSeconds, String folder, List<String> fixedTimesFromFile) {
        this.fileService = fileService;
        this.folder = folder;

        for(String time : fixedTimesFromFile) {
            fixedTimes.add(LocalTime.parse(time, formatter));
        }

        if (intervalSeconds > 0) {
            scheduledExecutorService.scheduleAtFixedRate(this::makeASnapshot,
                    intervalSeconds, intervalSeconds, TimeUnit.SECONDS);
        }

        scheduledExecutorService.scheduleAtFixedRate(this::checkFixedTimes, 0, 1, TimeUnit.SECONDS);
    }

    private void makeASnapshot() {
        fileService.saveSnapshotToCSV(folder);
    }

    private void checkFixedTimes() {
        LocalTime now = LocalTime.now().withNano(0);
        for(LocalTime time : fixedTimes) {
            if(now.equals(time)) {
                makeASnapshot();
            }
        }
    }

    public void shutdown() {
        scheduledExecutorService.shutdown();
        try{
            boolean finish = scheduledExecutorService.awaitTermination(5, TimeUnit.SECONDS);
            if(!finish) {
                System.err.println("Timed out waiting for snapshots to finish");
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
