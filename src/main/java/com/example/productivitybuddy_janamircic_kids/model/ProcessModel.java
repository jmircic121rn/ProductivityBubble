package com.example.productivitybuddy_janamircic_kids.model;

public class ProcessModel {
    //za json
    private String originalName;
    private String aliasName;
    private Category category;
    private boolean isTrackingFreezed;
    private long totalTimeSeconds;
    //za pracenje procesa gasenje paljenje
    private transient long processId;
    private transient double cpuUsage;
    private transient long ramUsage;
    private transient long processSessionTimeSeconds;
    private transient long processStartTimeOS;
    private transient long processStartTimeInApp;

    public ProcessModel(String originalName, Category category, long processId) {
        this.originalName = originalName;
        this.category = category;
        this.processId = processId;
        this.aliasName = originalName;
        this.isTrackingFreezed = false;
        this.processSessionTimeSeconds = 0;
        this.processStartTimeOS = System.currentTimeMillis();
        this.processStartTimeInApp = System.currentTimeMillis();
    }

    public String getOriginalName() {
        return originalName;
    }

    public void setOriginalName(String originalName) {
        this.originalName = originalName;
    }

    public String getAliasName() {
        return aliasName;
    }

    public void setAliasName(String aliasName) {
        this.aliasName = aliasName;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public boolean isTrackingFreezed() {
        return isTrackingFreezed;
    }

    public void setTrackingFreezed(boolean trackingFreezed) {
        isTrackingFreezed = trackingFreezed;
    }

    public long getTotalTimeSeconds() {
        return totalTimeSeconds;
    }

    public void setTotalTimeSeconds(long totalTimeSeconds) {
        this.totalTimeSeconds = totalTimeSeconds;
    }

    public long getProcessId() {
        return processId;
    }

    public void setProcessId(long processId) {
        this.processId = processId;
    }

    public double getCpuUsage() {
        return cpuUsage;
    }

    public void setCpuUsage(double cpuUsage) {
        this.cpuUsage = cpuUsage;
    }

    public long getRamUsage() {
        return ramUsage;
    }

    public void setRamUsage(long ramUsage) {
        this.ramUsage = ramUsage;
    }

    public long getProcessSessionTimeSeconds() {
        return processSessionTimeSeconds;
    }

    public void setProcessSessionTimeSeconds(long processSessionTimeSeconds) {
        this.processSessionTimeSeconds = processSessionTimeSeconds;
    }

    public long getProcessStartTimeOS() {
        return processStartTimeOS;
    }

    public void setProcessStartTimeOS(long processStartTimeOS) {
        this.processStartTimeOS = processStartTimeOS;
    }

    public long getProcessStartTimeInApp() {
        return processStartTimeInApp;
    }

    public void setProcessStartTimeInApp(long processStartTimeInApp) {
        this.processStartTimeInApp = processStartTimeInApp;
    }
}