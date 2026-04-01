package com.example.productivitybuddy_janamircic_kids.analytics;

import com.example.productivitybuddy_janamircic_kids.model.Category;
import com.example.productivitybuddy_janamircic_kids.model.ProcessModel;
import com.example.productivitybuddy_janamircic_kids.service.ProcessRegistry;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class AnalyticsService implements Runnable{

    private final ProcessRegistry registryHashMapProcess;
    private volatile boolean running = true;
    private final long interval;
    private final ConcurrentHashMap<Category, Long> processesTimePerCategory = new ConcurrentHashMap<>();
    private final List<ProcessModel> top10 = new ArrayList<>();
    private final Object top10Lock = new Object();

    public AnalyticsService(ProcessRegistry registryHashMapProcess, long interval) {
        this.registryHashMapProcess = registryHashMapProcess;
        this.interval = interval;
    }

    @Override
    public void run() {
        while (running) {
            try{
                updateTime();
                calculateStatistics();
                Thread.sleep(interval);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private void updateTime() {
        long currentTime = System.currentTimeMillis();

        for(ProcessModel processModel : registryHashMapProcess.getAllProcesses()) {
            if(!processModel.isTrackingFreezed()){
                long timePassed = (currentTime - processModel.getProcessStartTimeInApp()) / 1000;
                processModel.setProcessSessionTimeSeconds(timePassed);
            }
        }
    }

    private void calculateStatistics() {
        Map<Category, Long> newTimePerCategory = new HashMap<>();
        newTimePerCategory.put(Category.Work, 0L);
        newTimePerCategory.put(Category.Creativity, 0L);
        newTimePerCategory.put(Category.Gaming, 0L);
        newTimePerCategory.put(Category.Entertainment, 0L);
        newTimePerCategory.put(Category.Productivity, 0L);
        newTimePerCategory.put(Category.Social_and_Communication, 0L);
        newTimePerCategory.put(Category.WebBrowsers, 0L);
        newTimePerCategory.put(Category.Other, 0L);
        newTimePerCategory.put(Category.Uncategorized, 0L);

        List<ProcessModel> allProcesses  = new ArrayList<>(registryHashMapProcess.getAllProcesses());

        for(ProcessModel processModel : allProcesses) {
            long totalTimeForProcess = processModel.getProcessSessionTimeSeconds() + processModel.getTotalTimeSeconds();
            if (totalTimeForProcess > 0) {
                newTimePerCategory.put(processModel.getCategory(), totalTimeForProcess + newTimePerCategory.get(processModel.getCategory()));
            }
        }

        processesTimePerCategory.putAll(newTimePerCategory);

        List<ProcessModel> newTop10Categories = allProcesses.stream().sorted((a, b) -> Long.compare(b.getTotalTimeSeconds() + b.getProcessSessionTimeSeconds(), a.getTotalTimeSeconds() +a.getProcessSessionTimeSeconds())).limit(10).toList();

        synchronized (top10Lock) {
            top10.clear();
            top10.addAll(newTop10Categories);
        }
    }

    public Map<Category, Long> getProcessesTimePerCategory() {
        return Collections.unmodifiableMap(processesTimePerCategory);
    }

    public List<ProcessModel> getTop10() {
        synchronized (top10Lock) {
            return new ArrayList<>(top10);
        }
    }

    public void stop(){
        running = false;
    }
}
