package com.example.productivitybuddy_janamircic_kids.service;

import com.example.productivitybuddy_janamircic_kids.model.ProcessModel;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class FileService {

    private final ProcessRegistry registryHashMapProcess;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final Gson gson = new GsonBuilder().setPrettyPrinting().serializeNulls().create();
    private final Object fileLock = new Object();
    private final java.util.concurrent.ConcurrentHashMap<String, ProcessModel>
            pendingProcesses = new java.util.concurrent.ConcurrentHashMap<>();

    public FileService(ProcessRegistry registryHashMapProcess) {
        this.registryHashMapProcess = registryHashMapProcess;
    }

    public void loadProcessesFromFileToApp(String filePath){
        executorService.submit(() -> {
            synchronized (fileLock) {
                try{
                    String content = new String(Files.readAllBytes(Paths.get(filePath)));
                    ProcessInfoWrapper processInfoWrapper = gson.fromJson(content, ProcessInfoWrapper.class);

                    if(processInfoWrapper != null && processInfoWrapper.processes != null){
                        for(ProcessModel processModel : processInfoWrapper.processes){
                            if(registryHashMapProcess.isProcessInMap(processModel.getOriginalName())){
                                ProcessModel existingProcess = registryHashMapProcess.getProcessByName(processModel.getOriginalName());
                                existingProcess.setCategory(processModel.getCategory());
                                existingProcess.setAliasName(processModel.getAliasName());
                                existingProcess.setTotalTimeSeconds(processModel.getTotalTimeSeconds());
                                existingProcess.setTrackingFreezed(processModel.isTrackingFreezed());
                            }
                            else{
                                pendingProcesses.put(processModel.getOriginalName(), processModel);
                            }
                        }
                    }
                } catch (IOException | JsonSyntaxException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    public void saveProcessesToJsonFile(String filePath){
        executorService.submit(() -> {
            synchronized (fileLock) {
                try{
                    ProcessInfoWrapper processInfoWrapper = new ProcessInfoWrapper();
                    processInfoWrapper.processes = new ArrayList<>(registryHashMapProcess.getAllProcesses());
                    String json = gson.toJson(processInfoWrapper);
                    Files.write(Paths.get(filePath), json.getBytes());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void saveSnapshotToCSV (String folder){
        executorService.submit(() -> {
            synchronized (fileLock) {
                try{
                    Files.createDirectories(Paths.get(folder));
                    String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy_MM_dd_HH_mm_ss"));
                    String isoTimestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) + "Z";
                    String filePath = folder + "/snapshot_"+timestamp+".csv";
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append("timestamp,pid,process_name,cpu_usage,ram_usage,category,alias_name\n");

                    for(ProcessModel processModel : registryHashMapProcess.getAllProcesses()){
                        stringBuilder.append(isoTimestamp).append(",")
                                .append(processModel.getProcessId()).append(",")
                                .append(processModel.getOriginalName()).append(",")
                                .append(processModel.getCpuUsage()).append(",")
                                .append(processModel.getRamUsage()).append(",")
                                .append(processModel.getCategory()).append(",")
                                .append(processModel.getAliasName()).append("\n");
                    }
                    Files.write(Paths.get(filePath), stringBuilder.toString().getBytes());
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }
    public void applyPendingData(String processName, ProcessModel process) {
        ProcessModel pending = pendingProcesses.get(processName);
        if (pending != null) {
            process.setCategory(pending.getCategory());
            process.setAliasName(pending.getAliasName());
            process.setTotalTimeSeconds(pending.getTotalTimeSeconds());
            process.setTrackingFreezed(pending.isTrackingFreezed());
            pendingProcesses.remove(processName);
        }
    }

    public void shutdown(){
        executorService.shutdown();
        try{
            boolean finish = executorService.awaitTermination(10,TimeUnit.SECONDS);
            if(!finish){
                System.out.println("Service did not finish");
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    static class ProcessInfoWrapper{
        List<ProcessModel> processes;
    }
}
