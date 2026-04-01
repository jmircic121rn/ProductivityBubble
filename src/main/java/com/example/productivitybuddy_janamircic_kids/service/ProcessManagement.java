package com.example.productivitybuddy_janamircic_kids.service;

import com.example.productivitybuddy_janamircic_kids.MainApp;
import com.example.productivitybuddy_janamircic_kids.model.Category;
import com.example.productivitybuddy_janamircic_kids.model.ProcessModel;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.TimeUnit;

public class ProcessManagement {

    private final ProcessRegistry registryHashMapProcess;
    private final ForkJoinPool pool;
    private static final int numForProcess = 10;

    public ProcessManagement(ProcessRegistry registryHashMapProcess) {
        this.registryHashMapProcess = registryHashMapProcess;
        this.pool = new ForkJoinPool();
    }

    public void getAllProcessesFromOS() {
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

            double cpu = getCpuUsage(processId);

            registryHashMapProcess.setCpuAndRamUsage(processName, cpu, Runtime.getRuntime().totalMemory()/1024/1024);
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

    private double getCpuUsage(long pid) {
        try {
            ProcessBuilder pb = new ProcessBuilder(
                    "bash", "-c",
                    "top -pid " + pid + " -l 1 -stats cpu | tail -1");
            pb.redirectErrorStream(true);
            Process process = pb.start();
            String output = new String(process.getInputStream().readAllBytes()).trim();
            return Double.parseDouble(output);
        } catch (Exception e) {
            return 0.0;
        }
    }


}
