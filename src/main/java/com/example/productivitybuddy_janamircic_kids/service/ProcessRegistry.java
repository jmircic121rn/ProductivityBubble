package com.example.productivitybuddy_janamircic_kids.service;

import com.example.productivitybuddy_janamircic_kids.model.ProcessModel;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

public class ProcessRegistry {

    private final ConcurrentHashMap<String, ProcessModel> processMap =
            new ConcurrentHashMap<>();

    public void addNewProcessIfNotExist(String processName, ProcessModel processInfo) {
        processMap.putIfAbsent(processName, processInfo);
    }

    public ProcessModel getProcessByName(String processName) {
        return processMap.get(processName);
    }

    public void setCpuAndRamUsage(String processName, double cpuUsage, long ramUsage) {
        ProcessModel info = processMap.get(processName);
        if (info != null) {
            info.setCpuUsage(cpuUsage);
            info.setRamUsage(ramUsage);
        }
    }

    public void removeProcess(String processName) {
        processMap.remove(processName);
    }

    public Collection<ProcessModel> getAllProcesses() {
        return processMap.values();
    }

    public boolean isProcessInMap(String processName) {
        return processMap.containsKey(processName);
    }

    public void updateTotalTimeForAllProcesses() {
        for (ProcessModel process : processMap.values()) {
            process.setTotalTimeSeconds(
                    process.getTotalTimeSeconds() +
                            process.getProcessSessionTimeSeconds());
        }
    }

}
