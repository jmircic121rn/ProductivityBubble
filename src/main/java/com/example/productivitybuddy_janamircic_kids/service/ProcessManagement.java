package com.example.productivitybuddy_janamircic_kids.service;

import com.example.productivitybuddy_janamircic_kids.MainApp;
import com.example.productivitybuddy_janamircic_kids.model.Category;
import com.example.productivitybuddy_janamircic_kids.model.ProcessModel;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.TimeUnit;

public class ProcessManagement {

    private final ProcessRegistry registryHashMapProcess;
    private final ForkJoinPool pool;
    private static final int numForProcess = 10;

    private volatile Map<Long, double[]> psData = new HashMap<>();

    public ProcessManagement(ProcessRegistry registryHashMapProcess) {
        this.registryHashMapProcess = registryHashMapProcess;
        this.pool = new ForkJoinPool();
    }

    public void getAllProcessesFromOS() {
        psData = fetchAllPsData();

        List<ProcessHandle> allProcesses = ProcessHandle.allProcesses()
                .toList();

        pool.invoke(new DivideTasks(allProcesses));
    }

    private class DivideTasks extends RecursiveAction {

        private final List<ProcessHandle> allProcesses;

        public DivideTasks(List<ProcessHandle> allProcesses) {
            this.allProcesses = allProcesses;
        }

        @Override
        protected void compute() {
            if(allProcesses.size() <= numForProcess) {
                for (ProcessHandle processHandle : allProcesses) {
                    process(processHandle);
                }
                return;
            }
            int divide = allProcesses.size() / 2;
            DivideTasks left = new DivideTasks(allProcesses.subList(0, divide));
            DivideTasks right = new DivideTasks(allProcesses.subList(divide, allProcesses.size()));
            left.fork();
            right.fork();
            left.join();
            right.join();
        }
    }

    private void process(ProcessHandle processHandle) {
        try {
            if (!processHandle.isAlive()) return;

            long processId = processHandle.pid();
            String processName = processHandle.info().command().map(cmd -> cmd.substring(cmd.lastIndexOf("/") + 1)).orElse("" + processId);
            long processStartTimeOS = processHandle.info().startInstant().map(Instant::toEpochMilli).orElse(0L);

            ProcessModel processExist = registryHashMapProcess.getProcessByName(processName);

            if(processExist != null) {
                if(processExist.getProcessStartTimeOS() != processStartTimeOS) {
                    registryHashMapProcess.removeProcess(processName);
                    processExist = null;
                }
            }

            if(processExist == null) {
                ProcessModel process = new ProcessModel(processName, Category.Uncategorized, processId);
                process.setProcessStartTimeOS(processStartTimeOS);
                process.setProcessStartTimeInApp(System.currentTimeMillis());
                registryHashMapProcess.addNewProcessIfNotExist(processName, process);

                MainApp.fileService.applyPendingData(processName, process);
            }

            double[] stats = psData.getOrDefault(processId, new double[]{0.0, 0.0});
            double cpu = stats[0];
            long ram = (long) stats[1];

            registryHashMapProcess.setCpuAndRamUsage(processName, cpu, ram);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void shutdown() {
        pool.shutdown();
        try {
            boolean shutdownOkay = pool.awaitTermination(5, TimeUnit.SECONDS);
            if (!shutdownOkay) {
                System.out.println("Pool did not terminate");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private Map<Long, double[]> fetchAllPsData() {
        Map<Long, double[]> result = new HashMap<>();
        try {
            ProcessBuilder pb = new ProcessBuilder(
                    "ps", "-eo", "pid,%cpu,rss");
            pb.redirectErrorStream(true);
            Process process = pb.start();
            String output = new String(process.getInputStream().readAllBytes());
            String[] lines = output.split("\n");
            for (int i = 1; i < lines.length; i++) {
                String line = lines[i].trim();
                if (line.isEmpty()) continue;
                String[] parts = line.split("\\s+");
                if (parts.length >= 3) {
                    try {
                        long pid = Long.parseLong(parts[0]);
                        double cpu = Double.parseDouble(parts[1]);
                        long rssKb = Long.parseLong(parts[2]);
                        result.put(pid, new double[]{cpu, rssKb / 1024.0});
                    } catch (NumberFormatException ignored) {
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }


}
